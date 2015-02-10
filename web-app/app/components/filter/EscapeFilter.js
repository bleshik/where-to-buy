'use strict';

angular.module('where.filter', [])
.filter('escape', function() {
    return window.encodeURIComponent;
});
