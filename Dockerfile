FROM amazonlinux
ENV SCALA_VERSION="2.11.12"
WORKDIR /build/

RUN yum install -y -q -e 0 java-1.8.0 && \
    yum install -y -q -e 0 tar.x86_64 && \
    yum install -y -q -e 0 gzip gunzip

RUN curl -O http://downloads.typesafe.com/scala/${SCALA_VERSION}/scala-${SCALA_VERSION}.tgz 
RUN ls -al
RUN tar -xzvf scala-${SCALA_VERSION}.tgz && \
    rm -rf scala-${SCALA_VERSION}.tgz && \
    echo "export SCALA_HOME=/home/ec2-user/scala-${SCALA_VERSION}" >> ~/.bashrc && \
    echo "export PATH=$PATH:/home/ec2-user/scala-${SCALA_VERSION}/bin" >> ~/.bashrc

#SBT
RUN curl https://bintray.com/sbt/rpm/rpm | tee /etc/yum.repos.d/bintray-sbt-rpm.repo

RUN yum install -y -q -e 0 sbt

WORKDIR /build/warm/

ADD warm/build.sbt warm/main.scala /build/warm/
ADD warm/project/build.properties warm/project/plugin.sbt /build/warm/project/

RUN sbt compile

WORKDIR /build/runtime/

RUN yum install -y -q yum-utils
RUN yum-config-manager --enable epel > /dev/null
RUN yum -y update
RUN yum -y group install "development tools"
RUN yum install -y https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
RUN echo $'[alonid-llvm-3.9.0] \n\
name=Copr repo for llvm-3.9.0 owned by alonid \n\
baseurl=https://copr-be.cloud.fedoraproject.org/results/alonid/llvm-3.9.0/epel-7-$basearch/ \n\
type=rpm-md \n\
skip_if_unavailable=True \n\
gpgcheck=1 \n\
gpgkey=https://copr-be.cloud.fedoraproject.org/results/alonid/llvm-3.9.0/pubkey.gpg \n\
repo_gpgcheck=0 \n\
enabled=1 \n\
enabled_metadata=1' >> /etc/yum.repos.d/epel.repo
RUN yum install -y clang-3.9.0
RUN yum install -y llvm-3.9.0
RUN yum install -y zip which libunwind libunwind-devel python-pip jq
RUN pip install awscli
RUN mkdir -p /build/runtime/lib/ && cp /usr/lib64/libunwind.so /build/runtime/lib/libunwind.so.8 && cp /usr/lib64/libunwind-x86_64.so.8 /build/runtime/lib/libunwind-x86_64.so.8
ADD bootstrap /build/runtime/

RUN zip runtime.zip bootstrap lib/libunwind.so.8 lib/libunwind-x86_64.so.8

ENV PATH="/opt/llvm-3.9.0/bin:${PATH}" CPATH="/opt/opt/llvm-3.9.0/include:${CPATH}"

WORKDIR /build/main/

ADD project/build.properties project/plugin.sbt /build/main/project/
ADD build.sbt main.scala scripts/init.sh scripts/update.sh scripts/delete.sh scripts/invoke.sh /build/main/

RUN sbt compile
RUN sbt nativeLink
# ADD function.sh function.sh 
RUN zip function.zip target/scala-2.11/main-out 

ENV FUNCTION_NAME=native_test RUNTIME_NAME=native_test_runtime
 