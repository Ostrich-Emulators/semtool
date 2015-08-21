

    // ---------------------------------
    // Application Data Type Management Controller
    // ---------------------------------
	
	var DatabaseManagementController = function ($scope, $log) {
    	$scope.instances = [];
        $scope.loading = false;
        $scope.instanceLoading = false;
        $scope.deleteID = null;

        var vm= this;
        vm.dtInstance = {};
        vm.dtOptions = {};
        vm.dtColumns = {};
        
        // Initialization function
        $scope.init = function () {
            $scope.loading = true;
            DT.listDatabases(
                    function (token) {
    	                var returnedInstances = JSON.parse(token.data.data);
    	                $scope.instances = [];
    	                for (var i=0; i<returnedInstances.length; i++){
    	                	var nativeInstance = new SEMOSS.VcampDatabase();
    	                	nativeInstance.setAttributes(returnedInstances[i]);
    	                	$scope.instances.push(nativeInstance);
    	                }
    	                $scope.loading = false;
    	                vm.dtInstance.rerender();
                    }, true);
        };
        
        $scope.rowSelected = function(index){
        	$scope.currentRow = index;
        }
        
        $scope.showEdit = function(id){
       	 	DT.getDatabase(id, function(token){
       	 		var foreignInstance = JSON.parse(token.data.data);
       	 		var nativeInstance = new SEMOSS.VcampDatabase();
       	 		nativeInstance.setAttributes(foreignInstance);
       	 		$scope.activeInstance = nativeInstance;
           	 	$scope.mode = 'edit';
           	 	$scope.$apply();
            	$('#semossdb_modal').modal('show');
       	 	}, true);
        }
        
        $scope.update = function(){
       	 	DT.updateDatabase($scope.activeInstance, function(token){
       	 		DT.processAnyFailures(token, 'Database successfully updated.');
        		if (token.result <= 1){
        			$scope.instances.splice($scope.currentRow, 1, angular.copy($scope.activeInstance));
        			vm.dtInstance.rerender();
        		}
       	 	}, true);
       	 	$scope.mode = 'edit';
       	 	$scope.$apply();
        	$('#semossdb_modal').modal('hide');
        }
        
        $scope.view = function(id){
       	 	DT.getDatabase(id, function(token){
       	 		var foreignInstance = JSON.parse(token.data.data);
       	 		var nativeInstance = new SEMOSS.VcampDatabase();
       	 		nativeInstance.setAttributes(foreignInstance);
       	 		$scope.activeInstance = nativeInstance;
           	 	$scope.mode = 'view';
           	 	$scope.$apply();
            	$('#semossdb_modal').modal('show');
       	 	}, true);
        }
        
        $scope.showCreate = function(){
   	 		var nativeInstance = new SEMOSS.VcampDatabase();
   	 		$scope.activeInstance = nativeInstance;
       	 	$scope.mode = 'create';
        	$('#semossdb_modal').modal('show');
        }

        $scope.create = function(){
        	DT.createApplicationDataType($scope.activeInstance, function(token){
        		$scope.activeInstance.id = token.data.id;
        		DT.processAnyFailures(token, 'Database successfully created.');
        		if (token.result <= 1){
        			$scope.instances.push(angular.copy($scope.activeInstance));
        			vm.dtInstance.rerender();
        		}
       	 	}, true);
        	$('#semossdb_modal').modal('hide');
        }

        $scope.listInstances = function () {
            $scope.loading = true;
            DT.listApplicationDataTypes(
                    function (token) {
    	                var returnedInstances = JSON.parse(token.data.data);
    	                $scope.instances = [];
    	                for (var i=0; i<returnedInstances.length; i++){
    	                	var nativeInstance = new SEMOSS.VcampDatabase();
    	                	alert(returnedInstances[i]);
    	                	nativeInstance.setAttributes(returnedInstances[i]);
    	                	$scope.instances.push(nativeInstance);
    	                }
    	                $scope.loading = false;
    	                vm.dtInstance.rerender();
                    }, true);
        };
        
        $scope.deleteInstance = function(){
        	DT.deleteApplicationDataType($scope.deleteID, function(token){
        		DT.processAnyFailures(token, 'Database successfully deleted.');
        		if (token.result <= 1){
        			$scope.instances.splice($scope.currentRow, 1);
        			vm.dtInstance.rerender();
        		}
       	 	}, true);
        	$('#semossdb_delete_confirm').modal('hide');
        }
        
        $scope.showDelete = function(id){
        	$scope.deleteID = id;
        	$('#semossdb_delete_confirm').modal('show');
        }
       
        // init immediately
        $scope.init();
       
    };
    

    DatabaseManagementController.$inject = ['$scope', '$log'];
    
    DTAngular.app.controller('DatabaseManagementController', DatabaseManagementController)

