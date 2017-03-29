# Introduction #

Starting with version 1.0, jvmtop includes an experimental, (Heap) memory sampling. You can invoke the profiling view by specifying the `--profile-mem` argument, followed by the PID.


```
jvmtop.sh --profile-mem <PID>
```
[![JvmTop](https://asciinema.org/a/9uu9mm4n3j633m6mgm892jpuy.png)](https://asciinema.org/a/9uu9mm4n3j633m6mgm892jpuy?speed=0.5&preload=1&autoplay=0&theme=solarized-dark)


inorder to view the deltas between the updates, you can use `--enable-deltas` flag in addition. 

```
jvmtop.sh --profile-mem <PID> --enable-deltas
```
[![JvmTop](https://asciinema.org/a/e53qml4g2gccsk5wm1whyi8ow.png)](https://asciinema.org/a/e53qml4g2gccsk5wm1whyi8ow?speed=0.5&preload=1&autoplay=0&theme=solarized-dark)



# Important notes #

Due to its design,
  * the to-be-profiled jvm will face an significantly increased CPU-usage till the profiling ends
  * compared to other profilers, the sample-rate is lower, however, for huge performance issues, it should suffice in most-cases
