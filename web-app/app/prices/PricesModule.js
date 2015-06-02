'use strict';

angular.module('where.prices', [
    'ngResource',
    'n3-charts.linechart'
])
.controller('pricesChartCtrl', PricesChartCtrl)
.directive('whPricesChart', WhPricesChart);
