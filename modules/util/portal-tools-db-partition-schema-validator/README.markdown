# DB Partition Schema Validator Tool

This tool validates a partition in a database partitioned environment to ensure
that they only contain data associated with their proper company ID.

## Requirements:

- MySQL or PostgreSQL
- Database user with DDL privileges

## Usage

```
java -jar com.liferay.portal.tools.db.partition.schema.validator.jar -d myDatabaseName -p myDabatabasePassword -u myDatabaseUser
```

-a,--debug Print all log traces
-d,--db-name <arg> Database name
-h,--help Print help message
-j,--jdbc-url <arg> JDBC URL
-p,--password <arg> Database user password
-s,--schema-prefix <arg> Schema prefix for non-default partitions
-t,--db-type Database type [mysql or postgresql]
-u,--user <arg> Database user name