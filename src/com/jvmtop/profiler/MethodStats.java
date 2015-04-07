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

import java.util.concurrent.atomic.AtomicLong;

/**
 * Stores method invocations in a thread-safe manner.
 *
 * @author paru
 *
 */
public class MethodStats implements Comparable<MethodStats>
{
  private AtomicLong hits_       = new AtomicLong(0);

  private String        className_  = null;

  private String        methodName_ = null;

  /**
   * @param className
   * @param methodName
   */
  public MethodStats(String className, String methodName)
  {
    super();
    className_ = className;
    methodName_ = methodName;
  }


  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((className_ == null) ? 0 : className_.hashCode());
    result = prime * result
        + ((methodName_ == null) ? 0 : methodName_.hashCode());
    return result;
  }



  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    MethodStats other = (MethodStats) obj;
    if (className_ == null)
    {
      if (other.className_ != null)
      {
        return false;
      }
    }
    else if (!className_.equals(other.className_))
    {
      return false;
    }
    if (methodName_ == null)
    {
      if (other.methodName_ != null)
      {
        return false;
      }
    }
    else if (!methodName_.equals(other.methodName_))
    {
      return false;
    }
    return true;
  }



  @Override
  /**
   * Compares a MethodStats object by its hits
   */
  public int compareTo(MethodStats o)
  {
    return Long.valueOf(o.hits_.get()).compareTo(hits_.get());
  }

  public AtomicLong getHits()
  {
    return hits_;
  }

  public String getClassName()
  {
    return className_;
  }

  public String getMethodName()
  {
    return methodName_;
  }



}