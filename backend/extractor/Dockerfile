FROM bleshik/scala:2.11.5
MAINTAINER Alexey Balchunas <bleshik@gmail.com>
RUN mkdir -p /var/log/dumps
ADD / /extractor
RUN cd /extractor && sbt test && sbt stage
WORKDIR /extractor
EXPOSE 9001
EXPOSE 9010
ENTRYPOINT [ "target/universal/stage/bin/wh-extractor" ]
CMD []
