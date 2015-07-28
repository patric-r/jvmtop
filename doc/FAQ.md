# Frequenty asked questions #

Before issueing a bug report, please check below if your question has already been answered here.

## Q: jvmtop can't monitor JRE's with version 1.5 (Java 5) or below ##

Unfortunately, jvmtop does currently not support attaching to jvms with version <= 1.5.

Due to the fact that 1.5 jvms are rarely used only (EOL has been reached since years!) it's unlikely that this will ever be implemented.
However, any help, hints or patches are welcome.

## Q: I'm getting `[ERROR: Connection refused/access denied]` for some/all to-be-monitored jvms ##

jvmtop uses official `javax.management` APIs to gather its telemetries, hence ensuring compatiblity.
However, due to several reasons (often security-related reasons), it might be possible that jvmtop might not be able to connect to some jvms.

Before raising a bug report (which is always welcome), please check the following things:

  * Does your target process run under the same user as jvmtop?
    In many JREs, this is a strict requirement due to security reasons.
    root is also not able to monitor java processes other than the ones owned by itself in most environments.

  * Does your jvmtop process run with the same JDK/JRE (same version/binary) as the process you're trying to monitor?
 
  * Are you able to connect to the process with the JDK tool jconsole?
    If you can't, then probably jvmtop can't do anything here because both tools uses the same attach mechanism.


If you answered all bullets with "yes", please file a bug report.
Provide details of your operating system, the JDK jvmtop is running and the to-be-monitored processes is running.

If you answered at least one bullet with "no", some security policy kicked in, and there's nothing jvmtop can do about this here - but maybe you can try relaxing the security policies.
