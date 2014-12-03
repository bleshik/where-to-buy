FROM dockerfile/nodejs:latest
MAINTAINER Alexey Balchunas <bleshik@gmail.com>
ADD / /bills
RUN npm install -g bower
RUN cd /bills && npm install
WORKDIR /bills
EXPOSE 8000
ENTRYPOINT [ "npm" ]
CMD [ "start" ]
