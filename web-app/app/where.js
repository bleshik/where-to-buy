'use strict';

angular.module('where', [
  'ngRoute',
  'where.search',
  'where.api',
  'where.filter',
  'where.directive',
  'where.filter.currency'
]).
config(['$routeProvider', function($routeProvider) {
  $routeProvider.otherwise({redirectTo: '/commodities'});
}]);
