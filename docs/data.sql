CREATE KEYSPACE IF NOT EXISTS qualification
    WITH replication = {
        'class' : 'SimpleStrategy',
        'replication_factor' : 1
        };

CREATE TABLE IF NOT EXISTS  qualification.period (
    cpid text,
    ocid text,
    start_date timestamp,
    end_date timestamp,
    primary key(cpid, ocid)
);

CREATE TABLE IF NOT EXISTS  qualification.history
(
    command_id text,
    command text,
    command_date timestamp,
    json_data text,
    primary key(command_id, command)
);