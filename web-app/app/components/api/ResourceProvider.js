'use strict';

angular.module('where.api', ["ngResource"])
.factory('whereApi', function($resource) {
    var _this = this;
    this.$resource = $resource;
    return function(name) {
        switch(name) {
            case "commodities": 
                return _this.$resource('/api/commodities/:name?q=:query', {name:'@name', query: "@query"});
        }
    }
});
