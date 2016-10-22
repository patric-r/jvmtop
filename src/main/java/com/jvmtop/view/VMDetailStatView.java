/**
 * jvmtop - java monitoring for the command-line
 *
 * Copyright (C) 2013 by Patric Rufflar. All rights reserved. DO NOT ALTER OR
 * REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 only, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.jvmtop.view;


import com.jvmtop.monitor.VMInfo;
import com.jvmtop.monitor.VMInfoState;
import com.jvmtop.openjdk.tools.LocalVirtualMachine;

/**
 * "detail" view, printing detail metrics of a specific jvm in a vmstat manner.
 *
 *
 * @author
 *
 */
public class VMDetailStatView extends AbstractConsoleView {

    private VMInfo vmInfo_;

    public VMDetailStatView(int vmid, Integer width) throws Exception {
        super(width);
        LocalVirtualMachine localVirtualMachine = LocalVirtualMachine
                .getLocalVirtualMachine(vmid);
        vmInfo_ = VMInfo.processNewVM(localVirtualMachine, vmid);
    }

    @Override
    public void printView() throws Exception {
        vmInfo_.update();

        if (vmInfo_.getState() == VMInfoState.ATTACHED_UPDATE_ERROR) {
            System.out
                    .println("ERROR: Could not fetch telemetries - Process terminated?");
            exit();
            return;
        }
        if (vmInfo_.getState() != VMInfoState.ATTACHED) {
            System.out.println("ERROR: Could not attach to process.");
            exit();
            return;
        }
        printVM(vmInfo_);

    }

    private void printVM(VMInfo vmInfo) throws Exception {

        String deadlockState = "";
        if (vmInfo.hasDeadlockThreads()) {
            deadlockState = "!D";
        }

        System.out
                .printf(
                        "%5d %5s %5.2f%% %5.2f%% %4d %2.2s%n",
                        vmInfo.getId(),
                        toMB(vmInfo.getHeapUsed()),
                        vmInfo.getCpuLoad() * 100,
                        vmInfo.getGcLoad() * 100,
                        vmInfo.getThreadCount(),
                        deadlockState);

    }

    @Override
    public boolean isTopBarRequired() {
        return false;
    }

    @Override
    public boolean isClearingRequired() {
        return false;
    }
}
