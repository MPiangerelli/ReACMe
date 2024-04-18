@echo off
setlocal enabledelayedexpansion

for %%a in (*.*) do (
	set "filename=%%~na"
	set "extension=%%~xa"
	ren "%%a" "!filename!_tg!extension!"
)