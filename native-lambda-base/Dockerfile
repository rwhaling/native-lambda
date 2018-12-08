FROM amazonlinux:latest
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
RUN yum install -y llvm-3.9.0 llvm-3.9.0-devel
RUN yum install -y zip which libunwind libunwind-devel python-pip jq libcurl-devel
RUN pip install awscli
RUN mkdir -p /build/runtime/lib/ && cp /usr/lib64/libunwind.so /build/runtime/lib/libunwind.so.8 && cp /usr/lib64/libunwind-x86_64.so.8 /build/runtime/lib/libunwind-x86_64.so.8

RUN yum install -y libidn libidn-devel && cp /usr/lib64/libidn.so.11 lib/libidn.so.11
RUN yum install -y git make openssl-devel

RUN curl -O -L https://github.com/curl/curl/releases/download/curl-7_62_0/curl-7.62.0.tar.gz && tar -zxvf curl-7.62.0.tar.gz
RUN curl -O -L https://github.com/openssl/openssl/archive/OpenSSL_1_0_2l.tar.gz && tar -zxvf OpenSSL_1_0_2l.tar.gz

RUN git clone https://github.com/google/re2.git

WORKDIR /build/runtime/re2/

ENV LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH
ENV PATH="/opt/llvm-3.9.0/bin:${PATH}" CXX=/opt/llvm-3.9.0/bin/clang++ LDFLAGS="-static-libstdc++"

RUN make && make install
RUN cp /usr/local/lib/libre2.so.0 /build/runtime/lib/libre2.so.0 

WORKDIR /build/runtime/openssl-OpenSSL_1_0_2l

RUN ./config --prefix=/opt/lib/ssl --openssldir=/opt/lib/ssl shared zlib
RUN make && make install

WORKDIR /build/runtime/curl-7.62.0/
ENV LD_LIBRARY_PATH=/opt/lib:/usr/lib64:$LD_LIBRARY_PATH
RUN mkdir -p /build/runtime/lib/ssl && cp -r /opt/lib/ssl/lib /build/runtime/lib/ssl/lib

RUN ./configure --prefix=$(/build/runtime) --with-ssl=/opt/lib/ssl && make && make install
# RUN cp /lib/libcurl.so.4  /build/runtime/lib/libcurl.so.4 && cp /usr/lib64/libcrypto.so.10 /build/runtime/lib/libcyrpto.so.10 && cp /usr/lib64/libssl.so.10 /build/runtime/lib/libssl.so.10
RUN cp /lib/libcurl.so.4  /build/runtime/lib/libcurl.so.4 && cp /opt/lib/ssl/lib/libcrypto.so.1.0.0 /build/runtime/lib/libcrypto.so.1.0.0 && cp /opt/lib/ssl/lib/libssl.so.1.0.0 /build/runtime/lib/libssl.so.1.0.0

WORKDIR /build/runtime/

ADD bootstrap /build/runtime/
RUN zip runtime.zip bootstrap lib/libunwind.so.8 lib/libunwind-x86_64.so.8 lib/libre2.so.0 lib/libidn.so.11 lib/libcurl.so.4 lib/libcrypto.so.1.0.0 lib/libssl.so.1.0.0
RUN mkdir -p /build/main

ADD *.sh /build/main/
ENV LD_LIBRARY_PATH=/opt/lib/ssl/lib:/usr/local/lib:$LD_LIBRARY_PATH PATH="/opt/llvm-3.9.0/bin:${PATH}" 
WORKDIR /build/main/

# WORKDIR /build/main/
# ADD project/build.properties project/plugin.sbt /build/main/project/
# ADD build.sbt *.scala scripts/init.sh scripts/update.sh scripts/delete.sh scripts/invoke.sh /build/main/

# ENV LD_LIBRARY_PATH=/opt/lib/ssl/lib:/usr/local/lib:$LD_LIBRARY_PATH
# ENV PATH="/opt/llvm-3.9.0/bin:${PATH}" 

# RUN sbt nativeLink

# RUN zip function.zip target/scala-2.11/main-out 

# ENV FUNCTION_NAME=native_test RUNTIME_NAME=native_test_runtime
 