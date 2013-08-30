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
package com.jvmtop.view;

import java.util.Iterator;

import com.jvmtop.monitor.VMInfo;
import com.jvmtop.monitor.VMInfoState;
import com.jvmtop.openjdk.tools.LocalVirtualMachine;
import com.jvmtop.profiler.CPUSampler;
import com.jvmtop.profiler.MethodStats;

/**
 * CPU sampling-based profiler view which shows methods with top CPU usage.
 *
 * @author paru
 *
 */
public class VMProfileView extends AbstractConsoleView
{

  private CPUSampler cpuSampler_;

  private VMInfo     vmInfo_;

  public VMProfileView(int vmid) throws Exception
  {
    LocalVirtualMachine localVirtualMachine = LocalVirtualMachine
        .getLocalVirtualMachine(vmid);
    vmInfo_ = VMInfo.processNewVM(localVirtualMachine, vmid);
    cpuSampler_ = new CPUSampler(vmInfo_);
  }

  @Override
  public void sleep(long millis) throws Exception
  {
    long cur = System.currentTimeMillis();
    cpuSampler_.update();
    while (cur + millis > System.currentTimeMillis())
    {
      cpuSampler_.update();
      super.sleep(100);
    }

  }

  @Override
  public void printView() throws Exception
  {
    if (vmInfo_.getState() == VMInfoState.ATTACHED_UPDATE_ERROR)
    {
      System.out
          .println("ERROR: Could not fetch telemetries - Process terminated?");
      exit();
      return;
    }
    if (vmInfo_.getState() != VMInfoState.ATTACHED)
    {
      System.out.println("ERROR: Could not attach to process.");
      exit();
      return;
    }

    System.out.printf(" Profiling PID %d: %40s %n%n", vmInfo_.getId(),
        leftStr(vmInfo_.getDisplayName(), 40));

    for (Iterator<MethodStats> iterator = cpuSampler_.getTop(20).iterator(); iterator
        .hasNext();)
    {
      MethodStats stats = iterator.next();
      double wallRatio = (double) stats.getHits().get()
          / cpuSampler_.getTotal() * 100;
      if (!Double.isNaN(wallRatio))
      {
        System.out.printf(" %6.2f%% (%9.2fs) %s()%n", wallRatio, wallRatio
            / 100d
            * cpuSampler_.getUpdateCount() * 0.1d,
            shortFQN(stats.getClassName(), stats.getMethodName(), 56));
      }
    }
  }

  /**
   * Shortens a full qualified class name if it exceeds the size.
   * TODO: improve method to shorten middle packages first,
   * maybe abbreviating the package by its first character.
   *
   * @param fqn
   * @param method
   * @param size
   * @return
   */
  private String shortFQN(String fqn, String method, int size)
  {
    String line = fqn + "." + method;
    if (line.length() > size)
    {
      line = "..." + line.substring(3, size);
    }
    return line;
  }

}
