REM chcp 65001 就是换成UTF-8代码页
REM chcp 936 可以换回默认的GBK
REM chcp 437 是美国英语  
chcp 65001
java -jar -Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true -Dspring.profiles.active=dev lib/ShadowSocks-Share-0.0.1-SNAPSHOT.jar --spring.config.location=config/application-dev.yml