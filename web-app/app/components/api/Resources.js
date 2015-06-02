(function() {
  this.Resources = (function() {
    function Resources($resource, baseUrl, resourcesDeclaration) {
      var json;
      this.$resource = $resource;
      this.baseUrl = baseUrl;
      this.resourcesDeclaration = resourcesDeclaration;
      json = "application/json";
      this.defineResourceMethods = (function(_this) {
        return function(baseUrl, obj) {
          var idName, resourceName, _fn, _ref;
          _ref = _this.resourcesDeclaration;
          _fn = function(resourceName, idName) {
            var resource, url;
            url = "" + baseUrl + "/" + resourceName;
            resource = function(q) {
              var queryParams;
              queryParams = q != null ? _(_.keys(q).filter((function(_this) {
                return function(p) {
                  return p !== idName && url.indexOf(p) < 0;
                };
              })(this))).map((function(_this) {
                return function(p) {
                  return p + "=:" + p;
                };
              })(this)).join("&") : "";
              return $resource(url + (idName != null ? "/:" + idName : "") + (queryParams.length > 0 ? "?" + queryParams : ""), null, {
                get: {
                  method: 'GET',
                  headers: {
                    "Accept": json
                  }
                },
                save: {
                  method: 'POST',
                  headers: {
                    "Accept": json,
                    "Content-Type": json
                  }
                },
                update: {
                  method: 'PUT',
                  headers: {
                    "Accept": json,
                    "Content-Type": json
                  }
                },
                query: {
                  method: 'GET',
                  isArray: true,
                  headers: {
                    "Accept": json
                  }
                },
                remove: {
                  method: 'DELETE'
                },
                "delete": {
                  method: 'DELETE'
                }
              });
            };
            return obj[resourceName] = function(id) {
              var q;
              if ((idName == null) && (id != null)) {
                throw "The resource is a singleton, but was queried with id " + id;
              }
              if (id == null) {
                return _this.defineResourceMethods(url, {
                  query: function(q, cb) {
                    if (q == null) {
                      q = {};
                    }
                    if (typeof q === 'function') {
                      this.query({}, q);
                    }
                    return resource(q).query(q, cb);
                  },
                  get: function(q, cb) {
                    if (q == null) {
                      q = {};
                    }
                    if (typeof q === 'function') {
                      this.query({}, q);
                    }
                    return resource(q).get(q, cb);
                  },
                  save: function(newObj, cb) {
                    return resource().save(newObj, cb);
                  },
                  "delete": function(cb) {
                    return resource()["delete"]({}, cb);
                  }
                });
              } else {
                q = {};
                q[idName] = id;
                return _this.defineResourceMethods("" + url + "/" + id, {
                  update: function(newObj, cb) {
                    return resource().update(q, newObj, cb);
                  },
                  "delete": function(cb) {
                    return resource()["delete"](q, cb);
                  },
                  get: function(cb) {
                    return resource().get(q, cb);
                  },
                });
              }
            };
          };
          for (resourceName in _ref) {
            idName = _ref[resourceName];
            _fn(resourceName, idName);
          }
          return obj;
        };
      })(this);
      this.defineResourceMethods(this.baseUrl, this);
    }

    return Resources;

  })();

}).call(this);
