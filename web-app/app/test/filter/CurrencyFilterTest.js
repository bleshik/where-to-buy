'use strict';

describe('where.filter.currency module', function() {

  beforeEach(module('where.filter.currency'));

  describe('rublesFilter', function(){

    it('should emit cents', inject(function(rublesFilter) {
      expect(rublesFilter(10000)).toEqual("100 руб.");
    }));

  });
});
