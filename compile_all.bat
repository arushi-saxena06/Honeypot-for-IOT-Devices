@echo off
if not exist bin\classes mkdir bin\classes

echo Compiling backend sources...
for /R backend\src\main\java %%f in (*.java) do (
  echo Compiling %%f
  javac -cp lib\mysql-connector-j-9.5.0.jar -d bin\classes "%%f"
)

echo Compiling frontend sources...
for /R frontend %%f in (*.java) do (
  echo Compiling %%f
  javac -cp lib\mysql-connector-j-9.5.0.jar;bin\classes -d bin\classes "%%f"
)

exit /b %errorlevel%
