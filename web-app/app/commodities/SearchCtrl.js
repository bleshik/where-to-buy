function SearchCtrl($rootScope, $scope, $timeout, $location, whereApi) {
    this.$rootScope = $rootScope;
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.$location = $location;
    this.whereApi = whereApi;

    this.$scope.query = this.$location.search().q;
    this.$scope.landed = this.$scope.query != null;   
    this.$rootScope.background = this.$scope.landed ? null : "/img/supermarket_" + Math.round(Math.random() * 1) + ".jpg";
    
    var prompts = ["Pepsi", "Coca-Cola", "Конфеты", "Шоколад", "Sprite"];
    this.$scope.queryPrompt = prompts[Math.round(Math.random() * (prompts.length - 1))];

    var titles = ["Где купить", "Окей, Грошри, где купить", "А не отведать ли мне", "Нужно купить", "Где выгоднее купить", "Надо пополнить запасы", "Где дешевле"];
    this.$scope.title = titles[Math.round(Math.random() * (titles.length - 1))];

    this.search();
}

SearchCtrl.prototype.search = function() {
    if (this.$scope.query) {
        if (this.timeout) {
            this.$timeout.cancel(this.timeout);
        }
        var _this = this;
        this.timeout = this.$timeout(function() {
            if (_this.$scope.landed) {
                var commodities = _this.whereApi("commodities").query({query: _this.$scope.query}, function() {
                    _this.$scope.commodities = commodities;
                });
            } else {
                _this.$location.search('q', _this.$scope.query);
            }
        }, 100);
    }
}
