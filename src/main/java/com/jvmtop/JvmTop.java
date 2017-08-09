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
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jvmtop.profiler.Config;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import com.jvmtop.view.ConsoleView;
import com.jvmtop.view.VMDetailView;
import com.jvmtop.view.VMOverviewView;
import com.jvmtop.view.VMProfileView;

/**
 * JvmTop entry point class.
 *
 * - parses program arguments
 * - selects console view
 * - prints header
 * - main "iteration loop"
 *
 * TODO: refactor to split these tasks
 *
 * @author paru
 *
 */
public class JvmTop
{

  public static final String                         VERSION                 = "0.8.0 alpha";

  private Double                                     delay_                  = 1.0;

  private Boolean                                    supportsSystemAverage_;

  private java.lang.management.OperatingSystemMXBean localOSBean_;

  private final static String                        CLEAR_TERMINAL_ANSI_CMD = new String(
                                                                                 new byte[] {
      (byte) 0x1b, (byte) 0x5b, (byte) 0x32, (byte) 0x4a, (byte) 0x1b,
      (byte) 0x5b, (byte) 0x48                                                  });

  private int                                        maxIterations_          = -1;

  private static Logger                              logger;

  private static OptionParser createOptionParser()
  {
    OptionParser parser = new OptionParser();
    parser.acceptsAll(Arrays.asList(new String[] { "help", "?", "h" }),
        "shows this help").forHelp();
    parser
        .acceptsAll(Arrays.asList(new String[] { "n", "iteration" }),
            "jvmtop will exit after n output iterations").withRequiredArg()
        .ofType(Integer.class);
    parser
        .acceptsAll(Arrays.asList(new String[] { "d", "delay" }),
            "delay between each output iteration").withRequiredArg()
        .ofType(Double.class);
    parser.accepts("profile", "start CPU profiling at the specified jvm");
    parser.accepts("sysinfo", "outputs diagnostic information");
    parser.accepts("verbose", "verbose mode");
    parser.accepts("threadlimit",
        "sets the number of displayed threads in detail mode")
        .withRequiredArg().ofType(Integer.class);
    parser
        .accepts("disable-threadlimit", "displays all threads in detail mode");

    parser
        .acceptsAll(Arrays.asList(new String[] { "p", "pid" }),
            "PID to connect to").withRequiredArg().ofType(Integer.class);

    parser
        .acceptsAll(Arrays.asList(new String[] { "w", "width" }),
            "Width in columns for the console display").withRequiredArg().ofType(Integer.class);

    parser
        .accepts("threadnamewidth",
            "sets displayed thread name length in detail mode (defaults to 30)")
        .withRequiredArg().ofType(Integer.class);

    parser.accepts("profileMinCost",
            "Profiler minimum function cost to be in output")
            .withRequiredArg().ofType(Double.class);
    parser.accepts("profileMinTotal",
            "Profiler minimum thread cost to be in output")
            .withRequiredArg().ofType(Double.class);
    parser.accepts("profileMaxDepth",
            "Profiler maximum function depth in output")
            .withRequiredArg().ofType(Integer.class);

    parser.accepts("profileFileVisualize",
            "Profiler file to output result")
            .withRequiredArg().ofType(String.class);
    parser.accepts("profileJsonVisualize",
            "Profiler file to output result (JSON format)")
            .withRequiredArg().ofType(String.class);
    parser.accepts("profileCachegrindVisualize",
            "Profiler file to output result (Cachegrind format)")
            .withRequiredArg().ofType(String.class);
    parser.accepts("profileFlameVisualize",
            "Profiler file to output result (Flame graph format)")
            .withRequiredArg().ofType(String.class);

    parser.accepts("profileCanSkip",
            "Profiler ability to skip intermediate functions with same cpu usage as their parent");
    parser.accepts("profilePrintTotal",
            "Profiler printing percent of total thread cpu");
    parser.accepts("profileRealTime",
            "Profiler uses real time instead of cpu time (usable for sleeps profiling)");


    parser.accepts("profileThreadIds",
            "Profiler thread ids to profile (id is #123 after thread name), separated by comma")
            .withRequiredArg().ofType(Long.class).withValuesSeparatedBy(',');
    parser.accepts("profileThreadNames",
            "Profiler thread names to profile, separated by comma")
            .withRequiredArg().ofType(String.class).withValuesSeparatedBy(',');

    return parser;
  }

