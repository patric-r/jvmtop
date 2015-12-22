@echo off
set DIR=%~dp0
set JAVA_HOME="c:\Program Files\Java\jdk1.8.0_25"
cls

%JAVA_HOME%\bin\java %JAVA_OPTS% -cp %DIR%/jvmtop.jar;%JAVA_HOME%/lib/tools.jar com.jvmtop.JvmTop %* 