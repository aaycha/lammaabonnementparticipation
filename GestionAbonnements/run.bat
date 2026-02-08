@echo off
echo Compilation et execution du projet GestionAbonnements...
echo.

REM Compilation
javac -cp "target/classes;%USERPROFILE%\.m2\repository\mysql\mysql-connector-java\8.0.33\mysql-connector-java-8.0.33.jar;%USERPROFILE%\.m2\repository\com\fasterxml\jackson\core\jackson-databind\2.15.2\jackson-databind-2.15.2.jar;%USERPROFILE%\.m2\repository\com\fasterxml\jackson\core\jackson-core\2.15.2\jackson-core-2.15.2.jar;%USERPROFILE%\.m2\repository\com\fasterxml\jackson\core\jackson-annotations\2.15.2\jackson-annotations-2.15.2.jar;%USERPROFILE%\.m2\repository\com\fasterxml\jackson\datatype\jackson-datatype-jsr310\2.15.2\jackson-datatype-jsr310-2.15.2.jar;%USERPROFILE%\.m2\repository\org\slf4j\slf4j-api\2.0.7\slf4j-api-2.0.7.jar;%USERPROFILE%\.m2\repository\org\slf4j\slf4j-simple\2.0.7\slf4j-simple-2.0.7.jar" -d target/classes src/main/java/com/gestion/**/*.java 2>compile_errors.txt

if %ERRORLEVEL% NEQ 0 (
    echo Erreur de compilation. Voir compile_errors.txt
    pause
    exit /b 1
)

echo Compilation reussie!
echo.
echo Execution du programme...
echo.

REM Execution
java -cp "target/classes;%USERPROFILE%\.m2\repository\mysql\mysql-connector-java\8.0.33\mysql-connector-java-8.0.33.jar;%USERPROFILE%\.m2\repository\com\fasterxml\jackson\core\jackson-databind\2.15.2\jackson-databind-2.15.2.jar;%USERPROFILE%\.m2\repository\com\fasterxml\jackson\core\jackson-core\2.15.2\jackson-core-2.15.2.jar;%USERPROFILE%\.m2\repository\com\fasterxml\jackson\core\jackson-annotations\2.15.2\jackson-annotations-2.15.2.jar;%USERPROFILE%\.m2\repository\com\fasterxml\jackson\datatype\jackson-datatype-jsr310\2.15.2\jackson-datatype-jsr310-2.15.2.jar;%USERPROFILE%\.m2\repository\org\slf4j\slf4j-api\2.0.7\slf4j-api-2.0.7.jar;%USERPROFILE%\.m2\repository\org\slf4j\slf4j-simple\2.0.7\slf4j-simple-2.0.7.jar" com.gestion.ConsoleTestMain

pause
