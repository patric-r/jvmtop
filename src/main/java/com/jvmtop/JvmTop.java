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
public class JvmTop {
    private final static String CLEAR_TERMINAL_ANSI_CMD = new String(new byte[]{
            (byte) 0x1b, (byte) 0x5b, (byte) 0x32, (byte) 0x4a, (byte) 0x1b,
            (byte) 0x5b, (byte) 0x48});

    private static Logger logger;

    private Boolean supportsSystemAverage_;

    private java.lang.management.OperatingSystemMXBean localOSBean_;

    private Config config = new Config();

    public JvmTop() {
        localOSBean_ = ManagementFactory.getOperatingSystemMXBean();
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);

        logger = Logger.getLogger("jvmtop");

        JvmTop jvmTop = new JvmTop();
        CommandLine commandLine = new CommandLine(jvmTop.config);
        commandLine.parse(args);

        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.err);
            System.exit(0);
        } else if (commandLine.isVersionHelpRequested()) {
            commandLine.printVersionHelp(System.err);
            System.exit(0);
        }

        jvmTop.start(jvmTop);
    }

    private void start(JvmTop jvmTop) throws Exception {
        if (config.delay < 0.1d) {
            throw new IllegalArgumentException("Delay cannot be set below 0.1");
        }

        if (config.verbose) {
            fineLogging();
            logger.setLevel(Level.ALL);
            logger.fine("Verbosity mode.");
        }

        if (config.pidParameter != null) {
            // support for parameter w/o name
            config.pid = config.pidParameter;
        }

        if (config.sysInfoOption) {
            outputSystemProps();
        } else {
            if (config.pid == null) {
                jvmTop.start(new VMOverviewView(config.width));
            } else {
                if (config.profileMode) {
                    jvmTop.start(new VMProfileView(config.pid, config));
                } else {
                    VMDetailView vmDetailView = new VMDetailView(config.pid, config.width);
                    vmDetailView.setDisplayedThreadLimit(config.threadLimitEnabled);
                    if (config.threadlimit != null) {
                        vmDetailView.setNumberOfDisplayedThreads(config.threadlimit);
                    }
                    if (config.threadNameWidth != null) {
                        vmDetailView.setThreadNameDisplayWidth(config.threadNameWidth);
                    }
                    jvmTop.start(vmDetailView);
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
                    System.err.println("Failed to start last in shutdown");
                    e.printStackTrace();
                }
            }
        }));
    }

    protected void start(final ConsoleView view) throws Exception {
        try {
            System.setOut(new PrintStream(new BufferedOutputStream(
                    new FileOutputStream(FileDescriptor.out)), false));
            int iterations = 0;
            registerShutdown(view);
            while (!view.shouldExit()) {
                if (this.config.iterations > 1 || this.config.iterations == -1) {
                    clearTerminal();
                }
                printTopBar();
                view.printView();
                System.out.flush();
                iterations++;
                if (iterations >= this.config.iterations && this.config.iterations > 0) {
                    break;
                }
                view.sleep((int) (this.config.delay * 1000));
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
        for (String versionPart : Config.class.getAnnotation(Command.class).version()) {
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
