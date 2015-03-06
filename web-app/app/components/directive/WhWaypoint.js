'use strict';

function whWaypoint($timeout) {
    return function init(scope, element, attrs) {
        new Waypoint({
            element: element,
            handler: function(direction) {
                element.addClass("loading");
                scope.$eval(attrs.whWaypoint);
                element.removeClass("loading");
                $timeout(function() {
                    Waypoint.refreshAll()
                });
            },
            offset: 'bottom-in-view'
        });
    }
}
