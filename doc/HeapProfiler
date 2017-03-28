# Introduction #

Starting with version 1.0, jvmtop includes an experimental, (Heap) memory sampling. You can invoke the profiling view by specifying the `--profile-mem` argument, followed by the PID:

```
jvmtop.sh --profile-mem <PID>
```

# Example output #
<script type="text/javascript" src="https://asciinema.org/a/14.js" id="9uu9mm4n3j633m6mgm892jpuy" async></script>


# Important notes #

Due to its design,
  * the to-be-profiled jvm will face an significantly increased CPU-usage till the profiling ends
  * compared to other profilers, the sample-rate is lower, however, for huge performance issues, it should suffice in most-cases
