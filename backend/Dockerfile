FROM bleshik/bills-api-base:latest
MAINTAINER Alexey Balchunas <bleshik@gmail.com>
ADD /api /api
ADD /extractor /extractor
RUN cd /api && sbt test
WORKDIR /api
EXPOSE 8080
EXPOSE 7000
ENTRYPOINT [ "sbt" ]
CMD [ "run" ]
