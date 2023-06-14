FROM openjdk:17-jdk-slim-buster
MAINTAINER urlique.studio

WORKDIR /app

COPY target/urlique-server-1.0-SNAPSHOT-application/urlique-server-1.0-SNAPSHOT/lib lib/

ENTRYPOINT ["java", "-Xms128M", "-Xmx3072M", "-cp", ".:lib/*", "studio.urlique.server.UrliqueServerApplication"]