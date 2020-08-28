#!/bin/sh
# jvmtop - java monitoring for the command-line
# launch script
#
# author: Markus Kolb
#
DIR=$( cd $(dirname $0) ; pwd -P )

if [ -z "$JAVA_HOME" ] ; then
        JAVA_HOME=`readlink -f \`which java 2>/dev/null\` 2>/dev/null | \
        sed 's/\/bin\/java//'`
fi

JMX_OPTS="$JMX_OPTS -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9999"
JMX_OPTS="$JMX_OPTS -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"

#JAVA_DEBUG_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=*:9002"
JAVA_MODULES="--add-modules java.se
                --add-exports=java.base/jdk.internal.ref=ALL-UNNAMED
                --add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED
                --add-exports=java.base/jdk.internal.perf=ALL-UNNAMED
                --add-exports=java.management/sun.management.counter.perf=ALL-UNNAMED
                --add-exports=java.management/sun.management.counter=ALL-UNNAMED
                --add-exports=jdk.management.agent/jdk.internal.agent=ALL-UNNAMED
                --add-opens java.rmi/sun.rmi.server=ALL-UNNAMED
                --add-opens java.rmi/sun.rmi.transport=ALL-UNNAMED
                --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED"

"$JAVA_HOME"/bin/java $JMX_OPTS $JAVA_DEBUG_OPTS $JAVA_MODULES $JAVA_OPTS -cp "$DIR/jvmtop.jar" \
com.jvmtop.JvmTop "$@"
exit $?
