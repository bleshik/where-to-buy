FROM bleshik/scala:2.11.2
MAINTAINER Alexey Balchunas <bleshik@gmail.com>
ADD / /extractor
RUN cd /extractor && sbt test
WORKDIR /extractor
EXPOSE 2552
ENV JAVA_OPTS "-Xms256M -Xmx256M"
ENTRYPOINT [ "sbt" ]
CMD [ "run" ]
