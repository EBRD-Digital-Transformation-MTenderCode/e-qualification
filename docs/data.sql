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

CREATE TABLE IF NOT EXISTS  qualification.qualifications
(
    cpid text,
    ocid text,
    id text,
    json_data text,
    primary key(cpid, ocid, id)
);

CREATE TABLE IF NOT EXISTS  qualification.qualification_states
(
    country text,
    pmd text,
    operation_type text,
    parameter text,
    value text
    primary key(country, pmd, operationType, parameter)
);
