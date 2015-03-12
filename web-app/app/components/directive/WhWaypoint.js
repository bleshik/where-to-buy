'use strict';

function whWaypoint($timeout) {
    function refresh(context) {
        context.refresh();
        $timeout(function() { refresh(context); }, 1000);
    }
    return function(scope, element, attrs) {
        refresh(new Waypoint({
            element: element,
            handler: function(direction) {
                element.addClass("loading");
                scope.$eval(attrs.whWaypoint);
                element.removeClass("loading");
            },
            offset: 'bottom-in-view'
        }).context);
    }
}
