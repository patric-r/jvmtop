/**
 * jvmtop - java monitoring for the command-line
 *
 * Copyright (C) 2013 by Patric Rufflar. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.jvmtop;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import com.jvmtop.view.ConsoleView;
import com.jvmtop.view.VMDetailView;
import com.jvmtop.view.VMOverviewView;
import com.jvmtop.view.VMProfileView;

/**
 * JvmTop entry point class.
 *
 * - parses program arguments - selects console view - prints header - main
 * "iteration loop"
 *
 * TODO: refactor to split these tasks
 *
 * @author paru
 *
 */
public class JvmTop
{

  public static final String                         VERSION                 = "1.0.0 alpha";

  private Double                                     delay_                  = 1.0;

  private Boolean                                    supportsSystemAverage_;

  private java.lang.management.OperatingSystemMXBean localOSBean_;

  private final static String                        CLEAR_TERMINAL_ANSI_CMD = new String(
      new byte[] { (byte) 0x1b, (byte) 0x5b, (byte) 0x32, (byte) 0x4a,
          (byte) 0x1b, (byte) 0x5b, (byte) 0x48 });

  private int                                        maxIterations_          = -1;

  private static Logger                              logger;

  private boolean                                    noANSITerminal_;

  private boolean                                    altClearRequired_;

  private static OptionParser createOptionParser()
  {
    OptionParser parser = new OptionParser();
    parser.acceptsAll(Arrays.asList(new String[] { "help", "?", "h" }),
        "shows this help").forHelp();
    parser.accepts("once",
        "jvmtop will exit after first output iteration [deprecated, use -n 1 instead]");
    parser
        .acceptsAll(Arrays.asList(new String[] { "n", "iteration" }),
            "jvmtop will exit after n output iterations")
        .withRequiredArg().ofType(Integer.class);
    parser
        .acceptsAll(Arrays.asList(new String[] { "d", "delay" }),
            "delay between each output iteration")
        .withRequiredArg().ofType(Double.class);
    parser.accepts("profile", "start CPU profiling at the specified jvm");
    parser.accepts("sysinfo", "outputs diagnostic information");
    parser.accepts("verbose", "verbose mode");
    parser
        .accepts("threadlimit",
            "sets the number of displayed threads in detail mode")
        .withRequiredArg().ofType(Integer.class);
    parser.accepts("disable-threadlimit",
        "displays all threads in detail mode");
    parser.accepts("disable-printvminfo",
        "does not print VM infos in detail mode");

    parser.acceptsAll(Arrays.asList(new String[] { "p", "pid" }),
        "PID to connect to").withRequiredArg().ofType(Integer.class);

    parser
        .acceptsAll(Arrays.asList(new String[] { "w", "width" }),
            "Width in columns for the console display")
        .withRequiredArg().ofType(Integer.class);

    parser
        .accepts("threadnamewidth",
            "sets displayed thread name length in detail mode (defaults to 30)")
        .withRequiredArg().ofType(Integer.class);

    return parser;
  }

  public static void main(String[] args) throws Exception
  {
    Locale.setDefault(Locale.US);

    logger = Logger.getLogger("jvmtop");

    OptionParser parser = createOptionParser();
    try
    {
      parseAndRun(args, parser);
    }
    catch (JvmTopException e)
    {
      System.err.println(e.getMessage());
      printHelp(parser);
      System.exit(-1);
    }
  }

  private static void parseAndRun(String[] args, OptionParser parser)
      throws IOException, Exception
  {
    OptionSet optionSet = parser.parse(args);
    if (optionSet.has("help"))
    {
      printHelp(parser);
      System.exit(0);
    }
    boolean sysInfoOption = optionSet.has("sysinfo");

    Integer pid = null;

    Integer width = null;

    double delay = 1.0;

    boolean profileMode = optionSet.has("profile");

    Integer iterations = optionSet.has("once") ? 1 : -1;

    Integer threadlimit = null;

    boolean threadLimitEnabled = true;

    boolean printVMInfo = true;

    Integer threadNameWidth = null;

    if (optionSet.hasArgument("delay"))
    {
      delay = (Double) (optionSet.valueOf("delay"));
      if (delay < 0.1d)
        throw new IllegalArgumentException("Delay cannot be set below 0.1");
    }

    if (optionSet.hasArgument("n"))
      iterations = (Integer) optionSet.valueOf("n");

    if (optionSet.hasArgument("pid"))
    {
      pid = (Integer) optionSet.valueOf("pid");
    }
    else
    {
      // to support PID as non option argument
      if (optionSet.nonOptionArguments().size() > 0)
        pid = safeValueOf((String) optionSet.nonOptionArguments().get(0));
    }

    if (optionSet.hasArgument("width"))
      width = (Integer) optionSet.valueOf("width");

    if (optionSet.hasArgument("threadlimit"))
      threadlimit = (Integer) optionSet.valueOf("threadlimit");

    if (optionSet.has("disable-threadlimit"))
      threadLimitEnabled = false;

    if (optionSet.has("disable-printvminfo"))
      printVMInfo = false;

    if (optionSet.has("verbose"))
    {
      fineLogging();
      logger.setLevel(Level.ALL);
      logger.fine("Verbosity mode.");
    }

    if (optionSet.hasArgument("threadnamewidth"))
      threadNameWidth = (Integer) optionSet.valueOf("threadnamewidth");

    if (sysInfoOption)
      outputSystemProps();
    else
    {
      JvmTop jvmTop = new JvmTop();
      jvmTop.setDelay(delay);
      jvmTop.setMaxIterations(iterations);
      if (pid == null)
        jvmTop.run(new VMOverviewView(width));
      else if (profileMode)
        jvmTop.run(new VMProfileView(pid, width));
      else
      {
        VMDetailView vmDetailView = new VMDetailView(pid, width);
        vmDetailView.setDisplayedThreadLimit(threadLimitEnabled);
        vmDetailView.setPrintVMInfo(printVMInfo);
        if (threadlimit != null)
          vmDetailView.setNumberOfDisplayedThreads(threadlimit);
        if (threadNameWidth != null)
          vmDetailView.setThreadNameDisplayWidth(threadNameWidth);
        jvmTop.run(vmDetailView);
      }
    }
  }

