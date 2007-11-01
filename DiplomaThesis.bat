@echo off

rem config variables ---------------
set VM_ARGS=-ea
set MAINCLASS=util.DiplomStarter
set JAVAEXE=java.exe
set NATIVEPATH=native/win32
rem --------------------------------

echo ---Matheon F1 Software Startup Sequence----
echo parsing libraries and plugins...
set CLASSPATH=./bin
for %%j in (.\lib\*.jar) do call:addJar %%j

set PATH=%PATH%;%NATIVEPATH%;
start "starting..." /B %JAVAEXE% %VM_ARGS% -Djava.library.path=%NATIVEPATH% -classpath %CLASSPATH% %MAINCLASS%

:addJar

set CLASSPATH=%CLASSPATH%;%1
echo %1
