
angular.module('os.administrative.models.order', ['os.common.models'])
  .factory('DistributionOrder', function(osModel, $http, Specimen, SpecimenList) {
    var DistributionOrder = osModel('distribution-orders',
      function(order) {
        if (!!order.specimenList) {
          order.specimenList = new SpecimenList(order.specimenList);
        }
      }
    );

    DistributionOrder.prototype.getType = function() {
      return 'distribution_order';
    }

    DistributionOrder.prototype.getDisplayName = function() {
      return this.name;
    }

    DistributionOrder.prototype.$saveProps = function() {
      this.requester = {id: this.requester.id};
      this.distributionProtocol = {id: this.distributionProtocol.id};
      this.request = !!this.request ? {id: this.request.id} : undefined;
      this.specimenList = !!this.specimenList ? {id: this.specimenList.id} : undefined;
      this.async = true;

      angular.forEach(this.orderItems,
        function(orderItem) {
          orderItem.specimen = {id: orderItem.specimen.id};
        }
      );

      return this;
    }

    DistributionOrder.prototype.generateReport = function() {
      return $http.get(DistributionOrder.url() + this.$id() + '/report').then(
        function(resp) {
          return resp.data;
        }
      );
    }

    DistributionOrder.list = function(opts) {
      return DistributionOrder.query(prepareFilterOpts(opts));
    }

    function prepareFilterOpts(filterOpts) {
      var params = {includeStats: true, query: filterOpts.title};
      return angular.extend(params, filterOpts);
    }

    DistributionOrder.getItemStatusPvs = function() {
      return [
        'DISTRIBUTED',
        'DISTRIBUTED_AND_CLOSED'
      ];
    }

    DistributionOrder.prototype.getOrderItems = function(params) {
      params = params || {};
      return $http.get(DistributionOrder.url() + this.$id() + '/items', {params: params}).then(
        function(resp) {
          return resp.data;
        }
      );
    }

    DistributionOrder.getDistributionDetails = function(labels, filterOpts) {
      filterOpts = filterOpts || {};
      filterOpts.label = labels;
      return $http.get(DistributionOrder.url() + 'specimens', {params: filterOpts}).then(
        function(resp) {
          return resp.data.map(
            function(item) {
              item.specimen = new Specimen(item.specimen);
              return item;
            }
          );
        }
      );
    }

    DistributionOrder.returnSpecimens = function(returnedSpmns) {
      return $http.post(DistributionOrder.url() + 'return-specimens', returnedSpmns).then(
        function(resp) {
          return resp.data;
        }
      );
    }

    DistributionOrder.getOrdersCount = function(filterOpts) {
      return DistributionOrder.getCount(prepareFilterOpts(filterOpts));
    }

    return DistributionOrder;
  });
