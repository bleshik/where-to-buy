'use strict';

angular.module('where.filter.currency', [])
.filter('rubles', function() {
    return function(cents) {
        var rubles = Math.floor(cents / 100);
        return rubles + " руб.";
    };
});
