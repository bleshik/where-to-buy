FROM kyma/docker-nginx
MAINTAINER Alexey Balchunas <bleshik@gmail.com>
ADD / /web-app
RUN apt-get update -y && apt-get install --no-install-recommends -y -q wget curl python build-essential git ca-certificates libfreetype6 libfreetype6-dev libfontconfig1-dev
RUN mkdir /nodejs && curl -L http://nodejs.org/dist/v0.10.33/node-v0.10.33-linux-x64.tar.gz | tar xvzf - -C /nodejs --strip-components=1
RUN mkdir /phantomjs && curl -L https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-1.9.7-linux-x86_64.tar.bz2 | tar xvjf - -C /phantomjs --strip-components=1
ENV PATH $PATH:/nodejs/bin:/phantomjs/bin
RUN phantomjs -v
RUN cd /web-app && npm install npm install phantomjs && npm install -g bower && bower --allow-root install && npm run test-single-run
COPY /web-app/app /var/www
CMD 'nginx'
