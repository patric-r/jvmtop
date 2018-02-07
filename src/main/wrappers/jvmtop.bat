@echo off
set DIR=%~dp0

rem In Case of debug, uncomment JAVA_HOME override
rem set JAVA_HOME="c:\Progra~1\Java\jdk-9.0.1"
rem set JAVA_HOME="c:\Progra~1\Java\jdk1.8.0_131"

rem To switch from Java 9 to Java 8 comment and uncomment necessary sections

rem java 9 version
"%JAVA_HOME%\bin\java" %JAVA_OPTS% --add-modules jdk.management,jdk.management.agent,jdk.attach,jdk.internal.jvmstat,java.rmi,java.management.rmi,java.desktop,java.sql --add-exports=java.rmi/sun.rmi.server=ALL-UNNAMED --add-exports=java.rmi/sun.rmi.transport=ALL-UNNAMED --add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED -cp "%DIR%/jvmtop.jar" com.jvmtop.JvmTop %*

rem legacy java 8 version
rem "%JAVA_HOME%\bin\java" %JAVA_OPTS% -cp "%DIR%/jvmtop.jar;%JAVA_HOME%/lib/tools.jar" com.jvmtop.JvmTop %*