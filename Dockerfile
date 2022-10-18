FROM eclipse-temurin:11-alpine
#Install curl for health check
RUN apk add --no-cache curl

#This container can access the build artifacts inside the BUILD container.
#Everything that is not copied is discarded
COPY target/transitdata-omm-alert-source-jar-with-dependencies.jar /usr/app/transitdata-omm-alert-source.jar
COPY start-application.sh /
RUN chmod +x /start-application.sh
CMD ["/start-application.sh"]
