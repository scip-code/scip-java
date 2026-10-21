#!/usr/bin/env bash
set -eux
curl -fLo /usr/local/bin/cs https://github.com/coursier/coursier/releases/download/v2.1.5/coursier
chmod +x /usr/local/bin/cs
ln -sf /usr/local/bin/cs /usr/local/bin/coursier
# Only install the JVM launcher. The native `cs` app embeds the trust store of
# the GraalVM it was built with, so it ignores the trust store set up below.
cs setup --yes --apps coursier

curl -fLo maven.zip https://archive.apache.org/dist/maven/maven-3/3.9.1/binaries/apache-maven-3.9.1-bin.zip 
unzip -d /opt/maven maven.zip
rm maven.zip
mv /opt/maven/*/* /opt/maven

curl -fLo gradle.zip https://services.gradle.org/distributions/gradle-9.4.1-bin.zip
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
