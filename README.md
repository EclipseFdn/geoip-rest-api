# Eclipse Foundation Marketplace Client API

---
**NOTE**

This project was migrated to [Eclipse Gitlab](https://gitlab.eclipse.org/eclipsefdn/it/api/geoip-rest-api) on October 14, 2021.

---

## Summary

TODO!!

## Requirements

1. Installed and configured JDK 1.8+
1. Apache Maven 3.5.3+
1. Running instance of MariaDB (Docker instructions below)
1. GraalVM (for compilation of native-image)

## Configuration

1. To import MaxMind data for usage with this microservice, CSV and binary database versions of MaxMind's GeoLite2 data must be retrieved. This can be done using the script `./bin/maxmind.sh <location>`, with location being the path to where the data MaxMind data should be stored. This will retrieve, extract, and clean up unneeded MaxMind files for use with the microservice. Note that this script uses Unix based commands, and will not work in windows environments unless run through a Unix terminal emulator (like WSL). This requires a license file with a maxmind key to be available at `/run/secrets/license_key`. If this key file is missing, the build script won't function.
- If using SQL:
    1. A small update will need to be done to the sample SQL provided for populating the data set before use. This file can be located at `./bin/prepare.sql`. Update the paths to the local files in lines 31-33 to be absolute file paths to where the MaxMind files are being stored.
    1. Open a connection to the required MariaDB instance. If there is no running instance, information on starting a local instance can be found below in the "Dockerizing MariaDB" section. Execute the updated SQL within the environment using a super user account. This script will create a new database, populate the required tables, and generate a user with read-only access to the new geoip database.
- If using CSV:
    1. Application.properties will need to be updated to point to the CSV files used in the service. If running in dev mode, this will be the location on your machine, otherwise, this will be the location within the docker container. The fields that need to be updated are `eclipse.subnet.ipv4.path`, `eclipse.subnet.ipv6.path`, and `eclipse.subnet.countries.path`, and should point to the ipv4, ipv6, and countries CSV retrieved by the maxmind script respectively.
        - Note that test application.properties need to reflect host state rather than container state, as its run before the application is packaged.
1. To run the Quarkus API in a docker container, a secret file must be mounted to simulate an OpenShift secret. This can be accomplished by adding `-v <fullpath to dir>/secrets:/run/secrets` to the build command, updating the full path to a secrets file in the project directory that contains a `license_key` file containing a valid maxmind key.

## Build

* Development 

    $ mvn compile quarkus:dev
   
* Build and run test

    $ mvn clean package
    
* Build native 

    $ mvn package -Pnative
    
* Build native & docker image

    $ mvn package -Pnative -Dnative-image.docker-build=true
    docker build -f src/main/docker/Dockerfile.native -t eclipse/geoip .
    docker run -i --rm -p 8080:8080 -v <fullpath to dir>/secrets:/run/secrets eclipse/geoip
    
See https://quarkus.io for more information.


## Dockerizing MariaDB

The `docker-compose.yml` file can be used in any environment with Docker fully configured to run MariaDB with some basic settings. It is recommended that the default root password be changed from `example` to something more secure.

## Copyright 

Copyright (c) 2019 Eclipse Foundation and others.
This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-v20.html,

SPDX-License-Identifier: EPL-2.0
