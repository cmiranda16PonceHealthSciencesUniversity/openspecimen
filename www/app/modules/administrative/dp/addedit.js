
angular.module('os.administrative.dp.addedit', ['os.administrative.models', 'os.query.models'])
  .controller('DpAddEditCtrl', function(
    $scope, $state, $translate, $q, currentUser, distributionProtocol, extensionCtxt,
    DistributionProtocol, Institute, User, SavedQuery, ExtensionsUtil, Alerts) {
    
    var availableInstituteNames = [];
    var availableInstSites = {};
    
    function init() {
      $scope.distributionProtocol = distributionProtocol;
      $scope.op = !distributionProtocol.id ? 'Create' : 'Update';
      $scope.deFormCtrl = {};
      $scope.extnOpts = ExtensionsUtil.getExtnOpts(distributionProtocol, extensionCtxt);
      $scope.userFilterOpts = {institute: distributionProtocol.instituteName};
      $scope.queryList = [];
      $scope.all_sites = $translate.instant('dp.all_sites');
      loadInstitutes();
    }

    function loadInstitutes() {
      $scope.institutes = [];
      $scope.instituteNames = [];
      
      Institute.query().then(
        function(institutes) {
          $scope.institutes = institutes;
          loadDistInstSites();
          setDefaultDistInst();
        }
      );
    }

    function loadQueries(searchTerm) {
      $scope.queryList = SavedQuery.list({searchString: searchTerm});
    }

    function loadDistInstSites() {
      var dp = $scope.distributionProtocol;
      setDistributingSites(dp);
      filterAvailableInstituteNames();
    }

    function setDistributingSites(dp) {
      var result = [];
      angular.forEach(dp.distributingSites, function (instituteSites, institute) {
        if (!instituteSites || instituteSites.length == 0) {
          result.push({instituteName: institute, sites: [$scope.all_sites]});
        } else {
          result.push({instituteName: institute, sites: instituteSites});
        }
      });
      
      if (result.length == 0) {
        result = [{instituteName: '', sites: []}];
      }
      
      dp.distributingSites = result;
    }
    
    function filterAvailableInstituteNames() {
      if (availableInstituteNames.length == 0 ) {
        availableInstituteNames = Institute.getNames($scope.institutes);
      }
      
      var selectedInst = getDistInstitutes();
      $scope.instituteNames = availableInstituteNames.filter(
        function(name) {
          return selectedInst.indexOf(name) == -1;
        }
      )
    }
    
    function getDistInstitutes() {
      var dp = $scope.distributionProtocol;
      if (!dp.distributingSites) {
        dp.distributingSites = [];
      }
      
      return dp.distributingSites.map(function(instSite) { return instSite.instituteName});
    }
    
    function setDefaultDistInst() {
      if (currentUser.admin || !!distributionProtocol.id) {
        return;
      }

      distributionProtocol.distributingSites = [{
        instituteName: $scope.currentUser.instituteName,
        sites: []
      }];

      $scope.onDistInstSelect(0);
    }
    
    function newDistSite() {
      var dp = $scope.distributionProtocol;
      if (!dp.distributingSites) {
        dp.distributingSites = [];
      }
      
      dp.distributingSites.push({instituteName: '', sites: []});
    }
    
    function updateDistSites(dp) {
      var sites = {};
      angular.forEach(dp.distributingSites, function (distSite) {
        if (distSite.sites.indexOf($scope.all_sites) > -1) {
          sites[distSite.instituteName] = [];
        } else {
          sites[distSite.instituteName] = distSite.sites;
        }
      });
      
      dp.distributingSites = sites;
    }
    
    function isAllSitesPresent(index) {
      var dp = $scope.distributionProtocol;
      var sites = dp.distributingSites[index].sites;
      return sites.indexOf($scope.all_sites) > -1;
    }

    //
    // ng-required and therefore form validation is not working on
    // multi-select dropdown widgets; therefore we need to ensure at
    // least one site is selected when a non admin user is creating DP
    //
    function validateDistSites(dp) {
      if (currentUser.admin || currentUser.instituteAdmin) {
        return true;
      }

      if (!dp.distributingSites[0].sites || dp.distributingSites[0].sites.length == 0) {
        Alerts.error('dp.select_dist_site');
        return false;
      }

      return true;
    }
    
    $scope.createDp = function() {
      var formCtrl = $scope.deFormCtrl.ctrl;
      if (formCtrl && !formCtrl.validate()) {
        return;
      }

      if (!validateDistSites($scope.distributionProtocol)) {
        return;
      }

      var dp = angular.copy($scope.distributionProtocol);
      updateDistSites(dp);
      if (formCtrl) {
        dp.extensionDetail = formCtrl.getFormData();
      }

      dp.$saveOrUpdate().then(
        function(savedDp) {
          $state.go('dp-detail.overview', {dpId: savedDp.id});
        }
      );
    };

    $scope.onInstituteSelect = function() {
      $scope.distributionProtocol.principalInvestigator = undefined;
      $scope.distributionProtocol.coordinators = undefined;
      $scope.userFilterOpts = {institute: $scope.distributionProtocol.instituteName};
      $scope.distributionProtocol.defReceivingSiteName = undefined;
    }
    
    $scope.onDistInstSelect = function(index) {
      var dp = $scope.distributionProtocol;
      dp.distributingSites[index].sites = [];
      filterAvailableInstituteNames();
    }
    
    $scope.onDistSiteSelect = function(index) {
      var dp = $scope.distributionProtocol;
      var institute = dp.distributingSites[index].instituteName;
      if (isAllSitesPresent(index)) {
        dp.distributingSites[index].sites = [$scope.all_sites];
      }
    }
    
    $scope.removeDistSite = function(index) {
      var dp = $scope.distributionProtocol;
      dp.distributingSites.splice(index, 1);
      if (dp.distributingSites.length == 0) {
        newDistSite();
      }
      
      filterAvailableInstituteNames();
    }
    
    $scope.addDistSite = newDistSite;

    $scope.loadQueries = loadQueries;
    
    init();
  });
