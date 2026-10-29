#!/usr/bin/env bash
set -eux
curl -fLo /usr/local/bin/cs https://github.com/coursier/coursier/releases/download/v2.1.26/coursier
chmod +x /usr/local/bin/cs
# Keep a JVM launcher so the system trust store applies. Resolve Tika explicitly
# because Coursier 2.1.26 still depends on a version affected by CVE-2025-66516.
cs bootstrap io.get-coursier:coursier-cli_3:2.1.26 \
  --main-class coursier.cli.Coursier \
  --force-version org.apache.tika:tika-core:3.2.2 \
  --standalone -o /usr/local/lib/coursier.jar
rm /usr/local/bin/cs
ln -s /usr/local/lib/coursier.jar /usr/local/bin/cs
ln -s /usr/local/lib/coursier.jar /usr/local/bin/coursier
# The standalone launcher contains its resolved dependencies; discard the
# bootstrap launcher's old dependency cache, which is no longer used.
rm -rf /root/.cache/coursier/v1

curl -fLo maven.zip https://archive.apache.org/dist/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip
unzip -d /opt/maven maven.zip
rm maven.zip
mv /opt/maven/*/* /opt/maven

curl -fLo gradle.zip https://services.gradle.org/distributions/gradle-9.8.2-bin.zip
unzip -d /opt/gradle gradle.zip 
rm gradle.zip
mv /opt/gradle/*/* /opt/gradle

# Both distributions bundle Guava versions affected by CVE-2026-102554.
GUAVA_VERSION=33.7.2-jre
curl -fLo "/opt/maven/lib/guava-$GUAVA_VERSION.jar" "https://repo.maven.apache.org/maven2/com/google/guava/guava/$GUAVA_VERSION/guava-$GUAVA_VERSION.jar"
cp "/opt/maven/lib/guava-$GUAVA_VERSION.jar" /opt/gradle/lib/
rm /opt/maven/lib/guava-33.6.0-jre.jar /opt/gradle/lib/guava-33.4.6-jre.jar
# Gradle's dependency metadata names the JAR as well as its version.
sed -i "s/33.4.6-jre/$GUAVA_VERSION/g" /opt/gradle/lib/guava.properties

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
