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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class VMProfileViewTest
{

  @Test
  public void shouldDisplayCurrentProcessID() throws Exception
  {
    VMProfileView view = new VMProfileView();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    view.setPrintStream(new PrintStream(outputStream));
    view.printView();

    assertTrue("Output doesn't contain current PID",
        outputStream.toString().contains("PID " + currentProcessID()));
  }

}
