'use strict';

angular.module('where.search', ['ngRoute', 'ngResource'])
.config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/search', {
        templateUrl: 'search/search.html',
        controller: 'searchCtrl',
        controllerAs: 'ctrl'
    });
}])
.controller('searchCtrl', SearchCtrl);
