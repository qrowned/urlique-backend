FROM openjdk:17-jdk-slim-buster
WORKDIR /app

COPY target/urlique-server-1.0-SNAPSHOT-application/urlique-server-1.0-SNAPSHOT/lib build/lib/
COPY configs build/configs/

WORKDIR /app/build

ENTRYPOINT java -Xms128M -Xmx3072M -cp .:lib/* studio.urlique.server.UrliqueServerApplication