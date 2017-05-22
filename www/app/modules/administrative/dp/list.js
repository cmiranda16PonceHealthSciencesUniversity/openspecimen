
angular.module('os.administrative.dp.list', ['os.administrative.models'])
  .controller('DpListCtrl', function(
    $scope, $state, DistributionProtocol, Util, DeleteUtil,
    PvManager, ListPagerOpts, CheckList) {

    var pagerOpts;

    function init() {
      pagerOpts = $scope.pagerOpts = new ListPagerOpts({listSizeGetter: getDpsCount});
      $scope.dpFilterOpts = {includeStats: true, maxResults: pagerOpts.recordsPerPage + 1};
      $scope.ctx = {};
      loadDps($scope.dpFilterOpts);
      Util.filter($scope, 'dpFilterOpts', loadDps);
      loadActivityStatuses();
    }
    
    function loadDps(filterOpts) {
      DistributionProtocol.query(filterOpts).then(
        function(dps) {
          $scope.distributionProtocols = dps;
          $scope.ctx.checkList = new CheckList(dps);
          pagerOpts.refreshOpts(dps);
        }
      );
    }

    function getDpsCount() {
      return DistributionProtocol.getCount($scope.dpFilterOpts);
    }

    function loadActivityStatuses () {
      PvManager.loadPvs('activity-status').then(
        function (result) {
          $scope.activityStatuses = [];
          angular.forEach(result, function (status) {
            if (status != 'Disabled' && status != 'Pending') {
              $scope.activityStatuses.push(status);
            }
          });
        }
      );
    }

    function getDpIds(dps) {
      return dps.map(function(dp) { return dp.id; });
    }

    $scope.showDpOverview = function(distributionProtocol) {
      $state.go('dp-detail.overview', {dpId: distributionProtocol.id});
    };

    $scope.deleteDps = function() {
      var dps = $scope.ctx.checkList.getSelectedItems();
      var opts = {
        confirmDelete:  'dp.delete_dps',
        successMessage: 'dp.dps_deleted',
        onBulkDeletion: loadDps
      }

      DeleteUtil.bulkDelete({bulkDelete: DistributionProtocol.bulkDelete}, getDpIds(dps), opts);
    }

    init();
  });
