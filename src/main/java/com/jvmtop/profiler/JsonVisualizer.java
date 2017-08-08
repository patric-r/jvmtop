package com.jvmtop.profiler;

import java.io.PrintStream;
import java.util.List;

public class JsonVisualizer implements Visualizer {
    private final Config config;
    private final long processTotalTime;

    public JsonVisualizer(Config config, long processTotalTime) {
        this.config = config;
        this.processTotalTime = processTotalTime;
    }

    @Override
    public void print(CalltreeNode node, PrintStream out) {
        printInternal(node, node.getTotalTime(), node.getTotalTime(), processTotalTime, out, 0, this.config, false);
    }

    private static void printInternal(CalltreeNode node, long parentTotalTime, long threadTotalTime, long processTotalTime, PrintStream out, int depth, Config config, boolean skipped) {
        double percentFull = node.getTotalTime() * 100.0 / parentTotalTime;
        double percentSelf = node.getSelf() * 100.0 / parentTotalTime;

        List<CalltreeNode> children = node.getSortedChildren(config.minCost, threadTotalTime);

        boolean skipping = config.canSkip && node.getTotalTime() == parentTotalTime && children.size() == 1 && node.getSelf() == 0 && depth > 0;

        if (skipping) {
            if (!skipped) {
//                out.println("[...skipping...]");
            }
        } else {
//            out.printf("%." + functionMaxWidth + "s (%.1f%% | %.1f%% self)",
//                    node.getName(), percentFull, percentSelf, node.getIntermediate(), node.getSelf());
            if (config.printTotal) {
                double percentThread = node.getTotalTime() * 100.0 / threadTotalTime;
                double percentProcess = node.getTotalTime() * 100.0 / processTotalTime;
//                out.printf(" (%.1f%% thread | %.1f%% process)\n", percentThread, percentProcess);
            } else {
//                out.println();
            }
        }

        int nextDepth = skipping && skipped ? depth : depth + 1;
        for (CalltreeNode child : children)
            printInternal(child, node.getTotalTime(), threadTotalTime, processTotalTime, out, nextDepth, config, skipping);
    }
}
