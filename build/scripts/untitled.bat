@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  untitled startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and UNTITLED_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\untitled-1.0-SNAPSHOT.jar;%APP_HOME%\lib\rs2-cache-lib.jar;%APP_HOME%\lib\commons-compress-1.14.jar;%APP_HOME%\lib\joml-1.9.25.jar;%APP_HOME%\lib\pngdecoder-1.0.jar;%APP_HOME%\lib\lwjgl-assimp-3.2.3.jar;%APP_HOME%\lib\lwjgl-assimp-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-bgfx-3.2.3.jar;%APP_HOME%\lib\lwjgl-bgfx-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-cuda-3.2.3.jar;%APP_HOME%\lib\lwjgl-egl-3.2.3.jar;%APP_HOME%\lib\lwjgl-glfw-3.2.3.jar;%APP_HOME%\lib\lwjgl-glfw-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-jawt-3.2.3.jar;%APP_HOME%\lib\lwjgl-jemalloc-3.2.3.jar;%APP_HOME%\lib\lwjgl-jemalloc-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-libdivide-3.2.3.jar;%APP_HOME%\lib\lwjgl-libdivide-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-llvm-3.2.3.jar;%APP_HOME%\lib\lwjgl-llvm-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-lmdb-3.2.3.jar;%APP_HOME%\lib\lwjgl-lmdb-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-lz4-3.2.3.jar;%APP_HOME%\lib\lwjgl-lz4-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-meow-3.2.3.jar;%APP_HOME%\lib\lwjgl-meow-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-nanovg-3.2.3.jar;%APP_HOME%\lib\lwjgl-nanovg-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-nfd-3.2.3.jar;%APP_HOME%\lib\lwjgl-nfd-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-nuklear-3.2.3.jar;%APP_HOME%\lib\lwjgl-nuklear-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-odbc-3.2.3.jar;%APP_HOME%\lib\lwjgl-openal-3.2.3.jar;%APP_HOME%\lib\lwjgl-openal-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-opencl-3.2.3.jar;%APP_HOME%\lib\lwjgl-opengl-3.2.3.jar;%APP_HOME%\lib\lwjgl-opengl-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-opengles-3.2.3.jar;%APP_HOME%\lib\lwjgl-opengles-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-openvr-3.2.3.jar;%APP_HOME%\lib\lwjgl-openvr-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-opus-3.2.3.jar;%APP_HOME%\lib\lwjgl-opus-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-ovr-3.2.3.jar;%APP_HOME%\lib\lwjgl-ovr-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-par-3.2.3.jar;%APP_HOME%\lib\lwjgl-par-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-remotery-3.2.3.jar;%APP_HOME%\lib\lwjgl-remotery-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-rpmalloc-3.2.3.jar;%APP_HOME%\lib\lwjgl-rpmalloc-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-shaderc-3.2.3.jar;%APP_HOME%\lib\lwjgl-shaderc-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-sse-3.2.3.jar;%APP_HOME%\lib\lwjgl-sse-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-stb-3.2.3.jar;%APP_HOME%\lib\lwjgl-stb-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-tinyexr-3.2.3.jar;%APP_HOME%\lib\lwjgl-tinyexr-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-tinyfd-3.2.3.jar;%APP_HOME%\lib\lwjgl-tinyfd-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-tootle-3.2.3.jar;%APP_HOME%\lib\lwjgl-tootle-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-vma-3.2.3.jar;%APP_HOME%\lib\lwjgl-vma-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-vulkan-3.2.3.jar;%APP_HOME%\lib\lwjgl-xxhash-3.2.3.jar;%APP_HOME%\lib\lwjgl-xxhash-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-yoga-3.2.3.jar;%APP_HOME%\lib\lwjgl-yoga-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-zstd-3.2.3.jar;%APP_HOME%\lib\lwjgl-zstd-3.2.3-natives-windows.jar;%APP_HOME%\lib\lwjgl-3.2.3.jar;%APP_HOME%\lib\lwjgl-3.2.3-natives-windows.jar


@rem Execute untitled
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %UNTITLED_OPTS%  -classpath "%CLASSPATH%" main.Main %*

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable UNTITLED_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%UNTITLED_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
