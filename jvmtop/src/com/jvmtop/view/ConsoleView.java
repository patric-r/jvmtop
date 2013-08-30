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

/**
 *
 * Defines a console view.
 *
 * @author paru
 *
 */
public interface ConsoleView
{
  /**
   * Prints the view to STDOUT.
   *
   * @throws Exception
   */
  public void printView() throws Exception;

  /**
   * Notifies that this view encountered issues
   * and should be called again (e.g. due to exceptions)
   *
   * TODO: remove this method and use proper exception instead.
   *
   * @return
   */
  public boolean shouldExit();

  /**
   * Requests the view to sleep (defined as "not outputting anything").
   * However, the view is allowed to do some work / telemtry retrieval during sleep.
   *
   */
  public void sleep(long millis) throws Exception;
}
