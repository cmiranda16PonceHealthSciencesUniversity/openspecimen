
angular.module('os.administrative.form.list', ['os.administrative.models'])
  .controller('FormListCtrl', function(
    $scope, $state, $modal, $translate, Form, FormEntityReg,
    CollectionProtocol, Util, DeleteUtil, Alerts, ListPagerOpts, CheckList) {

    var cpListQ = undefined;
    var pagerOpts;

    function init() {
      pagerOpts = $scope.pagerOpts = new ListPagerOpts({listSizeGetter: getFormsCount});
      $scope.formFilterOpts = {maxResults: pagerOpts.recordsPerPage + 1, excludeSysForms: true};
      $scope.formsList = [];
      $scope.ctx = {};
      loadForms($scope.formFilterOpts);
      Util.filter($scope, 'formFilterOpts', loadForms);
    }

    function loadForms(filterOpts) {
      Form.query(filterOpts).then(function(result) {
        $scope.formsList = result;
        $scope.ctx.checkList = new CheckList(result);
        pagerOpts.refreshOpts(result);
      })
    }

    function reloadForms() {
      loadForms($scope.formFilterOpts);
    }

    function getCpList() {
      if (!cpListQ) {
        cpListQ = CollectionProtocol.list({detailedList: false, maxResults: 1000});
      }

      return cpListQ;
    }

    function deleteForm(form) {
      form.$remove().then(
        function(resp) {
          Alerts.success('form.form_deleted', form);
          reloadForms();
        }
      );
    }

    function getFormsCount() {
      return Form.getCount($scope.formFilterOpts);
    }

    function getFormIds(forms) {
      return forms.map(function(form) { return form.formId; });
    }

    $scope.openForm = function(form) {
      $state.go('form-addedit', {formId: form.formId});
    }

    $scope.showFormContexts = function(form) {
      form.getFormContexts().then(
        function(formCtxts) {
          var formCtxtsModal = $modal.open({
            templateUrl: 'modules/administrative/form/association.html',
            controller: 'FormCtxtsCtrl',

            resolve: {
              args: function() {
                return {
                  formCtxts: formCtxts,
                  form: form
                }
              },

              cpList: function() {
                return getCpList();
              },

              entities: function(FormEntityReg) {
                return FormEntityReg.getEntities();
              }
            }
          });

          formCtxtsModal.result.then(
            function(reload) {
              if (reload) {
                reloadForms();
              }
            }
          );
        }
      );
    };


    $scope.confirmFormDeletion = function(form) {
      FormEntityReg.getEntities().then(
        function(entities) {
          form.entityMap = {};
          angular.forEach(entities,
            function(entity) {
              form.entityMap[entity.name] = entity.caption;
            }
          );
        }
      );
      form.dependentEntities = [];
      form.getDependentEntities().then(
        function(result) {
          Util.unshiftAll(form.dependentEntities, result);
        } 
      );

      DeleteUtil.confirmDelete({
        entity: form,
        templateUrl: 'modules/administrative/form/confirm-delete.html',
        delete: function () { deleteForm(form); }
      });

    }

    $scope.deleteForms = function() {
      var forms = $scope.ctx.checkList.getSelectedItems();

      var opts = {
        confirmDelete:  'form.delete_forms',
        successMessage: 'form.forms_deleted',
        onBulkDeletion: function() {
          loadForms($scope.formFilterOpts);
        }
      }

      DeleteUtil.bulkDelete({bulkDelete: Form.bulkDelete}, getFormIds(forms), opts);
    }

    init();
  });
