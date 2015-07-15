### 08/14/2013 jvmtop 0.8.0 released ###
**Changes:**
  * improved attach compatibility for all IBM jvms
  * fixed wrong CPU/GC values for IBM J9 jvms
  * in case of unsupported heap size metric retrieval, n/a will be displayed instead of 0m
  * improved argument parsing, support for short-options, added help (pass `--help`), see [issue #28](https://code.google.com/p/jvmtop/issues/detail?id=#28) (now using the great [jopt-simple](http://pholser.github.io/jopt-simple) library)
  * when passing the `--once` option, terminal will not be cleared anymore (see [issue #27](https://code.google.com/p/jvmtop/issues/detail?id=#27))
  * improved shell script for guessing the path if a `JAVA_HOME` environment variable is not present (thanks to [Markus Kolb](https://groups.google.com/forum/#!topic/jvmtop-discuss/KGg_WpL_yAU))

### 07/30/2013 jvmtop 0.7.1 released ###
**Changes:**
  * scrolling issue in some terminal environments should now be finally fixed, see [issue #21](https://code.google.com/p/jvmtop/issues/detail?id=#21) and [issue #25](https://code.google.com/p/jvmtop/issues/detail?id=#25)
  * new argument: `--once` will terminate jvmtop right after first output iteration

### 07/19/2013 jvmtop 0.7.0 released ###
**Changes:**
  * New Feature: jvmtop does now include a sampling-based [CPU Profiler](http://code.google.com/p/jvmtop/wiki/ConsoleProfiler)
  * jvmtop does now support FreeBSD, fixing the [issue #21](https://code.google.com/p/jvmtop/issues/detail?id=#21) and [issue #22](https://code.google.com/p/jvmtop/issues/detail?id=#22)
  * refresh rate can now be configured, using argument `--delay <interval>`
  * minor fixes

### 06/05/2013 jvmtop 0.6.0 released ###
**Changes:**
  * Improved stability
  * exceptions which are permission/privilege related are not logged at the INFO level anymore
  * access denied/connection refused problems are now printed accordingly
  * fixed some minor issues

### 05/29/2013 jvmtop 0.5.1 released ###
**Changes:**
  * OpenJDK support
  * Improved robustness against jvm termination or telemetry update errors

### 05/24/2013 jvmtop 0.5.0 released ###
**User-visible changes:**
  * Support for monitoring jvms of all users when running under root (at least for Oracle JDKs)
  * Added new detail-mode metric: Total garbage collection time
  * Added new detail-mode metric: Garbage collections count
  * Added new detail-mode metric: Total loaded classes
  * Increased attach compatibility

### 04/05/2013 jvmtop 0.4.1 released ###
**User-visible changes:**
  * Improved stability in some situations where a jvm could not be attached

### 04/05/2013 jvmtop 0.4 released ###
**User-visible changes:**
  * Improved IBM JDK support (J9 is now supported)
  * Some bug fixes

### 03/26/2013 jvmtop 0.3 released ###
**User-visible changes:**
  * New feature: new vm detail mode (invoke with `jvmtop.sh <pid>`) to show **top threads** and more vm details (see below for an example)
  * Basic Windows support