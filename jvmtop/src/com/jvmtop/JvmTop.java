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
        .accepts("once",
            "jvmtop will exit after first output iteration [deprecated, use -n 1 instead]");
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

    parser
        .acceptsAll(Arrays.asList(new String[] { "p", "pid" }),
            "PID to connect to").withRequiredArg().ofType(Integer.class);

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

    double delay = 1.0;

    boolean profileMode = a.has("profile");

    Integer iterations = a.has("once") ? 1 : -1;

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

    if (a.hasArgument("PID"))
    {
      pid = (Integer) a.valueOf("PID");
    }

    if (a.has("verbose"))
    {
      fineLogging();
      logger.setLevel(Level.ALL);
      logger.fine("Verbosity mode.");
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
        jvmTop.run(new VMOverviewView());
      }
      else
      {
        if (profileMode)
        {
          jvmTop.run(new VMProfileView(pid));
        }
        else
        {
          jvmTop.run(new VMDetailView(pid));

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

  protected void run(ConsoleView view) throws Exception
  {
    try
    {
      System.setOut(new PrintStream(new BufferedOutputStream(
          new FileOutputStream(FileDescriptor.out)), false));
      int iterations = 0;
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
    System.out.println(" http://code.google.com/p/jvmtop");
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
