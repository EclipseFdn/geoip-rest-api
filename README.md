# Eclipse Foundation GeoIP REST API

## Summary

TODO!!

## Requirements

1. Installed and configured JDK 1.11+
1. Apache Maven 3.5.3+
1. Docker + Docker-compose
1. GraalVM (for compilation of native-image)
1. Make

## Configuration

To import MaxMind data for usage with this microservice, CSV and binary database versions of MaxMind's GeoLite2 data must be retrieved. This can be done using the script `./bin/maxmind.sh <location>`, with location being the path to where the data MaxMind data should be stored. This will retrieve, extract, and clean up unneeded MaxMind files for use with the microservice. Note that this script uses Unix based commands, and will not work in windows environments unless run through a Unix terminal emulator (like WSL). This requires a license key for MaxMind to be set into your local environment. This can be done by copying the `./config/.env.sample` into the project root as `.env` and updating the value of the variable to be your license key.  

Once the environment variable is available, the import can be run through `make install`. By default, this script installs the maxmind assets in the current project directory under the gitignored `./maxmind` folder. The stack can also be started instead, using `make headless-docker` which starts the API, binding it to port 8080.


## Build

* Development 

    $ mvn compile quarkus:dev
   
* Build docker container

    make headless-docker
    
See https://quarkus.io for more information.


## Copyright 

Copyright (c) 2022 Eclipse Foundation and others.
This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-v20.html,

SPDX-License-Identifier: EPL-2.0
