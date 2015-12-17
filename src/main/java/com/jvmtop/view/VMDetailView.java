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

import static com.jvmtop.monitor.VMUtils.currentProcessID;

import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.jvmtop.monitor.ThreadStats;
import com.jvmtop.monitor.VMInfo;
import com.jvmtop.monitor.VMInfoState;

/**
 * "detail" view, printing detail metrics of a specific jvm. Also printing the
 * top threads (based on the current CPU usage)
 *
 * @author paru
 *
 */
public class VMDetailView extends AbstractConsoleView {

	private VMInfo vmInfo_;

	private boolean sortByTotalCPU_ = false;

	private int numberOfDisplayedThreads_ = 10;

	private int threadNameDisplayWidth_ = 30;

	private boolean displayedThreadLimit_ = true;

	private boolean printVMInfo = true;
	
	/**
	 * @return the printVMInfo
	 */
	public boolean isPrintVMInfo() {
		return printVMInfo;
	}

	/**
	 * @param printVMInfo the printVMInfo to set
	 */
	public void setPrintVMInfo(boolean printVMInfo) {
		this.printVMInfo = printVMInfo;
	}


	public VMDetailView(int vmid, Integer width) throws Exception {
		super(width);
		vmInfo_ = VMInfo.processNewVM(vmid);
	}

	/**
	 * @throws Exception 
	 * 
	 */
	public VMDetailView() throws Exception {
		this(currentProcessID(), null);
	}

	public boolean isSortByTotalCPU() {
		return sortByTotalCPU_;
	}

	public void setSortByTotalCPU(boolean sortByTotalCPU) {
		sortByTotalCPU_ = sortByTotalCPU;
	}

	@Override
	public void printView() throws Exception {
		vmInfo_.update();

		if (vmInfo_.getState() == VMInfoState.ATTACHED_UPDATE_ERROR) {
			printStream_.println("ERROR: Could not fetch telemetries - Process terminated?");
			exit();
			return;
		}
		if (vmInfo_.getState() != VMInfoState.ATTACHED) {
			printStream_.println("ERROR: Could not attach to process.");
			exit();
			return;
		}

		if (printVMInfo) {
			printVMInfo();
		}

		printTopThreads();
	}

	private void printVMInfo() {
		Map<String, String> properties = vmInfo_.getSystemProperties();

		String command = properties.get("sun.java.command");
		if (command != null) {
			String[] commandArray = command.split(" ");

			List<String> commandList = Arrays.asList(commandArray);
			commandList = commandList.subList(1, commandList.size());

			printStream_.printf(" PID %d: %s %n", vmInfo_.getId(), commandArray[0]);

			String argJoin = join(commandList, " ");
			if (argJoin.length() > 67)
				printStream_.printf(" ARGS: %s[...]%n", leftStr(argJoin, 67));
			else
				printStream_.printf(" ARGS: %s%n", argJoin);
		} else {
			printStream_.printf(" PID %d: %n", vmInfo_.getId());
			printStream_.printf(" ARGS: [UNKNOWN] %n");
		}

		String join = join(vmInfo_.getRuntimeMXBean().getInputArguments(), " ");
		if (join.length() > 65)
			printStream_.printf(" VMARGS: %s[...]%n", leftStr(join, 65));
		else
			printStream_.printf(" VMARGS: %s%n", join);

		printStream_.printf(" VM: %s %s %s%n", properties.get("java.vendor"), properties.get("java.vm.name"), properties.get("java.version"));
		printStream_.printf(" UP: %-7s #THR: %-4d #THRPEAK: %-4d #THRCREATED: %-4d USER: %-12s%n", toHHMM(vmInfo_.getRuntimeMXBean().getUptime()), vmInfo_.getThreadCount(), vmInfo_.getThreadMXBean()
				.getPeakThreadCount(), vmInfo_.getThreadMXBean().getTotalStartedThreadCount(), vmInfo_.getOSUser());

		printStream_.printf(" GC-Time: %-7s  #GC-Runs: %-8d  #TotalLoadedClasses: %-8d%n", toHHMM(vmInfo_.getGcTime()), vmInfo_.getGcCount(), vmInfo_.getTotalLoadedClassCount());

		printStream_.printf(" CPU: %5.2f%% GC: %5.2f%% HEAP:%5s /%5s NONHEAP:%5s /%5s%n", vmInfo_.getCpuLoad() * 100, vmInfo_.getGcLoad() * 100, toMB(vmInfo_.getHeapUsed()),
				toMB(vmInfo_.getHeapMax()), toMB(vmInfo_.getNonHeapUsed()), toMB(vmInfo_.getNonHeapMax()));

		printStream_.println();
	}

