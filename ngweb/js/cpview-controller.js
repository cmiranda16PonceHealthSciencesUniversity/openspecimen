angular.module('plus.cpview', [])

.controller('CpViewController', ['$scope', 'repository',  function($scope, repository) {

  $scope.selectedCp = selectionCp;
  $scope.selectedParticipant = selParticipant;
    
  repository.getAllCps()           // TODO: Revisit
    .success(function(result) {
      $scope.cps = result;
    });
          
  $scope.participantList =[];

  $scope.onCpSelect = function(selected) {
    $scope.selectedCp = {id: selected.id, shortTitle: selected.text};
      
    repository.getRegisteredParticipants(selected.id, "").success(function(result) {
      $scope.participantList = [];
      for (var i = 0; i < result.length; ++i) {
        var participant = {
          id: result[i].id + "," + result[i].collectionProtocolRegistrationId,
          name: result[i].lastName + "," + result[i].firstName + '(' + result[i].protocolParticipantIdentifier + ')'           
        }
        $scope.participantList.push(participant);
      }
    });
      
    $scope.selectedParticipant={};
    $scope.tree=[];
  };

  $scope.registerParticipant = function() {
    $scope.tree = [];
    var url = "QueryParticipant.do?operation=add&pageOf=pageOfParticipantCPQuery&clearConsentSession=true&cpSearchCpId=" +
               $scope.selectedCp.id + "&refresh=true";
    $('#cpFrameNew').attr('src',url);
  }

  $scope.onParticipantSelect = function(selected, selectedScg) {
    var ids = selected.id.split(',');
    var participantId = ids[0], cprId = ids[1];
    $scope.selectedCprId = cprId;
    $scope.tree=[];

    if (!selectedScg) {
      var url = "QueryParticipantSearchForView.do?pageOf=newParticipantViewPage&operation=edit&cpSearchCpId=" + 
                 $scope.selectedCp.id + "&id=" + participantId;
      $('#cpFrameNew').attr('src',url);		
    }
      
    $scope.selectedParticipant = selected;
    return repository.getCollectionGroups($scope.selectedCprId).then(function(result) {
      var image;
      var collectionStatus;

      var tree = [];
      var scgs = result.data;
      for (var i = 0; i < scgs.length; ++i) {
        var scg = scgs[i];
        tree.push(
          {
            id: 'scg,'+ scg.id, 
            level: 1, 
            type: 'scg',
            name: $scope.getScgLabel(scg),
            collectionStatus: $scope.getStatusIcon(scg.collectionStatus),
            tooltip: $scope.getScgTooltip(scg),
            nodes: [],
            state: 'closed'
          });
      }
      $scope.tree = tree;
      return tree;
    });
  }

  $scope.viewParticipant = function(){
    var participantId = $scope.selectedParticipant.id.split(',')[0];
    var participantId 
    var url = "QueryParticipantSearchForView.do?pageOf=newParticipantViewPage&operation=edit&cpSearchCpId=" + 
              $scope.selectedCp.id + "&id=" + participantId;
    $('#cpFrameNew').attr('src',url);
  }
    
  if ($scope.selectedParticipant && $scope.selectedParticipant.id != -1) {
    $scope.onCpSelect({id: $scope.selectedCp.id, text: $scope.selectedCp.shortTitle});
    var scgTreeQ = $scope.onParticipantSelect(selParticipant, selectedScg);
    scgTreeQ.then(function() { $scope.handleDirectObjectLoad(); });
  } 

  $scope.handleDirectObjectLoad = function() {
    if (!selectedScg) {
      return;
    }

    var scgNode = null;
    var tree = $scope.tree;
    for (var i = 0; i < tree.length; i++) {
      if (tree[i].id == selectedScg.id) {
        scgNode = tree[i];
        break;
      }
    }

    if (!selectedSpecimen) {
      $scope.displayNode(scgNode);
      return;
    } 

    scgNode.state = 'opened';
    var scgId = selectedScg.id.split(',')[1];
    repository.getSpecimens(scgId).success(function(result) {
      scgNode.nodes = $scope.getSpecimenTree(result);
      scgNode.childrenProbed = true;
      if (scgNode.nodes.length == 0) {
        scgNode.state = 'disabled';
      }

      var path = [];
      $scope.getSpecimenNodePath(selectedSpecimen, scgNode.nodes, path);  

      var j = 0;
      var currNode = scgNode;
      for (var i = 0; i < path.length; ++i) {
        currNode.state = 'opened';
        var nodes = currNode.nodes;
        for (var j = 0; j < nodes.length; ++j) {
          if (path[i] == nodes[j].id) {
            currNode = nodes[j];
            break;
          }
        }
      }

      $scope.displayNode(currNode);
    });
  };

  $scope.getSpecimenNodePath = function(specimen, specimens, path) {
    for (var i = 0; i < specimens.length; ++i) {
      path.push(specimens[i].id);
      if (specimen.id == specimens[i].id) {
        return true;
      }

      if ($scope.getSpecimenNodePath(specimen, specimens[i].nodes, path)) {
        return true;
      }

      path.pop();
    }

    return false;
  };

  $scope.getSpecimenTree = function(specimens) {
    var specimenNodes = [];

    if (specimens == null || specimens == undefined) {
      return specimenNodes;
    }

    for (var i = 0; i < specimens.length; ++i) {
      var specimen = specimens[i];
      var name = $scope.getSpecimenName(specimen);
      var specimenNode = {
        id: 'specimen,' + specimen.id,
        name: name,
        tooltip: 'Label: ' + name + ' Type: ' + specimen.specimenType,
        collectionStatus: $scope.getStatusIcon(specimen.collectionStatus),
        type: 'specimen',
        nodes: $scope.getSpecimenTree(specimen.children),
        state: 'closed'
      }

      specimenNode.childrenProbed = true;
      if (specimenNode.nodes.length == 0) {
        specimenNode.state = 'disabled';
      }
      specimenNodes.push(specimenNode);
    }

    return specimenNodes;
  };
  
  $scope.getSpecimenName = function(specimen) {
    var name = specimen.label ? specimen.label : specimen.specimenClass;
    if (specimen.requirementLabel) {
      name = name + "(" + specimen.requirementLabel + ")";
    }
    return name;
  };

  $scope.onNodeToggle = function(data) {
    if (data.state == 'closed') {
      data.state = 'opened';

      if (data.childrenProbed == undefined) {
        if (data.type == 'scg') {
          var scgId = data.id.split(',')[1];
          repository.getSpecimens(scgId).success(function(result) {
            data.nodes = $scope.getSpecimenTree(result);
            data.childrenProbed = true;
            if (data.nodes.length == 0) {
              data.state = 'disabled';
            }
          });
        }
      }
    } else if (data.state == 'opened') {
      data.state = 'closed';
    }
  };

  $scope.displayNode = function(data) {
    $scope.selectedNode = data;

    if(data.type == 'scg') {
      var ids = data.id.split(',');
      var url = "QuerySpecimenCollectionGroupSearch.do?pageOf=pageOfSpecimenCollectionGroupCPQueryEdit&refresh=false&operation=edit&id="
        	 + ids[1] + "&cpSearchCpId=" + $scope.selectedCp.id + "&clickedNodeId="+ ids[1];
      $('#cpFrameNew').attr('src',url);
    } else if(data.type == 'specimen'){
      var ids = data.id.split(',');
      var url = "QuerySpecimenSearch.do?pageOf=pageOfNewSpecimenCPQuery&operation=edit&id=" + ids[1] + 
                "&refresh=false& " + "cpSearchCpId=" + $scope.selectedCp.id;
      $('#cpFrameNew').attr('src',url);
    } 
  }


  $scope.getStatusIcon = function(collectionStatus) {
    var statusIcon;
    if(collectionStatus == 'Complete' || collectionStatus == 'Collected') {
      statusIcon = 'img-circle complete';
    } else if(collectionStatus == 'Not Collected') {
      statusIcon = 'img-circle not-collected';
    } else if (collectionStatus == 'Distributed') {
      statusIcon = 'img-circle distributed';
    } else {
      statusIcon = 'img-circle pending';
    } 
    return statusIcon;
  };

  $scope.getScgLabel = function(scg) {
    var date = scg.receivedDate ? scg.receivedDate : scg.registrationDate;
    return "T" + scg.eventPoint + ": " + scg.collectionPointLabel + ": " + date;
  }

  $scope.getScgTooltip = function(scg) {
    var date = scg.receivedDate ? scg.receivedDate : scg.registrationDate;
    return "Event Point: "+ scg.eventPoint + 
           "; Collection point label: " + scg.collectionPointLabel + 
           "; Received date: " + date;
  }
}]);

