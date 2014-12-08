FROM bleshik/bills-api-base:latest
MAINTAINER Alexey Balchunas <bleshik@gmail.com>
ADD / /bills
RUN cd /bills && sbt test
WORKDIR /bills
EXPOSE 8080
ENV SBT_OPTS -Xms256M -Xmx256M
ENV JAVA_OPTS -Xms256M -Xmx256M
ENTRYPOINT [ "sbt" ]
CMD [ "run" ]
