@echo off
echo cleaning previously built files...
rmdir /S /Q bin
mkdir "./bin"

echo.

echo building MarkovBot 98...
javac -d "./bin" -g -cp "./src" src/com/jboby93/markovbot/*.java

echo done!
