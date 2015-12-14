/**
 * jvmtop - java monitoring for the command-line
 * 
 * Copyright (C) 2015 by Patric Rufflar. All rights reserved.
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

import java.lang.management.ThreadInfo;

import com.jvmtop.monitor.VMInfo;

/**
 * TODO: document this type!
 *
 * @author francol
 *
 */
public class ExtendedThreadInfo {

	private final ThreadInfo threadInfo_;
	private final long threadCpuTime;
	private VMInfo vmInfo_;

	public ExtendedThreadInfo(ThreadInfo threadInfo, long threadCpuTime, VMInfo vmInfo) {
		this.threadInfo_ = threadInfo;
		this.threadCpuTime = threadCpuTime;
		vmInfo_ = vmInfo;
	}

	public ThreadInfo getThreadInfo() {
		return threadInfo_;
	}
	
	private double getThreadCPUUtilization(long deltaThreadCpuTime, long totalTime) {
		return getThreadCPUUtilization(deltaThreadCpuTime, totalTime, 1000 * 1000);
	}

	private double getThreadCPUUtilization(long deltaThreadCpuTime, long totalTime, double factor) {
		if (totalTime == 0)
			return 0;
		return deltaThreadCpuTime / factor / totalTime * 100d;
	}

	/**
	 * @return
	 */
	public double getThreadCPUUtilization() {
		return getThreadCPUUtilization(threadCpuTime, vmInfo_.getDeltaUptime());
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	public double getTotalThreadCPUUtilization() throws Exception {
		return getThreadCPUUtilization(threadCpuTime, vmInfo_.getProxyClient().getProcessCpuTime(), 1);	
	}
}
