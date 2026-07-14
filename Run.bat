@echo off

cd /d "%~dp0"
javac -cp ".;sqlite-jdbc-3.53.2.0.jar" src\Main.java src\GUI.java
if errorlevel 1 (
	echo Compilation failed!
	pause
	exit /b
)

start "" javaw -cp "src;sqlite-jdbc-3.53.2.0.jar" Main

exit