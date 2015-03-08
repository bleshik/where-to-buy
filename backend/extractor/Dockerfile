FROM bleshik/scala:2.11.5
MAINTAINER Alexey Balchunas <bleshik@gmail.com>
ADD / /extractor
RUN cd /extractor && sbt test
WORKDIR /extractor
EXPOSE 9001
ENTRYPOINT [ "sbt" ]
CMD [ "run" ]
