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

import static com.jvmtop.monitor.VMUtils.currentProcessID;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class VMDetailViewTest
{

  @Test
  public void shouldDisplayCurrentThread() throws Exception
  {
    String threadName = "Strange thread name";
    Thread.currentThread().setName(threadName);

    VMDetailView view = new VMDetailView();

    view.setThreadNameDisplayWidth(threadName.length());
    view.setDisplayedThreadLimit(false);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    view.setPrintStream(new PrintStream(outputStream));
    view.printView();

    //		System.out.println(outputStream);

    assertTrue("Output doesn't contain current thread name",
        outputStream.toString().contains(threadName));
  }

  @Test
  public void shouldDisplayCurrentProcessID() throws Exception
  {
    VMDetailView view = new VMDetailView();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    view.setPrintStream(new PrintStream(outputStream));
    view.printView();

    assertTrue("Output doesn't contain current PID",
        outputStream.toString().contains("PID " + currentProcessID()));
  }

  @Test
  public void shouldDisplayThreadsOrderebByCPU() throws Exception
  {
    VMDetailView view = new VMDetailView();

    view.setDisplayedThreadLimit(false);
    view.setPrintVMInfo(false);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    view.setPrintStream(new PrintStream(outputStream));
    view.printView();

    System.out.println(outputStream);

    String[] lines = removeHeader(lines(outputStream));
    for (int i = 0; i < lines.length - 1; i++)
    {
      assertTrue(lines[i] + " is less than " + lines[i + 1],
          cpu(lines[i]) >= cpu(lines[i + 1]));
    }
  }

  private double cpu(String line) throws ParseException
  {
    int indexOfFirstPercentage = line.indexOf("%");
    return NumberFormat.getInstance()
        .parse(
            line.substring(indexOfFirstPercentage - 6, indexOfFirstPercentage)
                .trim())
        .doubleValue();
  }

  private String[] removeHeader(String[] lines)
  {
    List<String> result = new ArrayList<String>();
    for (int i = 0; i < lines.length; i++)
    {
      if (!lines[i].trim().startsWith("TID"))
      {
        result.add(lines[i]);
      }
    }
    return result.toArray(new String[result.size()]);
  }

  private String[] lines(ByteArrayOutputStream outputStream)
  {
    return outputStream.toString().split("\\r?\\n");
  }

}
