function SearchCtrl($scope, $timeout, $location, whereApi) {
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.$location = $location;
    this.whereApi = whereApi;
    this.$scope.query = this.$location.search().q;
    this.search();
}

SearchCtrl.prototype.search = function() {
    if (this.$scope.query) {
        if (this.timeout) {
            this.$timeout.cancel(this.timeout);
        }
        var _this = this;
        this.timeout = this.$timeout(function() {
            var commodities = _this.whereApi("commodities").query({query: _this.$scope.query}, function() {
                _this.$scope.commodities = commodities;
            });
        }, 100);
    }
}