  /**
   * @param string
   * @return
   * @throws JvmTopException 
   */
  private static Integer safeValueOf(String string) throws JvmTopException
  {
    try
    {
      return Integer.valueOf(string);
    }
    catch (NumberFormatException e)
    {
      throw new JvmTopException(
          "Required integer parameter instead of" + string);
    }
  }

  private static void printHelp(OptionParser parser) throws IOException
  {
    System.out.println("jvmtop - java monitoring for the command-line");
    System.out.println("Usage: jvmtop.sh [options...] [PID]");
    System.out.println("");
    parser.printHelpOn(System.out);
  }

  public int getMaxIterations()
  {
    return maxIterations_;
  }

  public void setMaxIterations(int iterations)
  {
    maxIterations_ = iterations;
  }

  private static void fineLogging()
  {
    // get the top Logger:
    Logger topLogger = java.util.logging.Logger.getLogger("");

    // Handler for console (reuse it if it already exists)
    Handler consoleHandler = null;
    // see if there is already a console handler
    for (Handler handler : topLogger.getHandlers())
      if (handler instanceof ConsoleHandler)
      {
        // found the console handler
        consoleHandler = handler;
        break;
      }

    if (consoleHandler == null)
    {
      // there was no console handler found, create a new one
      consoleHandler = new ConsoleHandler();
      topLogger.addHandler(consoleHandler);
    }
    // set the console handler to fine:
    consoleHandler.setLevel(java.util.logging.Level.FINEST);
  }

  private static void outputSystemProps()
  {
    for (Object key : System.getProperties().keySet())
      System.out.println(key + "=" + System.getProperty(key + ""));
  }

  protected void run(ConsoleView view) throws Exception
  {
    try (PrintStream ps = new PrintStream(
        new BufferedOutputStream(new FileOutputStream(FileDescriptor.out)),
        false))
    {
      System.setOut(ps);
      view.setPrintStream(ps);
      int iterations = 0;
      while (!view.shouldExit())
      {
        if (maxIterations_ > 1 || maxIterations_ == -1)
          clearTerminal(ps);
        printTopBar(ps);
        view.printView();
        ps.flush();
        iterations++;
        if (iterations >= maxIterations_ && maxIterations_ > 0)
          break;
        view.sleep((int) (delay_ * 1000));
      }
    }
    catch (NoClassDefFoundError e)
    {
      e.printStackTrace(System.err);

      System.err.println("");
      System.err.println("ERROR: Some JDK classes cannot be found.");
      System.err.println(
          "       Please check if the JAVA_HOME environment variable has been set to a JDK path.");
      System.err.println("");
    }
  }

  /**
   * @param ps 
   *
   */
  private void clearTerminal(PrintStream ps)
  {
    if (noANSITerminal_)
      // hack
      ps.printf(
          "%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n");
    else if (altClearRequired_)
      ps.print('\f');
    else
      ps.print(CLEAR_TERMINAL_ANSI_CMD);
  }

  public JvmTop()
  {
    localOSBean_ = ManagementFactory.getOperatingSystemMXBean();
    noANSITerminal_ = System.getProperty("os.name").contains("Windows")
        && System.getenv("ANSICON") == null;
    altClearRequired_ = System.getProperty("jvmtop.altClear") != null;
  }

  /**
   * @param ps 
   * @throws NoSuchMethodException
   * @throws SecurityException
   *
   */
  private void printTopBar(PrintStream ps)
  {
    ps.printf(" JvmTop %s - %8tT, %6s, %2d cpus, %15.15s", VERSION, new Date(),
        localOSBean_.getArch(), localOSBean_.getAvailableProcessors(),
        localOSBean_.getName() + " " + localOSBean_.getVersion());

    if (supportSystemLoadAverage() && localOSBean_.getSystemLoadAverage() != -1)
      ps.printf(", load avg %3.2f%n", localOSBean_.getSystemLoadAverage());
    else
      ps.println();
    ps.println(" https://github.com/patric-r/jvmtop");
    ps.println();
  }

  private boolean supportSystemLoadAverage()
  {
    if (supportsSystemAverage_ == null)
      try
      {
        supportsSystemAverage_ = (localOSBean_.getClass()
            .getMethod("getSystemLoadAverage") != null);
      }
      catch (Throwable e)
      {
        supportsSystemAverage_ = false;
      }
    return supportsSystemAverage_;
  }

  public Double getDelay()
  {
    return delay_;
  }

  public void setDelay(Double delay)
  {
    delay_ = delay;
  }

}
