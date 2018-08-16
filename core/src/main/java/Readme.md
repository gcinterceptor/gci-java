# Core Java package

# GC.java

* This class wraps the communication with JVMTI to force a true GC. This is needed because the `System.gc()` method does not offer the execution guarantee. That can cause latency unexpected latency spikes when GCI is on. [JVMT.ForceGarbageCollection](https://docs.oracle.com/javase/10/docs/specs/jvmti.html#ForceGarbageCollection) guarantees that:

> Force the VM to perform a garbage collection. The garbage collection is as complete as possible. This function does not cause finalizers to be run. This function does not return until the garbage collection is finished.

# Generating JNI header:

```sh
javac -classpath . com/gcinterceptor/core/GC.java
javac -h . com/gcinterceptor/core/GC.java
```

# Compiling libgc.so

```sh
gcc -shared -fpic -I"/usr/lib/jvm/java-6-sun/include" -I"/usr/lib/jvm/java-10.0.1-openjdk-amd64/include" -I"/usr/lib/jvm/java-10.0.1-openjdk-amd64/include/linux" com_gcinterceptor_core_GC.c -o libgc.so
```

# Run GCTest

```sh
javac com/gcinterceptor/core/GCTest.java
java -Djvmtilib=${PWD}/libgc.so -classpath . com.gcinterceptor.core.GCTest
```
