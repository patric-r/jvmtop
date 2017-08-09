package com.jvmtop.profiler;

import java.io.PrintStream;

public interface Visualizer {
    void start(PrintStream out);
    void print(CalltreeNode node, PrintStream out);
    void end(PrintStream out);
}
