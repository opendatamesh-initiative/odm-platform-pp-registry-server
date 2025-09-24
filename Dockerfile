FROM amazoncorretto:21-alpine-jdk

COPY target/odm-platform-pp-registry-server-*.jar ./application.jar

ENV JAVA_OPTS=""
ENV SPRING_PROPS=""
ENV PROFILES_ACTIVE="docker"
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS   -Dspring.profiles.active=$PROFILES_ACTIVE $SPRING_PROPS -jar ./application.jar" ]
