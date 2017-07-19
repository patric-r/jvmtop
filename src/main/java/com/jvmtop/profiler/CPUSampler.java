/**
 * jvmtop - java monitoring for the command-line
 * <p>
 * Copyright (C) 2013 by Patric Rufflar. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * <p>
 * <p>
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.jvmtop.profiler;

import com.jvmtop.monitor.VMInfo;

import java.lang.Thread.State;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Experimental and very basic sampling-based CPU-Profiler.
 *
 * It uses package excludes to filter common 3rd party libraries which often
 * distort application problems.
 *
 * @author paru
 *
 */
public class CPUSampler {
    private ThreadMXBean threadMxBean_ = null;

    private ConcurrentMap<Long, CalltreeNode> data_ = new ConcurrentHashMap<Long, CalltreeNode>();

    private long beginCPUTime_ = 0;

    private AtomicLong totalThreadCPUTime_ = new AtomicLong(
            0);

    private ConcurrentMap<Long, Long> threadCPUPreviousMark = new ConcurrentHashMap<Long, Long>();

    private AtomicLong updateCount_ = new AtomicLong(
            0);

    private VMInfo vmInfo_;

    /**
     * @param vmInfo
     * @throws Exception
     */
    public CPUSampler(VMInfo vmInfo) throws Exception {
        super();
        threadMxBean_ = vmInfo.getThreadMXBean();
        beginCPUTime_ = vmInfo.getProxyClient().getProcessCpuTime();
        vmInfo_ = vmInfo;
    }

    public List<CalltreeNode> getTop(double percentLimit, int limit) {
        List<CalltreeNode> statList = new ArrayList<CalltreeNode>();

        for (Map.Entry<Long, CalltreeNode> entry : data_.entrySet()) {
            Long cpu = entry.getValue().getTotalTime();
            if ((cpu * 100L / (totalThreadCPUTime_.get() + 1)) > percentLimit) {
                statList.add(entry.getValue());
            }
        }
        Collections.sort(statList);

        return statList.subList(0, Math.min(limit, statList.size()));
    }

    public long getTotal() {
        return totalThreadCPUTime_.get();
    }

    public void update(List<Integer> profileThreadIds) throws Exception {
        boolean samplesAcquired = false;
        ThreadInfo[] threads;
        if (profileThreadIds == null || profileThreadIds.size() == 0)
            threads = threadMxBean_.dumpAllThreads(false, false);
        else {
            long[] threadIds = new long[profileThreadIds.size()];
            for (int i = 0; i < profileThreadIds.size(); i++) threadIds[i] = profileThreadIds.get(i);

            threads = threadMxBean_.getThreadInfo(threadIds, false, false);
        }

        for (ThreadInfo ti : threads) {
            long cpuTime = threadMxBean_.getThreadCpuTime(ti.getThreadId());
            Long tCPUTime = threadCPUPreviousMark.get(ti.getThreadId());
            if (tCPUTime != null) {
                Long deltaCpuTime = (cpuTime - tCPUTime);

                if (ti.getStackTrace().length > 0 && ti.getThreadState() == State.RUNNABLE) {
                    data_.putIfAbsent(ti.getThreadId(), new CalltreeNode(null, ti.getThreadName()));
                    CalltreeNode root = data_.get(ti.getThreadId());
                    samplesAcquired = CalltreeNode.stack(ti, deltaCpuTime, root);
                    if (samplesAcquired) totalThreadCPUTime_.addAndGet(deltaCpuTime);
                }
            }
            threadCPUPreviousMark.put(ti.getThreadId(), cpuTime);
        }
        if (samplesAcquired) {
            updateCount_.incrementAndGet();
        }
    }

    public Long getUpdateCount() {
        return updateCount_.get();
    }
}