  public static void main(String[] args) throws Exception
  {
    Locale.setDefault(Locale.US);

    logger = Logger.getLogger("jvmtop");

    OptionParser parser = createOptionParser();
    OptionSet a = parser.parse(args);

    if (a.has("help"))
    {
      System.out.println("jvmtop - java monitoring for the command-line");
      System.out.println("Usage: jvmtop.sh [options...] [PID]");
      System.out.println("");
      parser.printHelpOn(System.out);
      System.exit(0);
    }
    boolean sysInfoOption = a.has("sysinfo");

    Integer pid = null;

    Integer width = null;

    double delay = 1.0;

    boolean profileMode = a.has("profile");

    Integer iterations = -1;

    Integer threadlimit = null;

    boolean threadLimitEnabled = true;

    Integer threadNameWidth = null;

    Double minTotal = null;
    Double minCost = null;
    Integer maxDepth = null;
    boolean canSkip = false;
    boolean printTotal = false;
    boolean profileRealTime = false;
    String fileVisualize = null;
    String jsonVisualize = null;
    String cachegrindVisualize = null;
    String flameVisualize = null;
    List<Long> profileThreadIds = null;
    List<String> profileThreadNames = null;

    if (a.hasArgument("delay"))
    {
      delay = (Double) (a.valueOf("delay"));
      if (delay < 0.1d)
      {
        throw new IllegalArgumentException("Delay cannot be set below 0.1");
      }
    }

    if (a.hasArgument("n"))
    {
      iterations = (Integer) a.valueOf("n");
    }

    //to support PID as non option argument
    if (a.nonOptionArguments().size() > 0)
    {
      pid = Integer.valueOf((String) a.nonOptionArguments().get(0));
    }

    if (a.hasArgument("pid"))
    {
      pid = (Integer) a.valueOf("pid");
    }

    if (a.hasArgument("width"))
    {
      width = (Integer) a.valueOf("width");
    }

    if (a.hasArgument("threadlimit"))
    {
      threadlimit = (Integer) a.valueOf("threadlimit");
    }

    if (a.has("disable-threadlimit"))
    {
      threadLimitEnabled = false;
    }

    if (a.has("verbose"))
    {
      fineLogging();
      logger.setLevel(Level.ALL);
      logger.fine("Verbosity mode.");
    }

    if (a.hasArgument("threadnamewidth"))
    {
      threadNameWidth = (Integer) a.valueOf("threadnamewidth");
    }

    if (a.hasArgument("profileMinCost")) {
      minCost = (Double)a.valueOf("profileMinCost");
    }

    if (a.hasArgument("profileMinTotal")) {
      minTotal = (Double)a.valueOf("profileMinTotal");
    }

    if (a.hasArgument("profileMaxDepth")) {
      maxDepth = (Integer) a.valueOf("profileMaxDepth");
    }

    if (a.has("profileCanSkip")) {
      canSkip = true;
    }

    if (a.has("profilePrintTotal")) {
      printTotal = true;
    }

    if (a.has("profileRealTime")) {
      profileRealTime = true;
    }

    // VISUALIZE
    if (a.hasArgument("profileFileVisualize")) {
      fileVisualize = (String) a.valueOf("profileFileVisualize");
    }

    if (a.hasArgument("profileJsonVisualize")) {
      jsonVisualize = (String) a.valueOf("profileJsonVisualize");
    }

    if (a.hasArgument("profileCachegrindVisualize")) {
      cachegrindVisualize = (String) a.valueOf("profileCachegrindVisualize");
    }

    if (a.hasArgument("profileFlameVisualize")) {
      flameVisualize = (String) a.valueOf("profileFlameVisualize");
    }

    if (a.hasArgument("profileThreadIds")) {
      @SuppressWarnings("unchecked")
      List<Long> list = (List<Long>) a.valuesOf("profileThreadIds");
      profileThreadIds = list;
    }

    if (a.hasArgument("profileThreadNames")) {
      @SuppressWarnings("unchecked")
      List<String> list = (List<String>) a.valuesOf("profileThreadNames");
      profileThreadNames = list;
    }

    if (sysInfoOption)
    {
      outputSystemProps();
    }
    else
    {
      JvmTop jvmTop = new JvmTop();
      jvmTop.setDelay(delay);
      jvmTop.setMaxIterations(iterations);
      if (pid == null)
      {
        jvmTop.run(new VMOverviewView(width));
      }
      else
      {
        if (profileMode)
        {
          jvmTop.run(new VMProfileView(pid, new Config(width, minCost, minTotal, maxDepth,
                  threadlimit, canSkip, printTotal,
                  profileRealTime, profileThreadIds, profileThreadNames,
                  fileVisualize, jsonVisualize, cachegrindVisualize, flameVisualize)));
        }
        else
        {
          VMDetailView vmDetailView = new VMDetailView(pid, width);
          vmDetailView.setDisplayedThreadLimit(threadLimitEnabled);
          if (threadlimit != null)
          {
            vmDetailView.setNumberOfDisplayedThreads(threadlimit);
          }
          if (threadNameWidth != null)
          {
            vmDetailView.setThreadNameDisplayWidth(threadNameWidth);
          }
          jvmTop.run(vmDetailView);

        }

      }
    }
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
    //get the top Logger:
    Logger topLogger = java.util.logging.Logger.getLogger("");

    // Handler for console (reuse it if it already exists)
    Handler consoleHandler = null;
    //see if there is already a console handler
    for (Handler handler : topLogger.getHandlers())
    {
      if (handler instanceof ConsoleHandler)
      {
        //found the console handler
        consoleHandler = handler;
        break;
      }
    }

    if (consoleHandler == null)
    {
      //there was no console handler found, create a new one
      consoleHandler = new ConsoleHandler();
      topLogger.addHandler(consoleHandler);
    }
    //set the console handler to fine:
    consoleHandler.setLevel(java.util.logging.Level.FINEST);
  }

