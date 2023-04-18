# Kafka Task Manager

## Preparing the environment:
To prepare the environment, go to the **docker** folder and execute:
```bash
docker-compose up -d
```

It brings up the following containers:
1. Kafka (2 brokers)
2. ZooKeeper
3. postgres (2 instances: one for TaskManager microservice, one for HistoryManager microservice)
4. cmak (UI for managing Kafka cluster)
5. Debezium Kafka connector

After the containers are up and running, connect Debezium with Postgres by executing the following 
script from the **connector** directory:
```bash
curl -i -X POST -H "Accept:application/json" \
-H "Content-Type:application/json" \
http://localhost:8083/connectors/ \
-d @debezium-postgres.json
```

Start TaskManager application. 