	/**
	 * @throws Exception
	 */
	private void printTopThreads() throws Exception {
		if (!vmInfo_.getThreadMXBean().isThreadCpuTimeSupported()) {
			printStream_.printf("%n -Thread CPU telemetries are not available on the monitored jvm/platform-%n");
			return;
		}
		
		printStream_.printf(" %6s %-" + threadNameDisplayWidth_ + "s  %13s %8s    %8s %5s %n", "TID", "NAME", "STATE", "CPU", "TOTALCPU", "BLOCKEDBY");
	
		List<ThreadStats> sortedThreadStats = new ArrayList<ThreadStats>(vmInfo_.getThreadStats().values());
		Collections.sort(sortedThreadStats);

		boolean someThreadsArentDisplayed = false;
		int displayedThreads = 0;
		long processCPUTime = vmInfo_.getProxyClient().getProcessCpuTime();
		for (ThreadStats threadStats : sortedThreadStats) {
			ThreadInfo info = vmInfo_.getThreadMXBean().getThreadInfo(threadStats.getTid());
			displayedThreads++;
			if (displayedThreads > numberOfDisplayedThreads_ && displayedThreadLimit_) {
				someThreadsArentDisplayed = true;
				break;
			}
			if (info != null)
				printStream_.printf(" %6d %-" + threadNameDisplayWidth_ + "s  %13s %5.2f%%    %5.2f%% %5s %n", 
						threadStats.getTid(), 
						leftStr(info.getThreadName(), threadNameDisplayWidth_), 
						info.getThreadState(),
						getThreadCPUUtilization(threadStats.getDeltaThreadCpuTime(), vmInfo_.getDeltaUptime()),
						getThreadCPUUtilization(threadStats.getTotalThreadCpuTime(), processCPUTime, 1), 
						getBlockedThread(info));
		}
		if (someThreadsArentDisplayed)
			printStream_.printf(" Note: Only top %d threads (according cpu load) are shown!", numberOfDisplayedThreads_);
	}


	private String getBlockedThread(ThreadInfo info) {
		if (info.getLockOwnerId() >= 0) {
			return "" + info.getLockOwnerId();
		}
		return "";
	}

	public int getNumberOfDisplayedThreads() {
		return numberOfDisplayedThreads_;
	}

	public void setNumberOfDisplayedThreads(int numberOfDisplayedThreads) {
		numberOfDisplayedThreads_ = numberOfDisplayedThreads;
	}

	public boolean isDisplayedThreadLimit() {
		return displayedThreadLimit_;
	}

	public void setDisplayedThreadLimit(boolean displayedThreadLimit) {
		displayedThreadLimit_ = displayedThreadLimit;
	}

	public int getThreadNameDisplayWidth() {
		return threadNameDisplayWidth_;
	}

	public void setThreadNameDisplayWidth(int threadNameDisplayWidth_) {
		this.threadNameDisplayWidth_ = threadNameDisplayWidth_;
	}

	private double getThreadCPUUtilization(long deltaThreadCpuTime, long totalTime) {
		return getThreadCPUUtilization(deltaThreadCpuTime, totalTime, 1000 * 1000);
	}

	private double getThreadCPUUtilization(long deltaThreadCpuTime, long totalTime, double factor) {
		if (totalTime == 0)
			return 0;
		return deltaThreadCpuTime / factor / totalTime * 100d;
	}
}
