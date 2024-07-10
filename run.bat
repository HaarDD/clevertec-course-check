cd /d D:\Projects\Java\check

rmdir /s /q build

javac -d build src/main/java/ru/clevertec/check/*.java

java -cp build ./src/main/java/ru/clevertec/check/CheckRunner.java 3-1 2-5 5-1 discountCard=1111 balanceDebitCard=100

pause
