@echo off
setlocal

set "thisdir=%~dp0"
set "langfile=%thisdir%\..\app_pojavlauncher\src\main\assets\language_list.txt"

if exist "%langfile%" del /q "%langfile%"

dir /s /b "%thisdir%\..\app_pojavlauncher\src\main\res\values-*" > "%langfile%"

endlocal
