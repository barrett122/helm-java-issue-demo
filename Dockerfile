FROM amazoncorretto:17

RUN mkdir /test /test/k8s
WORKDIR /test

COPY target/scala-2.12/helm-java-issue-demo-assembly-0.1.0.jar test.jar
COPY target/example-chart-0.1.0.tgz example-chart.tgz

ENV KUBECONFIG="/test/k8s/kubeconfig"

ENTRYPOINT ["java", "-jar", "test.jar"]