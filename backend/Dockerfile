FROM bleshik/scala:2.11.5
MAINTAINER Alexey Balchunas <bleshik@gmail.com>
RUN mkdir -p /var/log/dumps
ADD /api /api
ADD /extractor /extractor
RUN cd /api && sbt test && sbt stage
WORKDIR /api
EXPOSE 8080
EXPOSE 9000
EXPOSE 9010
ENTRYPOINT [ "target/universal/stage/bin/wh-api" ]
CMD []
