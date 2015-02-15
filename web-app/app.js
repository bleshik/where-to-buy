var express = require('express'),
    httpProxy = require('http-proxy'),
    path = require('path');

eval(require('fs').readFileSync('app/config/current.js', 'utf8'));
        
var proxy = new httpProxy.createProxy();
proxy.on('error', function (error, req, res) {
    var json;
    console.log('proxy error', error);
    if (!res.headersSent) {
        res.writeHead(500, { 'content-type': 'application/json' });
    }

    json = { error: 'proxy_error', reason: error.message };
    res.end(JSON.stringify(json));
});
 
var app = express();
 
app.use('/', express.static(path.join(__dirname, 'app/')));
 
app.all('/api/*',  function (req, res) {
    req.url = req.url.slice(4);
    return proxy.web(req, res, {
        target: "http://" + config.api.host + ":" + config.api.port
    });
});
 
console.log("Started server on " + config.port)
app.listen(config.port);
