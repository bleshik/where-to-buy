'use strict';

angular.module('where.search', ['ngRoute', 'ngResource'])
.config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/commodities', {
        templateUrl: 'commodities/Commodities.html',
        controller: 'searchCtrl',
        controllerAs: 'ctrl'
    });
}])
.controller('searchCtrl', SearchCtrl);
