function SearchCtrl($rootScope, $scope, $timeout, $location, resources, rublesFilter) {
    this.$rootScope = $rootScope;
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.$location = $location;
    this.resources = resources;

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
        this.timeout = this.$timeout(function() {
            if (_this.$scope.landed) {
                _this.loadMore(true);
            } else {
                _this.$location.search('q', _this.$scope.query);
            }
        }, 100);
    }
}
SearchCtrl.prototype.loadMore = function(replace) {
    var limit = 10;
    var offset = (this.$scope.commodities != null && replace !== true ? this.$scope.commodities.length : 0);
    var _this = this;
    var query = _this.$scope.query;
    this.resources.commodities().query({q: _this.$scope.query, offset: offset, limit: limit, city: this.$scope.city }, function(commodities) {
        commodities.forEach(function(c) {
            c.minPrice = _.min(_.map(c.entries.filter(function (e) { return e.shop.city === _this.$scope.city; }), function(e) { return e.price; }));
        });
        var result = replace !== true && _this.$scope.commodities != null ? _this.$scope.commodities.concat(commodities) : commodities;
        if (_this.$scope.query === query) {
            _this.$scope.commodities = result
        }
    });
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
