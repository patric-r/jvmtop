/**
 * jvmtop - java monitoring for the command-line
 * <p>
 * Copyright (C) 2013 by Patric Rufflar. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * <p>
 * <p>
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.jvmtop;

import com.jvmtop.cli.CommandLine;
import com.jvmtop.cli.CommandLine.Command;
import com.jvmtop.cli.CommandLine.Option;
import com.jvmtop.cli.CommandLine.Parameters;
import com.jvmtop.profiler.Config;
import com.jvmtop.view.ConsoleView;
import com.jvmtop.view.VMDetailView;
import com.jvmtop.view.VMOverviewView;
import com.jvmtop.view.VMProfileView;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

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
@Command(name = "JvmTop", description = "Java sampling command-line profiler", version = "1.0.2")
public class JvmTop {
    private final static String CLEAR_TERMINAL_ANSI_CMD = new String(new byte[]{
            (byte) 0x1b, (byte) 0x5b, (byte) 0x32, (byte) 0x4a, (byte) 0x1b,
            (byte) 0x5b, (byte) 0x48});

    private static Logger logger;

    private Boolean supportsSystemAverage_;

    private java.lang.management.OperatingSystemMXBean localOSBean_;

    @Option(names = {"-i", "--sysinfo"}, description = "Outputs diagnostic information")
    private boolean sysInfoOption = false;
    @Option(names = {"-v", "--verbose"}, description = "Outputs verbose logs")
    private boolean verbose = false;

    @Parameters(index = "0", description = "PID to connect to, override parameter")
    private Integer pidParameter = null;
    @Option(names = {"-p", "--pid"}, description = "PID to connect to")
    private Integer pid = null;

    @Option(names = {"-w", "--width"}, description = "Width in columns for the console display")
    private Integer width = null;

    @Option(names = {"-d", "--delay"}, description = "Delay between each output iteration")
    private double delay = 1.0;

    @Option(names = "--profile", description = "Start CPU profiling at the specified jvm")
    private boolean profileMode = false;

    @Option(names = {"-n", "--iteration"}, description = "jvmtop will exit after n output iterations")
    private Integer iterations = -1;

    @Option(names = "--threadlimit", description = "sets the number of displayed threads in detail mode")
    private Integer threadlimit = null;

    @Option(names = "--disable-threadlimit", description = "displays all threads in detail mode")
    private boolean threadLimitEnabled = true;

    @Option(names = "--threadnamewidth", description = "sets displayed thread name length in detail mode (defaults to 30)")
    private Integer threadNameWidth = null;

    @Option(names = "--profileMinTotal", description = "Profiler minimum thread cost to be in output")
    private Double minTotal;
    @Option(names = "--profileMinCost", description = "Profiler minimum function cost to be in output")
    private Double minCost;
    @Option(names = "--profileMaxDepth", description = "Profiler maximum function depth in output")
    private Integer maxDepth;
    @Option(names = "--profileCanSkip", description = "Profiler ability to skip intermediate functions with same cpu usage as their parent")
    private boolean canSkip = false;
    @Option(names = "--profilePrintTotal", description = "Profiler printing percent of total thread cpu")
    private boolean printTotal = false;
    @Option(names = "--profileRealTime", description = "Profiler uses real time instead of cpu time (usable for sleeps profiling)")
    private boolean profileRealTime = false;
    @Option(names = "--profileFileVisualize", description = "Profiler file to output result")
    private String fileVisualize;
    @Option(names = "--profileJsonVisualize", description = "Profiler file to output result (JSON format)")
    private String jsonVisualize;
    @Option(names = "--profileCachegrindVisualize", description = "Profiler file to output result (Cachegrind format)")
    private String cachegrindVisualize;
    @Option(names = "--profileFlameVisualize", description = "Profiler file to output result (Flame graph format)")
    private String flameVisualize;
    @Option(names = "--profileThreadIds", description = "Profiler thread ids to profile (id is #123 after thread name)", split = ",", type = Long.class)
    private List<Long> profileThreadIds;
    @Option(names = "--profileThreadNames", description = "Profiler thread names to profile", split = ",")
    private List<String> profileThreadNames;

    @Option(names = {"-V", "--version"}, versionHelp = true, description = "display version info")
    private boolean versionInfoRequested;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    private boolean usageHelpRequested;

    public JvmTop() {
        localOSBean_ = ManagementFactory.getOperatingSystemMXBean();
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);

        logger = Logger.getLogger("jvmtop");

        JvmTop jvmTop = new JvmTop();
        CommandLine commandLine = new CommandLine(jvmTop);
        commandLine.parse(args);

        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.err);
            System.exit(0);
        } else if (commandLine.isVersionHelpRequested()) {
            commandLine.printVersionHelp(System.err);
            System.exit(0);
        }

        if (jvmTop.delay < 0.1d) {
            throw new IllegalArgumentException("Delay cannot be set below 0.1");
        }

        if (jvmTop.verbose) {
            fineLogging();
            logger.setLevel(Level.ALL);
            logger.fine("Verbosity mode.");
        }

        if (jvmTop.pidParameter != null) {
            // support for parameter w/o name
            jvmTop.pid = jvmTop.pidParameter;
        }

        if (jvmTop.sysInfoOption) {
            outputSystemProps();
        } else {
            if (jvmTop.pid == null) {
                jvmTop.run(new VMOverviewView(jvmTop.width));
            } else {
                if (jvmTop.profileMode) {
                    jvmTop.run(new VMProfileView(jvmTop.pid, new Config(jvmTop.width, jvmTop.minCost, jvmTop.minTotal, jvmTop.maxDepth,
                            jvmTop.threadlimit, jvmTop.canSkip, jvmTop.printTotal,
                            jvmTop.profileRealTime, jvmTop.profileThreadIds, jvmTop.profileThreadNames,
                            jvmTop.fileVisualize, jvmTop.jsonVisualize, jvmTop.cachegrindVisualize, jvmTop.flameVisualize)));
                } else {
                    VMDetailView vmDetailView = new VMDetailView(jvmTop.pid, jvmTop.width);
                    vmDetailView.setDisplayedThreadLimit(jvmTop.threadLimitEnabled);
                    if (jvmTop.threadlimit != null) {
                        vmDetailView.setNumberOfDisplayedThreads(jvmTop.threadlimit);
                    }
                    if (jvmTop.threadNameWidth != null) {
                        vmDetailView.setThreadNameDisplayWidth(jvmTop.threadNameWidth);
                    }
                    jvmTop.run(vmDetailView);
                }
            }
        }
    }

    private static void fineLogging() {
        //get the top Logger:
        Logger topLogger = java.util.logging.Logger.getLogger("");

        // Handler for console (reuse it if it already exists)
        Handler consoleHandler = null;
        //see if there is already a console handler
        for (Handler handler : topLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                //found the console handler
                consoleHandler = handler;
                break;
            }
        }

        if (consoleHandler == null) {
            //there was no console handler found, create a new one
            consoleHandler = new ConsoleHandler();
            topLogger.addHandler(consoleHandler);
        }
        //set the console handler to fine:
        consoleHandler.setLevel(java.util.logging.Level.FINEST);
    }

    private static void outputSystemProps() {
        for (Object key : System.getProperties().keySet()) {
            System.out.println(key + "=" + System.getProperty(key + ""));
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

    protected void run(final ConsoleView view) throws Exception {
        try {
            System.setOut(new PrintStream(new BufferedOutputStream(
                    new FileOutputStream(FileDescriptor.out)), false));
            int iterations = 0;
            registerShutdown(view);
            while (!view.shouldExit()) {
                if (this.iterations > 1 || this.iterations == -1) {
                    clearTerminal();
                }
                printTopBar();
                view.printView();
                System.out.flush();
                iterations++;
                if (iterations >= this.iterations && this.iterations > 0) {
                    break;
                }
                view.sleep((int) (delay * 1000));
            }
//      view.last();
        } catch (NoClassDefFoundError e) {
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
    private void clearTerminal() {
        if (System.getProperty("os.name").contains("Windows")) {
            //hack
            System.out
                    .printf("%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n");
        } else if (System.getProperty("jvmtop.altClear") != null) {
            System.out.print('\f');
        } else {
            System.out.print(CLEAR_TERMINAL_ANSI_CMD);
        }
    }

    /**
     * @throws SecurityException
     *
     */
    private void printTopBar() {
        String version = "";
        for (String versionPart : JvmTop.class.getAnnotation(Command.class).version()) {
            version += versionPart;
        }
        System.out.printf(" JvmTop %s - %8tT, %6s, %2d cpus, %15.15s", version,
                new Date(), localOSBean_.getArch(),
                localOSBean_.getAvailableProcessors(), localOSBean_.getName() + " "
                        + localOSBean_.getVersion());

        if (supportSystemLoadAverage() && localOSBean_.getSystemLoadAverage() != -1) {
            System.out.printf(", load avg %3.2f%n",
                    localOSBean_.getSystemLoadAverage());
        } else {
            System.out.println();
        }
        System.out.println(" https://github.com/nemaefar/jvmtop");
        System.out.println();
    }

    private boolean supportSystemLoadAverage() {
        if (supportsSystemAverage_ == null) {
            try {
                supportsSystemAverage_ = (localOSBean_.getClass().getMethod(
                        "getSystemLoadAverage") != null);
            } catch (Throwable e) {
                supportsSystemAverage_ = false;
            }
        }
        return supportsSystemAverage_;
    }
}
