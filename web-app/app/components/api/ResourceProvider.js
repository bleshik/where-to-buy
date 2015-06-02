'use strict';

angular.module('where.api', ["ngResource"])
.factory('resources', function($resource) {
    return new Resources($resource, "/api", {
      "commodities" : "name",
      "prices" : null
    });
});
