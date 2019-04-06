#
# $Id: jmce.sh 622 2011-06-01 10:01:23Z mviara $
#
# JMCE Linux / Windows startup script.
#
# @author Mario Viara
# version 1.00
#
if test "$JMCE_ROOT" = "" 
then
	JMCE_ROOT=`pwd`
fi
PATH=$JMCE_ROOT/bin:$PATH
LD_LIBRARY_PATH=$JMCE_ROOT/bin:$LD_LIBRARY_PATH
export LD_LIBRARY_PATH PATH
java -server -classpath $JMCE_ROOT/lib/jmce.jar:$JMCE_ROOT/lib/RXTXcomm.jar jmce.Jmce $*

