FROM bleshik/bills-api-base:latest
MAINTAINER Alexey Balchunas <bleshik@gmail.com>
ADD /api /api
ADD /extractor /extractor
RUN cd /api && sbt test
WORKDIR /api
EXPOSE 8080
ENV JAVA_OPTS "-Xms256M -Xmx256M"
ENV ENVIRONMENT "production"
ENTRYPOINT [ "sbt" ]
CMD [ "run" ]
