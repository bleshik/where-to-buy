FROM bleshik/scala:2.11.5
MAINTAINER Alexey Balchunas <bleshik@gmail.com>
ADD / /extractor
RUN cd /extractor && sbt test && sbt stage
WORKDIR /extractor
EXPOSE 9001
ENTRYPOINT [ "target/universal/stage/bin/wh-extractor" ]
CMD []
