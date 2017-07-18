package com.jvmtop.profiler;

import java.io.PrintStream;
import java.util.List;

public class Visualize {
    private static final int PADDING = 4;
    private static final int OUT_FORMAT_LEN = 1 + 5 + 3 + 5 + 5 + 1; // [(99.0% | 88.0% self)]
    private static final int TOTAL_FORMAT_LEN = 1+ 1 + 5 + 1 + 5 + 1; // [ (99.0% total)]
    private static final String BRANCH = " \\_ ";

    public static void print(CalltreeNode node, long parentTotalTime, long threadTotalTime, PrintStream out, int depth, Config config, boolean skipped) {
        if (depth > config.maxDepth) return;
        double percentFull = node.getTotalTime() * 100.0 / parentTotalTime;
        double percentSelf = node.getSelf() * 100.0 / parentTotalTime;
        StringBuilder padding = new StringBuilder();

        if (depth > 0) {
            int n = (depth - 1) * PADDING;
            if (n > 0) padding.append(String.format("%1$" + n + "s", ""));
            padding.append(BRANCH);
        }

        int functionMaxWidth = config.screenMaxWidth - (PADDING * depth + BRANCH.length()) - OUT_FORMAT_LEN; // [     \_ ]
        if (config.printTotal) functionMaxWidth -= TOTAL_FORMAT_LEN;
        if (functionMaxWidth <= 0) return;

        List<CalltreeNode> children = node.getSortedChildren(config.minCost, parentTotalTime);

        boolean skipping = config.canSkip && node.getTotalTime() == parentTotalTime && children.size() == 1 && node.getSelf() == 0 && depth > 0;
        if (skipping) {
            if (!skipped) {
                out.print(padding.toString());
                out.println("[...skipping...]");
            }
        } else {
            out.print(padding.toString());
            out.printf("%." + functionMaxWidth + "s (%.1f%% | %.1f%% self)",
                    node.getName(), percentFull, percentSelf, node.getIntermediate(), node.getSelf());
            if (config.printTotal) {
                double percentTotal = node.getTotalTime() * 100.0 / threadTotalTime;
                out.printf(" (%.1f%% total)\n", percentTotal);
            } else {
                out.println();
            }
        }

        for (CalltreeNode child : children) {
            int nextDepth = skipping && skipped ? depth : depth + 1;
            print(child, node.getTotalTime(), threadTotalTime, out, nextDepth, config, skipping);
        }
    }

    public static class Config {
        public int screenMaxWidth = 280;
        public double minCost = 5.0;
        public double minTotal = 5.0;
        public int maxDepth = 15;
        public boolean canSkip = false;
        public boolean printTotal = false;

        public Config(Integer screenMaxWidth, Double minCost, Double minTotal, Integer maxDepth, boolean canSkip, boolean printTotal) {
            if (screenMaxWidth != null) this.screenMaxWidth = screenMaxWidth;
            if (minCost != null)        this.minCost = minCost;
            if (minTotal != null)       this.minTotal = minTotal;
            if (maxDepth != null)       this.maxDepth = maxDepth;
            this.canSkip = canSkip;
            this.printTotal = printTotal;
        }
    }
}
