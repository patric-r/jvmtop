package com.jvmtop.profiler;

import java.io.PrintStream;

public class Visualize {
    private static final int PADDING = 4;
    private static final int OUT_FORMAT_LEN = 1 + 5 + 3 + 5 + 5 + 1; // [(99.0% | 88.0% self)]
    private static final String BRANCH = " \\_ ";

    public static void print(CalltreeNode node, long total, PrintStream out, int depth, Config config) {
        if (depth > config.maxDepth) return;
        double percentFull = node.getTotalTime() * 100.0 / total;
        double percentSelf = node.getSelf() * 100.0 / total;

        if (depth > 0) {
            int n = (depth - 1) * PADDING;
            if (n > 0) out.printf("%1$" + n + "s", "");
            out.print(BRANCH);
        }

        int n = config.screenMaxWidth - (PADDING * depth + BRANCH.length()) - OUT_FORMAT_LEN; // [     \_ ]
        if (n <= 0)
            return;
        else
            out.printf("%." + n + "s (%.1f%% | %.1f%% self)\n", node.getName(), percentFull, percentSelf, node.getIntermediate(), node.getSelf());

        for (CalltreeNode child : node.getSortedChildren(config.minCost, total)) {
            print(child, total, out, depth + 1, config);
        }
    }

    public static class Config {
        public int screenMaxWidth = 280;
        public double minCost = 5.0;
        public double minTotal = 5.0;
        public int maxDepth = 15;

        public Config(Integer screenMaxWidth, Double minCost, Double minTotal, Integer maxDepth) {
            if (screenMaxWidth != null) this.screenMaxWidth = screenMaxWidth;
            if (minCost != null)        this.minCost = minCost;
            if (minTotal != null)       this.minTotal = minTotal;
            if (maxDepth != null)       this.maxDepth = maxDepth;
        }
    }
}
