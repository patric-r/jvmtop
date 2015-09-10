#!/bin/sh
# jvmtop - java monitoring for the command-line 
# launch script
#
# author: Markus Kolb
# 
SCRIPT=$(greadlink --canonicalize "${0}" 2>&1 || readlink --canonicalize "${0}" 2>&1 || readlink "${0}" 2>&1 || echo "${0}")
DIR="$(cd "$(dirname "${SCRIPT}")" && pwd)"

if [ -z "$JAVA_HOME" ] ; then
        JAVA_HOME=`readlink -f \`which java 2>/dev/null\` 2>/dev/null | \
        sed 's/\/bin\/java//'`
fi

TOOLSJAR="$JAVA_HOME/lib/tools.jar"

if [ ! -f "$TOOLSJAR" ] ; then
        echo "$JAVA_HOME seems to be no JDK!" >&2
        exit 1
fi

"$JAVA_HOME"/bin/java $JAVA_OPTS -cp "$DIR/jvmtop.jar:$TOOLSJAR" \
com.jvmtop.JvmTop "$@"
exit $?
