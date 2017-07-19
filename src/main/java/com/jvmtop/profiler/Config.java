package com.jvmtop.profiler;

import java.util.ArrayList;
import java.util.List;

public class Config {
    public int screenMaxWidth = 280;
    public double minCost = 5.0;
    public double minTotal = 5.0;
    public int maxDepth = 15;
    public boolean canSkip = false;
    public boolean printTotal = false;
    public int threadsLimit = Integer.MAX_VALUE;
    public List<Integer> profileThreadIds = new ArrayList<Integer>();
    public String fileVisualize;

    public Config(Integer screenMaxWidth, Double minCost, Double minTotal, Integer maxDepth, Integer threadsLimit,
                  boolean canSkip, boolean printTotal, List<Integer> profileThreadIds, String fileVisualize) {
        if (screenMaxWidth != null)   this.screenMaxWidth = screenMaxWidth;
        if (minCost != null)          this.minCost = minCost;
        if (minTotal != null)         this.minTotal = minTotal;
        if (maxDepth != null)         this.maxDepth = maxDepth;
        if (threadsLimit != null)     this.threadsLimit = threadsLimit;
        if (profileThreadIds != null) this.profileThreadIds = profileThreadIds;
        if (fileVisualize != null)    this.fileVisualize = fileVisualize;
        this.canSkip = canSkip;
        this.printTotal = printTotal;
    }
}
