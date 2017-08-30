package com.jvmtop;

import com.jvmtop.cli.CommandLine.Command;
import com.jvmtop.cli.CommandLine.Option;
import com.jvmtop.cli.CommandLine.Parameters;

import java.util.List;

@Command(name = "JvmTop", description = "Java sampling command-line profiler", version = "1.0.2")
public class Config {
    @Option(names = {"-i", "--sysinfo"}, description = "Outputs diagnostic information")
    public boolean sysInfoOption = false;
    @Option(names = {"-v", "--verbose"}, description = "Outputs verbose logs")
    public boolean verbose = false;

    @Parameters(index = "0", arity = "0..1", description = "PID to connect to, override parameter")
    public Integer pidParameter = null;
    @Option(names = {"-p", "--pid"}, description = "PID to connect to")
    public Integer pid = null;

    @Option(names = {"-w", "--width"}, description = "Width in columns for the console display")
    public Integer width = 280;

    @Option(names = {"-d", "--delay"}, description = "Delay between each output iteration")
    public double delay = 1.0;

    @Option(names = "--profile", description = "Start CPU profiling at the specified jvm")
    public boolean profileMode = false;

    @Option(names = {"-n", "--iteration"}, description = "jvmtop will exit after n output iterations")
    public Integer iterations = -1;

    @Option(names = "--threadlimit", description = "sets the number of displayed threads in detail mode")
    public Integer threadlimit = Integer.MAX_VALUE;

    @Option(names = "--disable-threadlimit", description = "displays all threads in detail mode")
    public boolean threadLimitEnabled = true;

    @Option(names = "--threadnamewidth", description = "sets displayed thread name length in detail mode (defaults to 30)")
    public Integer threadNameWidth = null;

    @Option(names = "--profileMinTotal", description = "Profiler minimum thread cost to be in output")
    public Double minTotal = 5.0;
    @Option(names = "--profileMinCost", description = "Profiler minimum function cost to be in output")
    public Double minCost = 5.0;
    @Option(names = "--profileMaxDepth", description = "Profiler maximum function depth in output")
    public Integer maxDepth = 15;
    @Option(names = "--profileCanSkip", description = "Profiler ability to skip intermediate functions with same cpu usage as their parent")
    public boolean canSkip = false;
    @Option(names = "--profilePrintTotal", description = "Profiler printing percent of total thread cpu")
    public boolean printTotal = false;
    @Option(names = "--profileRealTime", description = "Profiler uses real time instead of cpu time (usable for sleeps profiling)")
    public boolean profileRealTime = false;
    @Option(names = "--profileFileVisualize", description = "Profiler file to output result")
    public String fileVisualize;
    @Option(names = "--profileJsonVisualize", description = "Profiler file to output result (JSON format)")
    public String jsonVisualize;
    @Option(names = "--profileCachegrindVisualize", description = "Profiler file to output result (Cachegrind format)")
    public String cachegrindVisualize;
    @Option(names = "--profileFlameVisualize", description = "Profiler file to output result (Flame graph format)")
    public String flameVisualize;
    @Option(names = "--profileThreadIds", description = "Profiler thread ids to profile (id is #123 after thread name)", split = ",", type = Long.class)
    public List<Long> profileThreadIds;
    @Option(names = "--profileThreadNames", description = "Profiler thread names to profile", split = ",")
    public List<String> profileThreadNames;

    public Config() {
    }

    @Option(names = {"-V", "--version"}, versionHelp = true, description = "display version info")
    private boolean versionInfoRequested;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    private boolean usageHelpRequested;
}
