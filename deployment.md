# Deployment
## Locally
### Prerequisites
```bash

% docker run -d --name zookeeper -e ZOOKEEPER_CLIENT_PORT=2181 -p 2181:2181 -p 2888:2888 -p 3888:3888 confluentinc/cp-zookeeper:latest
% docker run -d --name kafka -e KAFKA_ZOOKEEPER_CONNECT=127.0.0.1:2181 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://127.0.0.1:9092 -p 9092:9092 confluentinc/cp-kafka:latest
```

### DevSpace (k8s)
## Deploy to k8s (DevSpace)
```bash
% npm install -g devspace
% brew install kubectl
% devspace login
% devspace connect cluster
% devspace init --dockerfile viewer-app/target/docker/stage/Dockerfile
% devspace create space lumina
% sbt docker:publish
% devspace deploy --skip-build
% devspace open --var="play.http.secret.key=*secret*"
```

### Add application secret to cloud environment
```bash
APPLICATION_SECRET=`sbt playGenerateSecret`
```
E.g., locally:
```bash
% docker run -p 9000:9000 -e play.http.secret.key=$APPLICATION_SECRET dscr.io/sothach/ipcress:latest
```

### App monitoring
```bash
docker run \
  -d \
  -p 3000:3000 \
  --name=grafana \
  -e "GF_SERVER_ROOT_URL=http://grafana.server.name" \
  -e "GF_SECURITY_ADMIN_PASSWORD=secret" \
  grafana/grafana
```

### Listen to events
```bash
/usr/local/kafka_2.12-2.3.0/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic viewer-event --from-beginning
```
### Clean-down events
kafka-delete.json:
```json
{"partitions": [{"topic": "viewer-event", "partition": 0, "offset": -1}], "version": 1}
```
```bash
/usr/local/kafka_2.12-2.3.0/bin/kafka-delete-records.sh --bootstrap-server localhost:9092 --offset-json-file kafka-delete.json 
```
## Build Docker image
`% mvn install dockerfile:build`

## Run Docker locally
`% sbt playGenerateSecret` 
`% docker run viewer-app -Dplay.server.pidfile.path=/dev/null -Dplay.http.secret.key=QCYtAnfkaZiwrNwnxIlR6CTfG3gf90Latabg5241ABR5W1uDFNIkn`

## Deploy to OpenShift
```bash
sbt -Ddocker.username=$NAMESPACE -Ddocker.registry=$DOCKER_REPO_URL viewer-app/docker:publish
```
