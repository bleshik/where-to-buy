FROM bleshik/bills:latest
MAINTAINER Alexey Balchunas <bleshik@gmail.com>
ADD / /bills
RUN cd /bills && sbt compile
WORKDIR /bills
EXPOSE 8080
ENTRYPOINT [ "sbt" ]
CMD [ "run" ]
