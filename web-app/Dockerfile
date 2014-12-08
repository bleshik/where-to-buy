FROM bleshik/bills-web-app-base
MAINTAINER Alexey Balchunas <bleshik@gmail.com>
ADD / /bills
RUN cd /bills && npm install
RUN cd /bills && bower --allow-root install
RUN cd /bills && npm run test-single-run
WORKDIR /bills
EXPOSE 8000
ENTRYPOINT [ "npm" ]
CMD [ "start" ]
