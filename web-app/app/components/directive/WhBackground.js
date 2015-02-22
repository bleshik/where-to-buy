'use strict';

function whBackground($timeout){
  return function(scope, element, attrs) {
    scope.$watch(attrs.whBackground, 
      function (url) { 
        var css = url != null && url.length > 0 ? {
            'background-image': 'url(' + url +')',
            'background-size' : 'cover',
            '-webkit-background-size': 'cover',
            '-moz-background-size': 'cover',
            '-o-background-size': 'cover',
            'background-repeat': 'no-repeat'
        } : {
            'background-image': '',
            'background-size' : '',
            '-webkit-background-size': '',
            '-moz-background-size': '',
            '-o-background-size': ''
        };
        $timeout(function() {
            element.css(css);
        });
      },true);
  };    
};
