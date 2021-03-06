# test-envoy-ext-authz

## Prepare

Ensure that the following are available:
* minikube
* httpie (or curl)
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

NOTE: the other images referred to in the deployment are already available
in docker.io. No need to build them.

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
# if you're using Minikube and can't use its tunnel:
INGRESS=$(minikube service envoy --url)

# if you're using some other cluster
# INGRESS=$(kubectl get service envoy -o=jsonpath='{.status.loadBalancer.ingress[0].ip}')

http $INGRESS/allow
http $INGRESS/deny

# or if you prefer to use cURL: 
# curl -v $INGRESS/allow
# curl -v $INGRESS/deny

kubectl logs authz

```
