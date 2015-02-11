FROM bleshik/scala:2.11.2
MAINTAINER Alexey Balchunas <bleshik@gmail.com>
ADD / /extractor
RUN cd /extractor && sbt test
WORKDIR /extractor
ENV JAVA_OPTS "-Xms256M -Xmx256M"
ENV ENVIRONMENT "production"
ENTRYPOINT [ "sbt" ]
CMD [ "run" ]
