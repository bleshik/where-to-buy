'use strict';

function whFocus($timeout) {
  return function(scope, element, attrs) {
    scope.$watch(attrs.whFocus, 
      function (newValue) { 
        $timeout(function() {
            newValue && element.focus();
        });
      },true);
  };    
}
