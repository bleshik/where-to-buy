'use strict';

angular.module('where.filter.util', [])
.filter('objectifyArray', objectifyArray);

function objectifyArray() {
    return function(a, fields) {
        var result = [];
        for (var e in a) {
            var newE = {};
            for (var f in fields) {
                newE[fields[f]] = a[e][f];
            }
            result.push(newE);
        }
        return result;
    };
}
