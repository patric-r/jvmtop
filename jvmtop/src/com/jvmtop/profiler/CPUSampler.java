/**
 * jvmtop - java monitoring for the command-line
 *
 * Copyright (C) 2013 by Patric Rufflar. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.jvmtop.profiler;

import java.lang.Thread.State;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.jvmtop.monitor.VMInfo;

/**
 * Experimental and very basic sampling-based CPU-Profiler.
 *
 * It uses package excludes to filter common 3rd party libraries which often
 * distort application problems.
 *
 * @author paru
 *
 */
public class CPUSampler
{
  private ThreadMXBean                          threadMxBean_ = null;

  private ConcurrentMap<String, MethodStats> data_         = new ConcurrentHashMap<String, MethodStats>();

  private long                               beginCPUTime_ = 0;

  private AtomicLong                         totalThreadCPUTime_ = new AtomicLong(
                                                                     0);


  //TODO: these exception list should be expanded to the most common 3rd-party library packages
  private List<String>                       filter        = Arrays
                                                               .asList(new String[] {
      "org.eclipse.", "org.apache.", "java.", "sun.", "com.sun.", "javax.",
      "oracle.", "com.trilead.", "org.junit.", "org.mockito.",
      "org.hibernate.", "com.ibm.", "com.caucho."

                                                                     });

  private ConcurrentMap<Long, Long>          threadCPUTime = new ConcurrentHashMap<Long, Long>();

  private AtomicLong                         updateCount_       = new AtomicLong(
                                                                     0);

  private VMInfo                             vmInfo_;

  /**
   * @param threadMxBean
   * @throws Exception
   */
  public CPUSampler(VMInfo vmInfo) throws Exception
  {
    super();
    threadMxBean_ = vmInfo.getThreadMXBean();
    beginCPUTime_ = vmInfo.getProxyClient().getProcessCpuTime();
    vmInfo_ = vmInfo;
  }

  public List<MethodStats> getTop(int limit)
  {
    ArrayList<MethodStats> statList = new ArrayList<MethodStats>(data_.values());
    Collections.sort(statList);
    return statList.subList(0, Math.min(limit, statList.size()));
  }

  public long getTotal()
  {
    return totalThreadCPUTime_.get();
  }

  public void update() throws Exception
  {
    boolean samplesAcquired = false;
    for (ThreadInfo ti : threadMxBean_.dumpAllThreads(false, false))
    {
      long cpuTime = threadMxBean_.getThreadCpuTime(ti.getThreadId());
      Long tCPUTime = threadCPUTime.get(ti.getThreadId());
      if (tCPUTime == null)
      {
        tCPUTime = 0L;
      }
      else
      {
      Long deltaCpuTime = (cpuTime - tCPUTime);

      if (ti.getStackTrace().length > 0
          && ti.getThreadState() == State.RUNNABLE
)
      {
        for (StackTraceElement stElement : ti.getStackTrace())
        {
          if (isFiltered(stElement))
          {
            continue;
          }
          String key = stElement.getClassName() + "."
              + stElement.getMethodName();
          data_.putIfAbsent(key, new MethodStats(stElement.getClassName(),
              stElement.getMethodName()));
          data_.get(key).getHits().addAndGet(deltaCpuTime);
          totalThreadCPUTime_.addAndGet(deltaCpuTime);
            samplesAcquired = true;
          break;
        }
      }
      }
      threadCPUTime.put(ti.getThreadId(), cpuTime);
    }
    if (samplesAcquired)
{
  updateCount_.incrementAndGet();
}
  }

  public Long getUpdateCount()
  {
    return updateCount_.get();
  }

  public boolean isFiltered(StackTraceElement se)
  {
    for (String filteredPackage : filter)
    {
      if (se.getClassName().startsWith(filteredPackage))
      {
        return true;
      }
    }
    return false;
  }
}

