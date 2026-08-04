FROM eclipse-temurin:25@sha256:12e44624adee6808a36d962717e1656e0afeeeff5a100f9cb00e0136513558f0

RUN apt-get update && apt-get install --yes jq wget curl zip unzip git python3 python3-pip autoconf automake libtool build-essential libtool make g++

WORKDIR /workdir

COPY ./bin/docker-setup.sh .
RUN ./docker-setup.sh


ENV PATH=/opt/maven/bin:${PATH}
ENV PATH=/opt/gradle/bin:${PATH}
ENV PATH=/root/.local/share/coursier/bin:${PATH}

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=80.0 -XX:+UseContainerSupport"
RUN git config --global --add safe.directory *

COPY . .

RUN gradle --no-daemon :scip-java:installDist && mkdir -p /app/scip-java && cp -R scip-java/build/install/scip-java/. /app/scip-java/

COPY ./bin/scip-java-docker-script.sh /usr/bin/scip-java

WORKDIR /sources

RUN rm -rf /workdir
