function SearchCtrl($rootScope, $scope, $timeout, $location, whereApi) {
    this.$rootScope = $rootScope;
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.$location = $location;
    this.whereApi = whereApi;

    this.$scope.city = this.$location.search().city;
    this.$scope.city = this.$scope.city != null ? this.$scope.city : "Москва";

    this.$scope.query = this.$location.search().q;
    this.land(this.$scope.query != null);
    this.search();
}

SearchCtrl.prototype.search = function() {
    if (this.$scope.query) {
        if (this.timeout) {
            this.$timeout.cancel(this.timeout);
        }
        var _this = this;
        var _timeout = this.$timeout(function() {
            if (_this.$scope.landed) {
                var commodities = _this.whereApi("commodities").query({query: _this.$scope.query}, function() {
                    if (_timeout == _this.timeout) {
                        commodities.forEach(function(c) {
                            c.minPrice = _.min(_.map(c.entries.filter(function (e) { return e.shop.city === _this.$scope.city; }), function(e) { return e.price; }));
                        });
                        _this.$scope.commodities = commodities;
                    }
                });
            } else {
                _this.$location.search('q', _this.$scope.query);
            }
        }, 100);
        this.timeout = _timeout;
    }
}

SearchCtrl.prototype.land = function(landed) {
    this.$scope.landed = landed;   
    this.$rootScope.background = this.$scope.landed ? null : "/img/supermarket_" + Math.round(Math.random() * 1) + ".jpg";
    
    var prompts = ["Pepsi", "Coca-Cola", "Конфеты", "Шоколад", "Sprite"];
    this.$scope.queryPrompt = prompts[Math.round(Math.random() * (prompts.length - 1))];

    var titles = ["Где купить", "Окей, Грошри, где купить", "А не отведать ли мне", "Нужно купить", "Где выгоднее купить", "Надо пополнить запасы", "Где дешевле"];
    this.$scope.title = titles[Math.round(Math.random() * (titles.length - 1))];
}

SearchCtrl.prototype.canQueryOnChange = function() {
    return !isMobile();
}
