# Introduction #

Starting with version 0.7.0, jvmtop includes a minimalistic, experimental, sampling-based CPU console profiler.
In contrast to many other profilers available, it does not take the use of instrumentation. The classes of the to-be-profiled jvm will not be altered.

You can invoke the profiling view by specifying the `--profile` argument, followed by the PID:

```
jvmtop.sh --profile <PID>
```

# Example output #
```
 JvmTop 0.7.0 alpha - 15:16:34,  amd64,  8 cpus, Linux 2.6.32-27, load avg 0.41
 http://code.google.com/p/jvmtop

 Profiling PID 24015: org.apache.catalina.startup.Bootstrap

  36.16% (    57.57s) hudson.model.AbstractBuild.calcChangeSet()
  30.36% (    48.33s) hudson.scm.SubversionChangeLogParser.parse()
   7.14% (    11.37s) org.kohsuke.stapler.jelly.JellyClassTearOff.parseScript()
   6.25% (     9.95s) net.sf.json.JSONObject.write()
   3.13% (     4.98s) ....kohsuke.stapler.jelly.CustomTagLibrary.loadJellyScri()
   3.13% (     4.98s) net.sf.json.JsonConfig.<init>()
   1.79% (     2.84s) org.tmatesoft.svn.core.internal.wc.SVNConfigFile.doLoad()
   1.79% (     2.84s) ....thoughtworks.xstream.converters.reflection.Sun14Refl()
   1.34% (     2.13s) hudson.ExpressionFactory2.createExpression()
   1.34% (     2.13s) hudson.ExpressionFactory2$JexlExpression.evaluate()
   0.89% (     1.42s) ....kohsuke.stapler.compression.CompressionServletRespon()
   0.89% (     1.42s) org.kohsuke.stapler.jelly.NbspTag.<init>()
   0.89% (     1.42s) org.jvnet.localizer.Localizable.toString()
   0.45% (     0.71s) org.tmatesoft.svn.core.internal.wc.SVNConfigFile.load()
   0.45% (     0.71s) hudson.util.AtomicFileWriter.commit()
   0.45% (     0.71s) ....tmatesoft.svn.core.internal.io.svn.SVNConnection.wri()
   0.45% (     0.71s) ....compass.core.lucene.engine.optimizer.DefaultLuceneSe()
   0.45% (     0.71s) hudson.scm.CompareAgainstBaselineCallable.call()
   0.45% (     0.71s) org.kohsuke.stapler.jelly.ReallyStaticTagLibrary$1.run()
   0.45% (     0.71s) org.kohsuke.stapler.jelly.CallTagLibScript.run()
```

# Important notes #

Due to its design,
  * the to-be-profiled jvm will face an significantly increased CPU-usage till the profiling ends
  * compared to other profilers, the sample-rate is lower, however, for huge performance issues, it should suffice in most-cases