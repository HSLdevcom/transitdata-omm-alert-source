[![Build Status](https://travis-ci.org/HSLdevcom/transitdata-omm-alert-source.svg?branch=master)](https://travis-ci.org/HSLdevcom/transitdata-omm-alert-source) master  
[![Build Status](https://travis-ci.org/HSLdevcom/transitdata-omm-alert-source.svg?branch=develop)](https://travis-ci.org/HSLdevcom/transitdata-omm-alert-source) develop

# Transitdata-omm-alert-source

This project is part of the [Transitdata Pulsar-pipeline](https://github.com/HSLdevcom/transitdata).

## Description

Application for creating Service Alerts from OMM database.

## Building

### Dependencies

This project depends on [transitdata-common](https://github.com/HSLdevcom/transitdata-common) project.

### Locally

- ```mvn compile```  
- ```mvn package```  

### Docker image

- Run [this script](build-image.sh) to build the Docker image


## Running

Requirements:
- Local Pulsar Cluster
  - By default uses localhost, override host in PULSAR_HOST if needed.
    - Tip: f.ex if running inside Docker in OSX set `PULSAR_HOST=host.docker.internal` to connect to the parent machine
  - You can use [this script](https://github.com/HSLdevcom/transitdata/blob/master/bin/pulsar/pulsar-up.sh) to launch it as Docker container
- Connection string to OMM database is read from file.
  - Set filepath via env variable FILEPATH_CONNECTION_STRING, default is `/run/secrets/db_conn_string`

Launch Docker container with

```docker-compose -f compose-config-file.yml up <service-name>```   
