@echo off

javac -cp ".;sqlite-jdbc-3.53.2.0.jar" src\Main.java src\GUI.java

if errorlevel 1 (
    pause
    exit /b
)

start "" javaw -cp ".;sqlite-jdbc-3.53.2.0.jar;src" Main

exit
