## Setup

* `minikube delete --all`
* `minikube start`
* `minikube dashboard`

## Generate Kubernetes PODs

* `kompose convert`: Convert `docker-compose.yml` files to k8s
* `kubectl apply -f .`

## Check Services
* `minikube service rabbitmq-fs`: Check RabbitMQ
* `minikube service postgres-fs`: Check Postgres in pgAdmin, change port
* `minikube service neo4j-fs`: Check Postgres in neo4j-Desktop, change port