package com.jvmtop.profiler;

import java.io.PrintStream;
import java.util.Collection;

public class FlameVisualizer implements Visualizer {
    @Override
    public void start(PrintStream out) {

    }

    @Override
    public void end(PrintStream out) {

    }
    @Override
    public void print(CalltreeNode node, PrintStream out) {
        printInternal("java;", node, out);
    }

    private static void printInternal(String callstack, CalltreeNode node, PrintStream out) {
        Collection<CalltreeNode> children = node.getChildren();

        for (CalltreeNode child : children) {
            printInternal(callstack + node.getName() + ";", child, out);
        }

        if (node.getSelf() != 0) {
            out.println(callstack + node.getName() + " " + node.getSelf());
        }
    }
}
