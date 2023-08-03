FROM harbor-repo.vmware.com/dockerhub-proxy-cache/library/maven:3.6.2-jdk-11-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
COPY settings.xml /usr/share/maven/ref/
RUN mvn -f /home/app/pom.xml -s /usr/share/maven/ref/settings.xml clean package
 
FROM harbor-repo.vmware.com/dockerhub-proxy-cache/library/openjdk:11-jre-slim
COPY --from=build /home/app/target/chs-zapigateway-0.0.1-SNAPSHOT.jar /usr/local/lib/chs-zapigateway.jar
WORKDIR /usr/local/lib
EXPOSE 9210
ENTRYPOINT ["java", "-jar", "chs-zapigateway.jar", "--spring.config.additional-location=override.properties,common.properties,env.properties,dr.properties,passwords.properties"]
