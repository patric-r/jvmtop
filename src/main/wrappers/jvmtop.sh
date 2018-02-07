#!/usr/bin/env bash
# jvmtop - java monitoring for the command-line 
# launch script
#
# author: Markus Kolb, Miroslav Hruz
# 
DIR=$(cd $(dirname $0) ; pwd -P)

if [ -z "$JAVA_HOME" ] ; then
    JAVA_HOME=`readlink -f \`which java 2>/dev/null\` 2>/dev/null | sed 's/\/bin\/java//'`
fi

JAVA_VERSION=$($JAVA_HOME/bin/java -version 2>&1 | head -n 1 | cut -d " " -f 3 | cut -d "." -f 1 | cut -d "\"" -f 2)
if [[ $JAVA_VERSION -gt 1 ]]; then
    ## java 9+
    "$JAVA_HOME"/bin/java $JAVA_OPTS -cp "$DIR/jvmtop.jar" \
    	--add-modules jdk.management,jdk.management.agent,jdk.attach,jdk.internal.jvmstat,java.rmi,java.management.rmi,java.desktop,java.sql \
    	--add-exports=java.rmi/sun.rmi.server=ALL-UNNAMED \
    	--add-exports=java.rmi/sun.rmi.transport=ALL-UNNAMED \
    	--add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED \
    	com.jvmtop.JvmTop "$@"
    exit $?
else
    ## legacy java < 9
    TOOLSJAR="$JAVA_HOME/lib/tools.jar"

    if [ ! -f "$TOOLSJAR" ] ; then
	    echo "$JAVA_HOME seems to be no JDK!" >&2
	    exit 1
    fi

    "$JAVA_HOME"/bin/java $JAVA_OPTS -cp "$DIR/jvmtop.jar":"$TOOLSJAR" com.jvmtop.JvmTop "$@"
    exit $?
fi