CREATE DATABASE tasksdb;

CREATE TABLE if not exists events (
                      id                  BIGSERIAL PRIMARY KEY NOT NULL,
                      task_id             bigint NOT NULL,
                      event_date          timestamp NOT NULL,
                      event_type          varchar(255) NOT NULL,
                      version             integer NOT NULL
);

CREATE TABLE if not exists tasks (
                        id                  BIGSERIAL PRIMARY KEY NOT NULL,
                        task_name           varchar(255) NOT NULL,
                        description         varchar(255) NOT NULL,
                        task_status         varchar(255) NOT NULL,
                        task_type           varchar(255) NOT NULL,
                        duration            bigint NOT NULL,
                        start_time          timestamp NOT NULL,
                        version             integer NOT NULL
);