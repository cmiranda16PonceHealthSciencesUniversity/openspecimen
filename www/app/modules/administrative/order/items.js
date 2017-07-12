angular.module('os.administrative.order')
  .controller('OrderItemsCtrl', function($scope, order) {
  
    var ctx = {
      totalItems: 0,
      currPage: 1,
      itemsPerPage: 100,
      items: [],
      loading: false
    };

    function init() {
      $scope.ctx = ctx;
      loadOrderItems(); 
      $scope.$watch('ctx.currPage', loadOrderItems);
    }

    function loadOrderItems() {
      //
      // if pending order is created using specimen list, show specimen list link
      //
      if (order.status === 'PENDING' && !!order.specimenList) {
        return;
      }

      var startAt     = (ctx.currPage - 1) * ctx.itemsPerPage;
      var maxResults  = ctx.itemsPerPage + 1;
      var queryParams = {startAt: startAt, maxResults: maxResults};
      ctx.loading = true;
      order.getOrderItems(queryParams).then(
        function(orderItems) {
          ctx.totalItems = (ctx.currPage - 1) * ctx.itemsPerPage + orderItems.length;
          if (orderItems.length >= maxResults) {
            orderItems.splice(orderItems.length - 1, 1);
          }

          ctx.items = orderItems;
          ctx.loading = false;
        }
      );
    }

    init();
  });
