FROM bleshik/scala:2.11.2
MAINTAINER Alexey Balchunas <bleshik@gmail.com>
ADD / /bills
RUN cd /bills && sbt compile
WORKDIR /bills
EXPOSE 8080
ENTRYPOINT [ "sbt" ]
CMD [ "run" ]
