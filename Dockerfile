FROM openjdk:17-alpine

RUN mkdir /app

COPY moneta-0.0.1.jar /app/moneta-0.0.1.jar
WORKDIR /app

EXPOSE 8080
EXPOSE 587

LABEL authors="begjosip"
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "moneta-0.0.1.jar"]