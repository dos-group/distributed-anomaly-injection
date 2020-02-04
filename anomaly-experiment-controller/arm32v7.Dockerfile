# teambitflow/anomaly-experiment-controller:arm32v7
FROM arm32v7/openjdk:11-jre-slim
WORKDIR /
ADD target/anomaly-experiment-controller-*-jar-with-dependencies.jar anomaly-experiment-controller.jar
ENTRYPOINT ["java", "-jar", "anomaly-experiment-controller.jar"]