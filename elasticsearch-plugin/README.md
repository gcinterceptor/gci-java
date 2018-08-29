# GCI Elasticsearch Plugin

## Pre-installation
Before install the plugin, you need to build the gci-core project. It can be done running the command below:

```bash
bash /path/to/gci-core/build.sh
```

## Installing the plugin
Before installing, you must download the elasticsearch. It can be done [here](https://www.elastic.co/downloads/elasticsearch). Run the commands below as wish.

```bash
# To install the gci elasticsearch plugin.
/path/to/elasticsearch/bin/elasticsearch-plugin install file:/your/path/to/gci-java/elasticsearch-plugin/target/gci-elasticsearch-plugin.zip

# To remove the gci elasticsearch plugin.
/path/to/elasticsearch/bin/elasticsearch-plugin remove gci-elasticsearch-plugin

```

## Running 
It is important to point that we need to set libgc.so and plugin-security.policy paths. You can start the elasticsearch application using the gci elasticsearch plugin properly using the command below:

```bash

ES_JAVA_OPTS="-Djvmtilib=/home/davidfq/Desktop/gci-java/core/src/main/java/libgc.so -Djava.security.policy=/home/davidfq/Desktop/gci-java/elasticsearch-plugin/src/main/resources/plugin-security.policy" /path/to/elasticsearch/bin/elasticsearch

```