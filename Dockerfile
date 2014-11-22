FROM bleshik/scala:2.11.2
MAINTAINER Alexey Balchunas <bleshik@gmail.com>
ADD / /bills
RUN cd /bills && sbt test
WORKDIR /bills
EXPOSE 8080
ENV JAVA_OPTS "-Xms256M -Xmx256M"
ENTRYPOINT [ "sbt" ]
CMD [ "run" ]
