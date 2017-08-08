package com.jvmtop.profiler;

import java.io.PrintStream;

public interface Visualizer {
    void print(CalltreeNode node, PrintStream out);
}
