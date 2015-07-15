## VM overview mode ##

Command-line: `jvmtop.sh`

<img src='http://jvmtop.googlecode.com/files/jvmtop-overview.gif'>


Columns are:<br>
<pre><code>PID = process id<br>
MAIN-CLASS = the "jvm name" but often the entry point class (with used main() method)<br>
HPCUR = currently used heap memory<br>
HPMAX = maximum heap memory the jvm can allocate<br>
NHCUR = currently used non-heap memory (e.g. PermGen)<br>
NHMAX = maximum non-heap memory the jvm can allocate<br>
CPU = CPU utilization<br>
GC = percentage of time spent in garbage collection (~100% means that the process does garbage collection only)<br>
VM = Shows JVM vendor, java version and release number (S6U37 = Sun JVM 6, Update 37)<br>
USERNAME = Username which owns this jvm process<br>
#T = Number of jvm threads<br>
DL = If !D is shown if the jvm detected a thread deadlock<br>
</code></pre>


<h2>Detail mode (Single-VM monitoring)</h2>

Command-line:  <code>jvmtop.sh &lt;pid&gt;</code>


<pre><code> JvmTop 0.4.1 alpha   amd64,  4 cpus, Linux 2.6.18-34<br>
 http://code.google.com/p/jvmtop<br>
<br>
 PID 3539: org.apache.catalina.startup.Bootstrap<br>
 ARGS: start<br>
 VMARGS: -Djava.util.logging.config.file=/home/webserver/apache-tomcat-5.5[...]<br>
 VM: Sun Microsystems Inc. Java HotSpot(TM) 64-Bit Server VM 1.6.0_25<br>
 UP: 869:33m #THR: 106  #THRPEAK: 143  #THRCREATED: 128020 USER: webserver<br>
 CPU:  4.55% GC:  3.25% HEAP: 137m / 227m NONHEAP:  75m / 304m<br>
 Note: Only top 10 threads (according cpu load) are shown!<br>
<br>
  TID   NAME                                    STATE    CPU  TOTALCPU BLOCKEDBY<br>
     25 http-8080-Processor13                RUNNABLE  4.55%     1.60%<br>
 128022 RMI TCP Connection(18)-10.101.       RUNNABLE  1.82%     0.02%<br>
  36578 http-8080-Processor164               RUNNABLE  0.91%     2.35%<br>
  36453 http-8080-Processor94                RUNNABLE  0.91%     1.52%<br>
     27 http-8080-Processor15                RUNNABLE  0.91%     1.81%<br>
     14 http-8080-Processor2                 RUNNABLE  0.91%     3.17%<br>
 128026 JMX server connection timeout   TIMED_WAITING  0.00%     0.00%<br>
 128025 JMX server connection timeout   TIMED_WAITING  0.00%     0.00%<br>
 128024 JMX server connection timeout   TIMED_WAITING  0.00%     0.00%<br>
 128023 JMX server connection timeout   TIMED_WAITING  0.00%     0.00%<br>
</code></pre>


Columns are:<br>
<pre><code>TID = thread id<br>
NAME = thread name<br>
STATE = current thread state<br>
CPU = current CPU utilization (in ratio to available cpu time on all processors)<br>
TOTALCPU = CPU utilization (in ratio to process cpu consumption) since the thread is alive<br>
BLOCKEDBY = the thread id which blocks this thread<br>
</code></pre>