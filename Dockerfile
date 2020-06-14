FROM adoptopenjdk/openjdk11:jdk-11.0.7_10-alpine

COPY build/libs/*.jar /app/app.jar

RUN echo -e '#!/bin/sh\nexec java $JAVA_OPTS -jar /app/app.jar "$@"' > /entrypoint.sh && chmod a+x /entrypoint.sh

VOLUME ["~/data"]

ENTRYPOINT ["/entrypoint.sh"]