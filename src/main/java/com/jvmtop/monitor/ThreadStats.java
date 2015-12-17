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
package com.jvmtop.monitor;

public class ThreadStats implements Comparable<ThreadStats>{

	private Long deltaThreadCpuTime_ = 0L;
	private Long totalThreadCpuTime_ = 0L;
	private final Long tid_;

	public ThreadStats(Long tid) {
		tid_ = tid;
	}

	public ThreadStats setDeltaCPUTime(long deltaThreadCpuTime) {
		deltaThreadCpuTime_ = deltaThreadCpuTime;
		return this;
	}

	public Long getDeltaThreadCpuTime() {
		return deltaThreadCpuTime_;
	}

	public ThreadStats setTotalThreadCPUTime(long threadCpuTime) {
		totalThreadCpuTime_ = threadCpuTime;
		return this;
	}
	
	public Long getThreadCpuTime() {
		return totalThreadCpuTime_;
	}


	public Long getTid() {
		return tid_;
	}

	@Override
	public int compareTo(ThreadStats other) {
	    int i = deltaThreadCpuTime_.compareTo(other.deltaThreadCpuTime_);
	    if (i != 0) return i * -1;

	    return totalThreadCpuTime_.compareTo(other.totalThreadCpuTime_) * -1;
	}

	public String toString() {
		return "" + getTid();
	}

	@Override
	public int hashCode() {
		return getTid().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ThreadStats other = (ThreadStats) obj;
		if (getTid() == null) {
			if (other.getTid() != null)
				return false;
		} else if (!getTid().equals(other.getTid()))
			return false;
		return true;
	}

}
