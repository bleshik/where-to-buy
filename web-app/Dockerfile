FROM dockerfile/nodejs:latest
MAINTAINER Alexey Balchunas <bleshik@gmail.com>
RUN apt-get update && apt-get install -y phantomjs
ENV PHANTOMJS_BIN /usr/bin/phantomjs
ADD / /web-app
RUN cd /web-app && npm install
RUN cd /wen-app && npm run test-single-run
WORKDIR /web-app
ENV ENVIRONMENT "production"
EXPOSE 8000
ENTRYPOINT [ "npm" ]
CMD [ "start" ]
