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

import static org.junit.Assert.*;

import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

public class ThreadStatsTest
{

  @Test
  public void naturalOrderingIsDescendentByDeltaAndTotalCPUTime()
  {
    SortedSet<ThreadStats> sorted = new TreeSet<ThreadStats>();
    sorted.add(newThreadStats(1L, 1L, 10L));
    sorted.add(newThreadStats(2L, 2L, 20L));
    sorted.add(newThreadStats(3L, 2L, 10L));
    sorted.add(newThreadStats(4L, 4L, 20L));

    String result = "";
    for (ThreadStats threadStats : sorted)
    {
      result += threadStats.getTid();
    }

    assertEquals("4231", result);
  }

  private ThreadStats newThreadStats(long id, long delta, long total)
  {
    return new ThreadStats(id).setDeltaCPUTime(delta)
        .setTotalThreadCPUTime(total);
  }

}
