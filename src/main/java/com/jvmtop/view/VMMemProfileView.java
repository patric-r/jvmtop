package com.jvmtop.view;

import com.jvmtop.monitor.VMInfo;
import com.jvmtop.monitor.VMInfoState;
import com.jvmtop.openjdk.tools.LocalVirtualMachine;
import com.jvmtop.profiler.MemorySampler;
import com.jvmtop.profiler.MemorySampler.HeapHistogram;
import com.sun.tools.attach.VirtualMachine;
import sun.tools.attach.HotSpotVirtualMachine;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created on 27/03/17.
 *
 * @author tckb
 */
public class VMMemProfileView extends AbstractConsoleView implements Closeable {
    private final MemorySampler memorySampler_;
    private final VMInfo vmInfo_;
    private final HotSpotVirtualMachine hVm;


    public VMMemProfileView(int vmid, Integer width) throws Exception {
        super(width);
        hVm = (HotSpotVirtualMachine) VirtualMachine.attach(String.valueOf(vmid));
        LocalVirtualMachine localVirtualMachine = LocalVirtualMachine
                .getLocalVirtualMachine(vmid);
        vmInfo_ = VMInfo.processNewVM(localVirtualMachine, vmid);
        memorySampler_ = new MemorySampler(hVm);
    }

    @Override
    public void printView() throws Exception {
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

        int w = width - 40;
        System.out.printf("Memory Profiling PID %d: %40s %n%n", vmInfo_.getId(),
                leftStr(vmInfo_.getDisplayName(), w));
        System.out.printf("HEAP:%5s /%5s NONHEAP:%5s /%5s%n",
                toMB(vmInfo_.getHeapUsed()), toMB(vmInfo_.getHeapMax()),
                toMB(vmInfo_.getNonHeapUsed()), toMB(vmInfo_.getNonHeapMax()));

        System.out.println();


        // these are the spaces taken up by the formatting, the rest is usable
        // for printing out the method name
        w = width - (8 + 4 + 5 + 3 + 12 + 3);

        for (HeapHistogram stats : memorySampler_.getHistogram(10)) {
            System.out.printf("%8s %3s / %5.2f%% ( %12s) %s\n", stats.memory, stats.memorySuffix, (stats.bytes * 1.d * 100 / vmInfo_.getHeapUsed()), stats.count, shortFQN(stats.className, w));
        }

    }


    private String shortFQN(String fqn, int size) {
        String line = fqn;
        if (line.length() > size) {
            line = "..." + line.substring(3, size);
        }
        return line;
    }

    @Override
    public void close() throws IOException {
        hVm.detach();
    }
}




