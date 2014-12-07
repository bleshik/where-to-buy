FROM dockerfile/nodejs:latest
MAINTAINER Alexey Balchunas <bleshik@gmail.com>
RUN apt-get update && apt-get install -y phantomjs
ENV PHANTOMJS_BIN /usr/bin/phantomjs
ADD / /bills
RUN cd /bills && npm install
RUN cd /bills && npm run test-single-run
WORKDIR /bills
EXPOSE 8000
ENTRYPOINT [ "npm" ]
CMD [ "start" ]
