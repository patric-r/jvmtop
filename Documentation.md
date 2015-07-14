# Documentation #

## Introduction ##

**jvmtop** is a pure java console utility which is able to monitor all accessible jvms running on the same machine at once and to display jvm metrics which standard operating system tools cannot provide.

It can be seen as an alternative for the jconsole but because of its **console nature** it can be easilier used in specific environments and has **less requirements** (e.g. X11). **Another advantage** is that jvmtop shows you the most important, live metrics of **all running jvms at once** - in a top-like manner.


## Compatibility ##

jvmtop has been tested using the Oracle JDK, IBM JDK (J9) and OpenJDK under Linux, Solaris and Windows. At least version 6 of the JDK is required.


## Installation ##

To install jvmtop, just extract the downloaded the gzipped tarball in a directory of your choice.

Ensure that the environment variable `JAVA_HOME` is set to a path of a JDK. **A JRE is not sufficient.**

Start jvmtop with the execution of `jvmtop.sh`


## Views ##

Currently, jvmtop has two views: The **overview mode** and the **detail mode**.

**The overview mode is the top-like view which shows all jvms at once** The detail view provides much more details about a specific jvm and provides a top-like view for all threads.

More views and other enhancements are planned. Ideas and suggestions are always welcome (add an entry in the issue tracker).


### VM overview mode ###

Command-line: `jvmtop.sh`

```
 JvmTop 0.4.1 alpha   amd64  8 cpus, Linux 2.6.32-27, load avg 0.12
 http://code.google.com/p/jvmtop

  PID MAIN-CLASS      HPCUR HPMAX NHCUR NHMAX    CPU     GC    VM USERNAME   #T DL
 3370 rapperSimpleApp  165m  455m  109m  176m  0.12%  0.00% S6U37 web        21
11272 ver.resin.Resin [ERROR: Could not attach to VM]
27338 WatchdogManager   11m   28m   23m  130m  0.00%  0.00% S6U37 web        31
19187 m.jvmtop.JvmTop   20m 3544m   13m  130m  0.93%  0.47% S6U37 web        20
16733 artup.Bootstrap  159m  455m  166m  304m  0.12%  0.00% S6U37 web        46
```


Columns are:
```
PID = process id
MAIN-CLASS = the "jvm name" but often the entry point class (with used main() method)
HPCUR = currently used heap memory
HPMAX = maximum heap memory the jvm can allocate
NHCUR = currently used non-heap memory (e.g. PermGen)
NHMAX = maximum non-heap memory the jvm can allocate
CPU = CPU utilization
GC = percentage of time spent in garbage collection (~100% means that the process does garbage collection only)
VM = Shows JVM vendor, java version and release number (S6U37 = Sun JVM 6, Update 37)
USERNAME = Username which owns this jvm process
#T = Number of jvm threads
DL = If !D is shown if the jvm detected a thread deadlock
```


### Detail mode (Single-VM monitoring) ###

Command-line:  `jvmtop.sh <pid>`


```
 JvmTop 0.4.1 alpha   amd64,  4 cpus, Linux 2.6.18-34
 http://code.google.com/p/jvmtop

 PID 3539: org.apache.catalina.startup.Bootstrap
 ARGS: start
 VMARGS: -Djava.util.logging.config.file=/home/webserver/apache-tomcat-5.5[...]
 VM: Sun Microsystems Inc. Java HotSpot(TM) 64-Bit Server VM 1.6.0_25
 UP: 869:33m #THR: 106  #THRPEAK: 143  #THRCREATED: 128020 USER: webserver
 CPU:  4.55% GC:  3.25% HEAP: 137m / 227m NONHEAP:  75m / 304m
 Note: Only top 10 threads (according cpu load) are shown!

  TID   NAME                                    STATE    CPU  TOTALCPU BLOCKEDBY
     25 http-8080-Processor13                RUNNABLE  4.55%     1.60%
 128022 RMI TCP Connection(18)-10.101.       RUNNABLE  1.82%     0.02%
  36578 http-8080-Processor164               RUNNABLE  0.91%     2.35%
  36453 http-8080-Processor94                RUNNABLE  0.91%     1.52%
     27 http-8080-Processor15                RUNNABLE  0.91%     1.81%
     14 http-8080-Processor2                 RUNNABLE  0.91%     3.17%
 128026 JMX server connection timeout   TIMED_WAITING  0.00%     0.00%
 128025 JMX server connection timeout   TIMED_WAITING  0.00%     0.00%
 128024 JMX server connection timeout   TIMED_WAITING  0.00%     0.00%
 128023 JMX server connection timeout   TIMED_WAITING  0.00%     0.00%
```


Columns are:
```
TID = thread id
NAME = thread name
STATE = current thread state
CPU = current CPU utilization (in ratio to available cpu time on all processors)
TOTALCPU = CPU utilization (in ratio to process cpu consumption) since the thread is alive
BLOCKEDBY = the thread id which blocks this thread
```