# Kafka Task Manager

Educational project created with the goal of learning Kafka Connect with Debezium.

For now, user can create/update/delete/get Task from TaskManager microservice.
At the same time TaskManager microservice saves CRUD events to a separate table used by Debezium as
a source of messages for Kafka. When a row changes in the events table, a message is sent to the Kafka topic 
events.public.events.

HistoryManager microservice tracks all events by listening to the Kafka topic and saves the events
to its database. 

## TBD
1. Let user retrieve history of task changes.
2. Validate task intersection (only one task at a time is allowed)
3. Add different types of tasks: Epic, SubTask
4. Create a gateway with all endpoints

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
After that you are ready to start microservices. 
