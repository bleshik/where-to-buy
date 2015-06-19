'use strict';

function WhPricesChart($timeout, rublesFilter){
    return {
        templateUrl: "prices/PricesChart.html",
        restrict: 'E',
        replace: true,
        scope: true,
        controller: PricesChartCtrl,
        link: function(scope, element, attrs) {
            if (attrs.commodityName != null) {
                scope.commodity = { commodityName: attrs.commodityName };
            }
            scope.$watch("commodity.history", function(history) {
                if (history == null) {
                    return;
                }
                scope.options = {
                    axes: {
                        x: {key: '0', ticksFormat: '%-d.%m.%y', type: 'date', ticks: 4},
                        y: {type: 'linear', ticksFormatter: rublesFilter, ticks: 4},
                    },
                    margin: {
                        left: 75
                    },
                    series: [
                        {y: '1', color: 'green', thickness: '2px', type: 'area', striped: true, label: scope.commodity.commodityName},
                    ],
                    lineMode: 'linear',
                    tooltip: {mode: 'scrubber', formatter: function(x, y, series) {return rublesFilter(y);}},
                    drawLegend: false,
                    drawDots: true,
                    hideOverflow: false,
                    columnsHGap: 5
                }
            });
        }
    }
};
