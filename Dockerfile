FROM adoptopenjdk/openjdk11:jdk-11.0.4_11-debian

RUN apt-get update && apt-get install -y gnupg apt-transport-https ca-certificates

RUN echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list && \
  curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add && \
  apt-get update && \
  apt-get install -y sbt && \
  sbt sbtVersion

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . /usr/src/app
RUN sbt "project injest" compile
CMD "./usr/src/app/run.sh"
ENTRYPOINT [ "/bin/bash" ]