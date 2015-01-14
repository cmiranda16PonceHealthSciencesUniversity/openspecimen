angular.module('os.biospecimen.common.uniquespecimenlabel', [])
  .directive('osUniqueSpecimenLabel', function($q, Specimen) {
    return {
      require: 'ngModel',
  
      link: function(scope, elm, attrs, ctrl) {
        ctrl.$asyncValidators.uniqueSpecimenLabel = function(modelValue, viewValue) {
          if (ctrl.$pristine || ctrl.$isEmpty(modelValue)) {
            return $q.when();
          }

          var def = $q.defer();
          Specimen.isUniqueLabel(modelValue).then(
            function(result) {
              if (result) {
                def.resolve();
              } else {
                def.reject();
              }
            },

            function(result) {
              def.resolve();
            }
          );

          return def.promise;
        }
      }
    };
  });
