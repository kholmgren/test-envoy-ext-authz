# test-envoy-ext-authz

## Prepare

Ensure that the following are available:
* minikube
* httpie
* skaffold

Start minikube and a minikube tunnel.

## Build

Run this command:
```bash
./mvnw clean package
```

Example output:
```text
[INFO] Scanning for projects...
[INFO] 
[INFO] -------------------< io.kettil:test-envoy-ext-authz >-------------------
[INFO] Building test-envoy-ext-authz 1.0

...

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  5.978 s
[INFO] Finished at: 2021-03-03T09:03:47-05:00
[INFO] ------------------------------------------------------------------------
```


## Deploy

Run this command:
```bash
skaffold run
```

Example output:
```text
Generating tags...
 - docker.io/kettil/test-envoy-ext-authz -> docker.io/kettil/test-envoy-ext-authz:d434a96-dirty
Checking cache...
 - docker.io/kettil/test-envoy-ext-authz: Not found. Building
Found [minikube] context, using local docker daemon.
Building [docker.io/kettil/test-envoy-ext-authz]...

...

Starting deploy...
 - configmap/manifest-c6992dctb8 created
 - service/authz created
 - service/envoy created
 - service/invoker created
 - pod/authz created
 - pod/envoy created
 - pod/invoker created
Waiting for deployments to stabilize...
Deployments stabilized in 11.442106ms
You can also run [skaffold run --tail] to get the logs
```


## Test

```bash
INGRESS=$(kubectl get service envoy -o=jsonpath='{.status.loadBalancer.ingress[0].ip}')

http $INGRESS/allow
http $INGRESS/deny

kubectl logs authz

```
