FROM rwhaling/native-lambda-base:latest

WORKDIR /build/main/

ADD project/build.properties project/plugin.sbt /build/main/project/
ADD build.sbt /build/main/
ADD src/main/scala/ /build/main/src/main/scala/
RUN sbt nativeLink
RUN zip function.zip target/scala-2.11/main-out 

ENV FUNCTION_NAME=native_test RUNTIME_NAME=native_test_runtime
 