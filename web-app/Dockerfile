FROM bleshik/bills-web-app-base
MAINTAINER Alexey Balchunas <bleshik@gmail.com>
ADD / /web-app
RUN cd /web-app && npm install
RUN cd /web-app && bower --allow-root install
RUN cd /web-app && npm run test-single-run
WORKDIR /web-app
EXPOSE 8000
ENTRYPOINT [ "npm" ]
CMD [ "start" ]
