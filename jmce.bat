@echo off
rem
rem $Id: jmce.bat 619 2011-06-01 08:37:04Z mviara $
rem 
rem JMCE launcher windows version.
rem
rem @Author Mario viara
rem @Version 1.00
rem
if not "%JMCE_ROOT%" == "" goto optset
set JMCE_ROOT=%CD%
PATH %CD%\bin;%path%
:optset
rem java -Xprof -classpath %JMCE_ROOT%/lib/jmce.jar;%JMCE_ROOT%/lib/RXTXcomm.jar jmce.Jmce %1 %2 %3 %4 %5 %6 %7 %8 %9 
java -server -classpath %JMCE_ROOT%/lib/jmce.jar;%JMCE_ROOT%/lib/RXTXcomm.jar jmce.Jmce %1 %2 %3 %4 %5 %6 %7 %8 %9
