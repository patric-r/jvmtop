package com.jvmtop.profiler;

import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class CalltreeNode implements Comparable<CalltreeNode> {
    private final String name;
    private AtomicLong intermediate = new AtomicLong(0);
    private AtomicLong self = new AtomicLong(0);

    private final ConcurrentMap<String, CalltreeNode> children = new ConcurrentHashMap<String, CalltreeNode>();

    public CalltreeNode(String name) {
        this.name = name;
    }

    private CalltreeNode getNode(StackTraceElement element) {
        String name = element.getClassName() + "." + element.getMethodName();

        children.putIfAbsent(name, new CalltreeNode(name));
        return children.get(name);
    }

    private CalltreeNode addIntermediate(StackTraceElement element, Long time) {
        CalltreeNode node = getNode(element);
        node.intermediate.addAndGet(time);
        return node;
    }

    private CalltreeNode addSelf(StackTraceElement element, Long time) {
        CalltreeNode node = getNode(element);
        node.self.addAndGet(time);
        return node;
    }

    public String getName() {
        return name;
    }

    public long getIntermediate() {
        return intermediate.get();
    }

    public long getSelf() {
        return self.get();
    }

    public long getTotalTime() {
        return intermediate.get() + self.get();
    }

    public List<CalltreeNode> getSortedChildren(double minCost, long totalCost) {
        List<CalltreeNode> result = new ArrayList<CalltreeNode>();
        for (CalltreeNode node : children.values()) {
            if (node.getTotalTime() * 100.0 / totalCost > minCost)
                result.add(node);
        }
        Collections.sort(result);
        return result;
    }

    public static boolean stack(ThreadInfo ti, Long deltaCPUTime, CalltreeNode root) {
        root.intermediate.addAndGet(deltaCPUTime);

        StackTraceElement[] stackTrace = ti.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (NodeFilter.isReallySleeping(stackTraceElement)) return false;
        }
        if (stackTrace.length == 0) return false;

        CalltreeNode current = root;
        for (int i = stackTrace.length - 1; i >= 1; i--) {
            StackTraceElement stackTraceElement = stackTrace[i];

            current = current.addIntermediate(stackTraceElement, deltaCPUTime);
        }
        current.addSelf(stackTrace[0], deltaCPUTime);
        return true;
    }

    @Override
    public int compareTo(CalltreeNode o) {
        return compare(o.getTotalTime(), this.getTotalTime());
    }

    private static int compare(long x, long y) { // copy-paste from Long from 1.7
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CalltreeNode)) return false;

        CalltreeNode o = (CalltreeNode) obj;

        return this.getName().equals(o.getName()) &&
                this.getIntermediate() == o.getIntermediate() &&
                this.getSelf() == o.getSelf();
    }
}
