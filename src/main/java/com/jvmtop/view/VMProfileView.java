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

import com.jvmtop.monitor.VMInfo;
import com.jvmtop.monitor.VMInfoState;
import com.jvmtop.openjdk.tools.LocalVirtualMachine;
import com.jvmtop.profiler.CPUSampler;
import com.jvmtop.profiler.CalltreeNode;
import com.jvmtop.profiler.Config;
import com.jvmtop.profiler.Visualize;

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

  private Config config_;

  public VMProfileView(int vmid, Config config) throws Exception
  {
    super(config.screenMaxWidth);
    LocalVirtualMachine localVirtualMachine = LocalVirtualMachine
        .getLocalVirtualMachine(vmid);
    vmInfo_ = VMInfo.processNewVM(localVirtualMachine, vmid);
    cpuSampler_ = new CPUSampler(vmInfo_);
    config_ = config;
  }

  @Override
  public void sleep(long millis) throws Exception
  {
    long cur = System.currentTimeMillis();
    cpuSampler_.update(config_.profileThreadIds);
    while (cur + millis > System.currentTimeMillis())
    {
      cpuSampler_.update(config_.profileThreadIds);
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

    int w = width - 40;
    System.out.printf(" Profiling PID %d: %40s %n%n", vmInfo_.getId(),
        leftStr(vmInfo_.getDisplayName(), w));

    long total = cpuSampler_.getTotal();
    if (total < 1) return;
    for (CalltreeNode node : cpuSampler_.getTop(config_.minTotal, config_.threadsLimit)) {
      Visualize.print(node, node.getTotalTime(), node.getTotalTime(), total, System.out, 0, config_, false);
    }
  }
}
