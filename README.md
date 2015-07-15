Jvmtop is a lightweight console application to monitor all accessible, running jvms on a machine.<br>
In a top-like manner, it displays <a href='http://code.google.com/p/jvmtop/wiki/ExampleOutput'>JVM internal metrics</a> (e.g. memory information) of running java processes.<br>
<br>
Jvmtop does also include a <a href='http://code.google.com/p/jvmtop/wiki/ConsoleProfiler'>CPU console profiler</a>.<br>
<br>
It's tested with different releases of Oracle JDK, IBM JDK and OpenJDK on Linux, Solaris, FreeBSD and Windows hosts.<br>
Jvmtop requires a JDK - a JRE will not suffice.<br>
<br>
Please note that it's currently in an alpha state -<br>
if you experience an issue or need further help, please <a href='http://code.google.com/p/jvmtop/issues/list'>let us know</a>.<br>
<br>
Jvmtop is open-source. Checkout the <a href='http://code.google.com/p/jvmtop/source/checkout'>source code</a>. Patches are very welcome!<br>
<br>
Also have a look at the <a href='http://code.google.com/p/jvmtop/wiki/Documentation'>documentation</a> or at a <a href='http://code.google.com/p/jvmtop/wiki/ExampleOutput'>captured live-example</a>.<br>
<br>
<pre><code> JvmTop 0.8.0 alpha   amd64  8 cpus, Linux 2.6.32-27, load avg 0.12<br>
 http://code.google.com/p/jvmtop<br>
<br>
  PID MAIN-CLASS      HPCUR HPMAX NHCUR NHMAX    CPU     GC    VM USERNAME   #T DL<br>
 3370 rapperSimpleApp  165m  455m  109m  176m  0.12%  0.00% S6U37 web        21<br>
11272 ver.resin.Resin [ERROR: Could not attach to VM]<br>
27338 WatchdogManager   11m   28m   23m  130m  0.00%  0.00% S6U37 web        31<br>
19187 m.jvmtop.JvmTop   20m 3544m   13m  130m  0.93%  0.47% S6U37 web        20<br>
16733 artup.Bootstrap  159m  455m  166m  304m  0.12%  0.00% S6U37 web        46<br>
</code></pre>

<hr />

<h3>08/14/2013 jvmtop 0.8.0 released</h3>
<b>Changes:</b>
<ul><li>improved attach compatibility for all IBM jvms<br>
</li><li>fixed wrong CPU/GC values for IBM J9 jvms<br>
</li><li>in case of unsupported heap size metric retrieval, n/a will be displayed instead of 0m<br>
</li><li>improved argument parsing, support for short-options, added help (pass <code>--help</code>), see <a href='https://code.google.com/p/jvmtop/issues/detail?id=#28'>issue #28</a> (now using the great <a href='http://pholser.github.io/jopt-simple'>jopt-simple</a> library)<br>
</li><li>when passing the <code>--once</code> option, terminal will not be cleared anymore (see <a href='https://code.google.com/p/jvmtop/issues/detail?id=#27'>issue #27</a>)<br>
</li><li>improved shell script for guessing the path if a <code>JAVA_HOME</code> environment variable is not present (thanks to <a href='https://groups.google.com/forum/#!topic/jvmtop-discuss/KGg_WpL_yAU'>Markus Kolb</a>)</li></ul>

<a href='http://code.google.com/p/jvmtop/wiki/Changelog'>Full changelog</a>

<hr />

In <a href='http://code.google.com/p/jvmtop/wiki/ExampleOutput'>VM detail mode</a> it shows you the top CPU-consuming threads, beside detailed metrics:<br>
<br>
<br>
<pre><code> JvmTop 0.8.0 alpha   amd64,  4 cpus, Linux 2.6.18-34<br>
 http://code.google.com/p/jvmtop<br>
<br>
 PID 3539: org.apache.catalina.startup.Bootstrap<br>
 ARGS: start<br>
 VMARGS: -Djava.util.logging.config.file=/home/webserver/apache-tomcat-5.5[...]<br>
 VM: Sun Microsystems Inc. Java HotSpot(TM) 64-Bit Server VM 1.6.0_25<br>
 UP: 869:33m #THR: 106  #THRPEAK: 143  #THRCREATED: 128020 USER: webserver<br>
 CPU:  4.55% GC:  3.25% HEAP: 137m / 227m NONHEAP:  75m / 304m<br>
<br>
  TID   NAME                                    STATE    CPU  TOTALCPU BLOCKEDBY<br>
     25 http-8080-Processor13                RUNNABLE  4.55%     1.60%<br>
 128022 RMI TCP Connection(18)-10.101.       RUNNABLE  1.82%     0.02%<br>
  36578 http-8080-Processor164               RUNNABLE  0.91%     2.35%<br>
  36453 http-8080-Processor94                RUNNABLE  0.91%     1.52%<br>
     27 http-8080-Processor15                RUNNABLE  0.91%     1.81%<br>
     14 http-8080-Processor2                 RUNNABLE  0.91%     3.17%<br>
 128026 JMX server connection timeout   TIMED_WAITING  0.00%     0.00%<br>
<br>
</code></pre>

<a href='http://code.google.com/p/jvmtop/issues/list'>Feature requests / bug reports</a> are always welcome.<br>
<br>
