
angular.module('openspecimen')
  .controller('RegParticipantCtrl', function(
    $state, $stateParams, $modal, AlertService) {
    
    var modalInstance = $modal.open({
      templateUrl: 'modules/biospecimen/participant/addedit.html',
      controller: 'ParticipantAddEditCtrl',
      resolve: {
        cpId: $stateParams.cpId
      },
      windowClass: 'os-modal-800'
    });

    modalInstance.result.then(
      function(result) {
        $state.go('participant-detail.overview', {cprId: result.id});
      },

      function() {
        $state.go('participant-list');
      }
    );
  })

  .controller('ParticipantAddEditCtrl', function(
    $scope, $modalInstance, $stateParams, 
    AlertService, CprService, ParticipantService,
    SiteService, PvManager) {

    $scope.cpId = $stateParams.cpId;
    $scope.pid = undefined;

    $scope.cpr = {
      participant: {
        pmis: []
      },
      registrationDate: Date.now()
    };

    $scope.addPmi = function() {
      $scope.cpr.participant.pmis.push({mrn: '', site: ''});
    };

    $scope.removePmi = function(index) {
      $scope.cpr.participant.pmis.splice(index, 1);
    };

    SiteService.getSites().then(
      function(result) {
        if (result.status != "ok") {
          alert("Failed to load sites information");
        }
        $scope.sites = result.data;
      }
    );

    PvManager.loadPvs($scope, 'gender');
    PvManager.loadPvs($scope, 'ethnicity');
    PvManager.loadPvs($scope, 'vitalStatus');
    PvManager.loadPvs($scope, 'race');


    var isMatchingInfoPresent = function(participant) { 
      if (participant.lastName && participant.birthDate) {
        return true;
      }

      if (participant.empi) {
        return true;
      }

      if (participant.ssn) {
        return true;
      }
 
      if (participant.pmis && participant.pmis.length > 0) {
        return true;
      }

      return false;
    };
 
 
    var handleMatchedResults = function() {
      if (!$scope.showMatchingParticipants) {
        return false;
      }

      if (!$scope.selectedParticipant) {
        AlertService.display($scope, "Select Participant before moving forward", "danger");
        return false;
      }

      $scope.matchedResults = undefined;
      $scope.showMatchingParticipants = false;
      return true;
    };

    var getMatchingCriteria = function(participant) {
      return {
        lastName: participant.lastName,
        birthDate: participant.birthDate,
        empi: participant.empi,
        ssn : formatSsn(participant.ssn),
        pmis: formatPmis(participant.pmis)
      };
    };

    $scope.validateBasicInfo = function() {
      if ($scope.matchedResults) {
        return handleMatchedResults();
      }   

      var participant = $scope.cpr.participant;
      if (participant.id || !isMatchingInfoPresent(participant)) {
        return true;
      }

      var criteria = getMatchingCriteria(participant);
      if (angular.equals($scope.ignoredCrit, criteria)) {
        return true;
      }

      $scope.matchedResults = undefined;
      return ParticipantService.getMatchingParticipants(criteria).then(
        function(result) {
          if (result.status != 'ok') {
            AlertService.display($scope, "Participant matching failed", "danger");
            return false;
          }

          if (result.data.matchedAttr == 'none') {
            $scope.ignoredCrit = undefined;
            return true;
          }

          $scope.matchedResults = result.data;
          $scope.showMatchingParticipants = true;
          $scope.origParticipant = angular.copy($scope.cpr.participant);
          return false;
        }
      );
    };

    $scope.showMatches = function() {
    };

    $scope.selectParticipant = function(participant) {
      $scope.selectedParticipant = true;
      $scope.cpr.participant = angular.extend({}, participant);
      angular.forEach($scope.cpr.participant.pmis, function(pmi) {
        pmi.site = {name: pmi.siteName};
      });
    };

    $scope.lookupAgain = function() {
      $scope.matchedResults = undefined;
      $scope.showMatchingParticipants = false;
      $scope.cpr.participant = $scope.origParticipant;
      $scope.origParticipant = undefined;
      $scope.selectedParticipant = false;
      $scope.ignoredCrit = undefined;
    };

    $scope.ignoreMatches = function(wizard) {
      $scope.matchedResults = undefined;
      $scope.showMatchingParticipants = false;
      $scope.ignoredCrit = getMatchingCriteria($scope.origParticipant);
      wizard.next(false);
    };

    var formatSsn = function(ssn) {
      if (ssn && ssn.length > 0) {
        ssn = [ssn.slice(0, 3), '-', ssn.slice(3, 5), '-', ssn.slice(5)].join('');
      } 

      return ssn;
    };

    var formatPmis = function(inputPmis) {
      var pmis = [];
      angular.forEach(inputPmis, function(pmi) {
        pmis.push({siteName: pmi.site.name, mrn: pmi.mrn});
      });

      return pmis;
    };

    var handleRegResult = function(result) {
      if (result.status == 'ok') {
        $modalInstance.close(result.data);
      } else if (result.status == 'user_error') {
        var errMsgs = result.data.errorMessages;
        if (errMsgs.length > 0) {
          var errMsg = errMsgs[0].attributeName + ": " + errMsgs[0].message;
          AlertService.display($scope, errMsg, 'danger');
        }
      } else {
        AlertService.display($scope, 'Internal Server Error', 'danger');
      }
    };

    $scope.register = function() {
      var cpr = angular.copy($scope.cpr);
      cpr.cpId = $scope.cpId;
      cpr.participant.ssn = formatSsn(cpr.participant.ssn);
      cpr.participant.pmis = formatPmis(cpr.participant.pmis);
      CprService.registerParticipant(cpr).then(handleRegResult);
    };

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
  });
