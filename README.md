# Example of Helm-java + async-profiler bug

This repo is a minimal way to reproduce the helm-java + async-profiler issue which causes segmentation faults and other native errors through JNI.

## Prerequisites
- Docker
- Minikube or another kubernetes environment
- Java & sbt
- Helm

## Steps to Run
Note: If you are not making any code/chart changes you can skip steps 1-3 and use a pre-built image [here](https://hub.docker.com/r/barrett122/helm-java-issue-demo)

1. Package the provided example helm chart so we have something to deploy
    * `helm package example-chart -d target`
2. Build the uber-jar
    * `sbt assembly`
3. Build the docker image
    * `docker build . -t helm-java-issue-demo:0.1.0`
4. Configure our K8s cluster
    * We need a `kubeconfig` file to mount into our container to allow communication with k8s
    * For minikube, we can generate this by:
      * `export KUBECONFIG=$(pwd)/kubeconfig`
      * `minikube start`
      * The generated config will use the hostname `localhost`, for mac & windows this will need to be updated
        * Replace `server: https://127.0.0.1:<port_number>` with `server: https://host.docker.internal:<port_number>` in the `kubeconfig` file
        * Below the `server: ...` add `insecure-skip-tls-verify: true` to skip hostname validation (as the hostname no longer matches the generated certificates)
      * The paths to the various certificates should also be updated in the kubeconfig:
        * `client-certificate: /test/k8s/profile/client.crt`
        * `client-key: /test/k8s/profile/client.key`
      * And the `certificate-authority` field should be removed altogether
    * For other k8s configurations you'll need to provide your own `kubeconfig` file
5. Run the test:
```bash
docker run \
  --rm \
  -v "$HOME/.minikube/profiles/minikube:/test/k8s/profile:ro" \
  -v "$HOME/.minikube/ca.crt:/test/k8s/ca.crt:ro" \
  --add-host=host.docker.internal:host-gateway \
  -v "$(pwd)/kubeconfig:/test/k8s/kubeconfig:ro" \
  helm-java-issue-demo:0.1.0 <true/false>
```
6. Obeserve the result, if true is passed in to your docker run command the container should fail, likely with some segmentation violation or similar. If false is passed in, the test should complete eventually
    * Number of releases created and threads used can be tweaked in `Application.scala` if desired.