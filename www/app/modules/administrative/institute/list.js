angular.module('os.administrative.institute.list', ['os.administrative.models'])
  .controller('InstituteListCtrl', function($scope, $state, Institute, Util, DeleteUtil, ListPagerOpts, CheckList) {

    var pagerOpts;

    function init() {
      pagerOpts = $scope.pagerOpts = new ListPagerOpts({listSizeGetter: getInstitutesCount});
      $scope.instituteFilterOpts = {includeStats: true, maxResults: pagerOpts.recordsPerPage + 1};
      $scope.ctx = {
        exportDetail: {objectType: 'institute'}
      };
      loadInstitutes($scope.instituteFilterOpts);
      Util.filter($scope, 'instituteFilterOpts', loadInstitutes);
    }

    function loadInstitutes(filterOpts) {
      Institute.query(filterOpts).then(
        function(instituteList) {
          $scope.instituteList = instituteList;
          $scope.ctx.checkList = new CheckList(instituteList);
          pagerOpts.refreshOpts(instituteList);
        }
      );
    }

    function getInstituteIds(institutes) {
      return institutes.map(function(institute) { return institute.id; });
    }

    function getInstitutesCount() {
      return Institute.getCount($scope.instituteFilterOpts);
    }

    $scope.showInstituteOverview = function(institute) {
      $state.go('institute-detail.overview', {instituteId: institute.id});
    };

    $scope.deleteInstitutes = function() {
      var institutes = $scope.ctx.checkList.getSelectedItems();

      var opts = {
        confirmDelete:  'institute.delete_institutes',
        successMessage: 'institute.institutes_deleted',
        onBulkDeletion: loadInstitutes
      }

      DeleteUtil.bulkDelete({bulkDelete: Institute.bulkDelete}, getInstituteIds(institutes), opts);
    }

    init();
  });
