#!/usr/bin/env bash
set -eux
# renovate: datasource=github-releases depName=coursier/coursier versioning=semver
COURSIER_VERSION=2.1.26
# renovate: datasource=maven depName=org.apache.maven:apache-maven versioning=maven
MAVEN_VERSION=3.9.16
# renovate: datasource=gradle-version depName=gradle versioning=gradle
GRADLE_VERSION=9.8.2

# Use the upstream JVM launcher so the configured system trust store applies.
curl -fLo /usr/local/bin/cs "https://github.com/coursier/coursier/releases/download/v${COURSIER_VERSION}/coursier"
chmod +x /usr/local/bin/cs
ln -sf /usr/local/bin/cs /usr/local/bin/coursier

curl -fLo maven.zip "https://archive.apache.org/dist/maven/maven-${MAVEN_VERSION%%.*}/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.zip"
unzip -d /opt/maven maven.zip
rm maven.zip
mv /opt/maven/*/* /opt/maven

curl -fLo gradle.zip "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
unzip -d /opt/gradle gradle.zip 
rm gradle.zip
mv /opt/gradle/*/* /opt/gradle

# The JVMs trust the system CA store instead of their bundled cacerts, so CA
# distrusts shipped in Ubuntu's ca-certificates (e.g. emSign ECC Root CA - G3
# after the eMudhra compromise) apply to them without waiting for JDK releases.
mkdir -p /etc/ssl/certs/java
trust extract --overwrite --format=java-cacerts --filter=ca-anchors --purpose=server-auth /etc/ssl/certs/java/cacerts
ln -sf /etc/ssl/certs/java/cacerts "$JAVA_HOME/lib/security/cacerts"

# pre-install JDK for all supported major versions
for JVM_VERSION in 25 21 17
do 
  coursier java --jvm $JVM_VERSION --jvm-index https://github.com/coursier/jvm-index/blob/master/index.json -- -version
  ln -sf /etc/ssl/certs/java/cacerts "$(coursier java-home --jvm $JVM_VERSION --jvm-index https://github.com/coursier/jvm-index/blob/master/index.json)/lib/security/cacerts"
done
