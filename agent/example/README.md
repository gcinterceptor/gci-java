# GCI Java Agent Example

## Running

```sh
$ mvn clean package
$ java -Djvmtilib=${PATH_TO_GCI_JAVA}/core/src/main/java/libgc.so  -javaagent:${PATH_TO_GCI_JAVA}//agent/target/gciagent-0.1-jar-with-dependencies.jar=8500 -jar target/gci-java-agent-example-1.0-jar-with-dependencies.jar
```