  private static void outputSystemProps()
  {
    for (Object key : System.getProperties().keySet())
    {
      System.out.println(key + "=" + System.getProperty(key + ""));
    }
  }

  protected void run(final ConsoleView view) throws Exception
  {
    try
    {
      System.setOut(new PrintStream(new BufferedOutputStream(
          new FileOutputStream(FileDescriptor.out)), false));
      int iterations = 0;
      registerShutdown(view);
      while (!view.shouldExit())
      {
        if (maxIterations_ > 1 || maxIterations_ == -1)
        {
          clearTerminal();
        }
        printTopBar();
        view.printView();
        System.out.flush();
        iterations++;
        if (iterations >= maxIterations_ && maxIterations_ > 0)
        {
          break;
        }
        view.sleep((int) (delay_ * 1000));
      }
//      view.last();
    }
    catch (NoClassDefFoundError e)
    {
      e.printStackTrace(System.err);

      System.err.println("");
      System.err.println("ERROR: Some JDK classes cannot be found.");
      System.err
          .println("       Please check if the JAVA_HOME environment variable has been set to a JDK path.");
      System.err.println("");
    }
  }

  private static void registerShutdown(final ConsoleView view) {
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          System.out.print("Finish execution ... ");
          view.last();
          System.out.println("done!");
        } catch (Exception e) {
          System.err.println("Failed to run last in shutdown");
          e.printStackTrace();
        }
      }
    }));
  }

  /**
   *
   */
  private void clearTerminal()
  {
    if (System.getProperty("os.name").contains("Windows"))
    {
      //hack
      System.out
          .printf("%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n");
    }
    else if (System.getProperty("jvmtop.altClear") != null)
    {
      System.out.print('\f');
    }
    else
    {
      System.out.print(CLEAR_TERMINAL_ANSI_CMD);
    }
  }

  public JvmTop()
  {
    localOSBean_ = ManagementFactory.getOperatingSystemMXBean();
  }

  /**
   * @throws NoSuchMethodException
   * @throws SecurityException
   *
   */
  private void printTopBar()
  {
    System.out.printf(" JvmTop %s - %8tT, %6s, %2d cpus, %15.15s", VERSION,
        new Date(), localOSBean_.getArch(),
        localOSBean_.getAvailableProcessors(), localOSBean_.getName() + " "
            + localOSBean_.getVersion());

    if (supportSystemLoadAverage() && localOSBean_.getSystemLoadAverage() != -1)
    {
      System.out.printf(", load avg %3.2f%n",
          localOSBean_.getSystemLoadAverage());
    }
    else
    {
      System.out.println();
    }
    System.out.println(" https://github.com/patric-r/jvmtop");
    System.out.println();
  }

  private boolean supportSystemLoadAverage()
  {
    if (supportsSystemAverage_ == null)
    {
      try
      {
        supportsSystemAverage_ = (localOSBean_.getClass().getMethod(
            "getSystemLoadAverage") != null);
      }
      catch (Throwable e)
      {
        supportsSystemAverage_ = false;
      }
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
