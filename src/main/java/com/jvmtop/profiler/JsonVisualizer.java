package com.jvmtop.profiler;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.List;

public class JsonVisualizer implements Visualizer {
    private final Config config;
    private final long processTotalTime;

    public JsonVisualizer(Config config, long processTotalTime) {
        this.config = config;
        this.processTotalTime = processTotalTime;
    }

    private static String id(int id, String parent) {
        return parent + '_' + id;
    }

    private static StringBuilder json(int id, String parent, String text) {
        return json(id, parent, text, null);
    }

    private static StringBuilder json(int id, String parent, String text, String attr) {
        StringBuilder builder = new StringBuilder();

        builder.append('{')
                .append("\"id\": \"").append(id(id, parent))
                .append("\", \"parent\": \"").append(parent)
                .append("\", \"text\": \"").append(text);

        if (attr != null) {
            builder.append("\", \"li_attr\": \"").append(attr);
        }

        builder.append("\"}");

        return builder;
    }

    @Override
    public void print(CalltreeNode node, PrintStream out) {
        printHeader(out);
        printInternal(node, node.getTotalTime(), node.getTotalTime(), processTotalTime, out, 0, this.config, false, "#", 0);
        printTailer(out);
    }

    private static void printHeader(PrintStream out) {
        out.println("[");
    }

    private static void printTailer(PrintStream out) {
        out.println();
        out.println("]");
    }

    private static void printInternal(CalltreeNode node, long parentTotalTime, long threadTotalTime, long processTotalTime,
                                      PrintStream out, int depth, Config config, boolean skipped, String parent, int idNumber) {
        double percentFull = node.getTotalTime() * 100.0 / parentTotalTime;
        double percentSelf = node.getSelf() * 100.0 / parentTotalTime;

        List<CalltreeNode> children = node.getSortedChildren(config.minCost, threadTotalTime);

        boolean skipping = config.canSkip && node.getTotalTime() == parentTotalTime && children.size() == 1 && node.getSelf() == 0 && depth > 0;

        if (depth > 0 && !skipped) out.println(',');

        if (skipping) {
            if (!skipped) {
                out.println(json(idNumber, parent, "[...skipping...]") + ",");
            }
        } else {
            String text = String.format("%s (%.1f%% | %.1f%% self)", node.getName(), percentFull, percentSelf);
            if (config.printTotal) {
                double percentThread = node.getTotalTime() * 100.0 / threadTotalTime;
                double percentProcess = node.getTotalTime() * 100.0 / processTotalTime;
                text = text + String.format(" (%.1f%% thread | %.1f%% process) %d calls", percentThread, percentProcess, node.getCalls());
            }
            out.print(json(idNumber, parent, text));
        }

        int nextDepth = skipping && skipped ? depth : depth + 1;
        int childNum = 0;
        for (CalltreeNode child : children)
            printInternal(child, node.getTotalTime(), threadTotalTime, processTotalTime, out, nextDepth, config, skipping, id(idNumber, parent), childNum++);
    }
}
