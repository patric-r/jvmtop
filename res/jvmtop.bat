@echo off
set DIR=%~dp0

%JAVA_HOME%\bin\java %JAVA_OPTS% -cp %DIR%/jvmtop.jar;%JAVA_HOME%/lib/tools.jar com.jvmtop.JvmTop %* 