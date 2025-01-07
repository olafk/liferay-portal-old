FROM liferay/jar-runner:latest

COPY --chown=liferay:liferay *.jar /opt/liferay/jar-runner.jar
COPY --chown=liferay:liferay static /static

ENV JAVA_VERSION=zulu21