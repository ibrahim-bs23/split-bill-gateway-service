FROM openjdk:17-jdk-slim

COPY service-jars/gateway-service.jar gateway-service.jar

RUN useradd -r -u 10001 appuser
RUN mkdir -p ./logs

RUN chown appuser:appuser gateway-service.jar
RUN chown appuser:appuser ./logs

USER appuser
ENTRYPOINT ["java", "-Duser.timezone=Asia/Dhaka", "-jar", "gateway-service.jar"]