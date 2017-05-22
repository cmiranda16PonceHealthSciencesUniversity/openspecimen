
angular.module('openspecimen')
  .directive('osClearFilters', function() {
    return {
      restrict: 'E',

      scope: {
        opts: '='
      },

      link: function(scope, element, attrs) {
        scope.clearFilters = function() {
          for (var opt in scope.opts) {
            if (opt !== 'maxResults' && opt !== 'includeStats') {
              delete scope.opts[opt];
            }
          }
        };
      },

      template: '<div class="form-group os-btns">' +
                '  <button class="default full-width" ng-click="clearFilters()">' +
                '     <span translate="common.buttons.clear_filters">Clear Filters</span>' +
                '  </button>' +
                '</div>'
    };
  });
