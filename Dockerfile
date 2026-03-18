#FROM repository.empresa.mg.gov.br/hub/base/java-21:latest
FROM eclipse-temurin:21-jre
ARG JAR_FILE=target/*.jar
WORKDIR /home/nonroot
#USER nonroot
#COPY --chown=nonroot:nonroot ${JAR_FILE} /home/nonroot/app.jar
#verificar se usuario e grupo existe
#id nonroot
#getent passwd nonroot
#getent group nonroot
COPY ${JAR_FILE} /home/nonroot/app.jar
CMD ["java", "-XX:MaxRAMPercentage=75", "-jar", "-Duser.timezone=America/Sao_Paulo", "-Djava.security.egd=file:/dev/./urandom", "/home/nonroot/app.jar"]
EXPOSE 8080
