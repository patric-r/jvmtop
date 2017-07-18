package com.jvmtop.profiler;

import java.util.Arrays;
import java.util.List;

class NodeFilter {
    private static List<String> filter = Arrays
            .asList(
                    "org.eclipse.",
                    "org.apache.",
                    "java.",
                    "sun.",
                    "com.sun.",
                    "javax.",
                    "oracle.",
                    "com.trilead.",
                    "org.junit.",
                    "org.mockito.",
                    "org.hibernate.",
                    "com.ibm.",
                    "com.caucho."
            );

    static boolean isReallySleeping(StackTraceElement se) {
        return se.getClassName().equals("sun.nio.ch.EPollArrayWrapper") &&
                se.getMethodName().equals("epollWait");
    }

    static boolean isFiltered(StackTraceElement se) {
        for (String filteredPackage : filter) {
            if (se.getClassName().startsWith(filteredPackage)) {
                return true;
            }
        }
        return false;
    }
}
