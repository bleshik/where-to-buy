function PricesChartCtrl($scope, resources) {
    resources.commodities($scope.commodity != null ? $scope.commodity.name : null).prices().get({city: $scope.city}, function(prices) {
        $scope.commodity = prices;
    });
}
