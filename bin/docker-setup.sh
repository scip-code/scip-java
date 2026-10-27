#!/usr/bin/env bash
set -eux
curl -fLo /usr/local/bin/coursier https://github.com/coursier/coursier/releases/download/v2.1.5/coursier
chmod +x /usr/local/bin/coursier
coursier setup --yes --apps coursier,sbt

curl -fLo maven.zip https://archive.apache.org/dist/maven/maven-3/3.9.1/binaries/apache-maven-3.9.1-bin.zip 
unzip -d /opt/maven maven.zip
rm maven.zip
mv /opt/maven/*/* /opt/maven

curl -fLo gradle.zip https://services.gradle.org/distributions/gradle-7.6.1-bin.zip
unzip -d /opt/gradle gradle.zip 
rm gradle.zip
mv /opt/gradle/*/* /opt/gradle

# The JVMs trust the system CA store instead of their bundled cacerts, so CA
# distrusts shipped in Ubuntu's ca-certificates (e.g. emSign ECC Root CA - G3
# after the eMudhra compromise) apply to them without waiting for JDK releases.
mkdir -p /etc/ssl/certs/java
trust extract --overwrite --format=java-cacerts --filter=ca-anchors --purpose=server-auth /etc/ssl/certs/java/cacerts

link_cacerts() {
  # JDK 8 keeps its trust store under jre/, later JDKs under lib/.
  for cacerts in "$1/lib/security/cacerts" "$1/jre/lib/security/cacerts"
  do
    if [ -f "$cacerts" ]; then
      ln -sf /etc/ssl/certs/java/cacerts "$cacerts"
      return
    fi
  done
  echo "no cacerts found in $1" >&2
  exit 1
}

link_cacerts "$JAVA_HOME"

# pre-install JDK for all major versions
for JVM_VERSION in 21 17 11 8
do 
  coursier java --jvm $JVM_VERSION --jvm-index https://github.com/coursier/jvm-index/blob/master/index.json -- -version
  link_cacerts "$(coursier java-home --jvm $JVM_VERSION --jvm-index https://github.com/coursier/jvm-index/blob/master/index.json)"
done
