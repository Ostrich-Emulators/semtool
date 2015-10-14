    // ---------------------------------
    // User Management Controller
    // ---------------------------------
	
	var UserManagementController = function ($scope, $log) {
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
            SEMOSS.listUsers(
                    function (users) {
    	                $scope.instances = [];
    	                for (var i=0; i<users.length; i++){
    	                	var nativeInstance = new SEMOSS.User();
    	                	nativeInstance.setAttributes(users[i]);
    	                	$scope.instances.push(nativeInstance);
    	                }
    	                $scope.loading = false;
    	                try {
    	                	vm.dtInstance.rerender();
    	                }
    	                catch (err){}
                    });
        };
        
        $scope.rowSelected = function(index){
        	$scope.currentRow = index;
        }
        
        $scope.showEdit = function(username){
       	 	SEMOSS.getUser(username, function(foreignInstance){
       	 		var nativeInstance = new SEMOSS.User();
       	 		nativeInstance.setAttributes(foreignInstance);
       	 		$scope.activeInstance = nativeInstance;
           	 	$scope.mode = 'edit';
           	 	$scope.$apply();
            	$('#user_modal').modal('show');
       	 	});
        }
        
        $scope.update = function(){
       	 	SEMOSS.updateUser($scope.activeInstance, function(result){
       	 		if (result){
        			$scope.instances.splice($scope.currentRow, 1, angular.copy($scope.activeInstance));
        			vm.dtInstance.rerender();
        		}
       	 	});
       	 	$scope.mode = 'edit';
        	$('#user_modal').modal('hide');
        }
        
        $scope.setAccesses = function(){
       	 	SEMOSS.setAccesses($scope.activeInstance.username, function(result){
       	 		if (result){
        			$scope.instances.splice($scope.currentRow, 1, angular.copy($scope.activeInstance));
        			vm.dtInstance.rerender();
        		}
       	 	});
       	 	$scope.mode = 'edit';
       	 	$scope.$apply();
        	$('#user_modal').modal('hide');
        }
        
        $scope.view = function(id){
       	 	SEMOSS.getUser(id, function(foreignInstance){
       	 		var nativeInstance = new SEMOSS.User();
       	 		nativeInstance.setAttributes(foreignInstance);
       	 		$scope.activeInstance = nativeInstance;
           	 	$scope.mode = 'view';
           	 	$scope.$apply();
            	$('#user_modal').modal('show');
       	 	}, true);
        }
        
        $scope.showPrivleges = function(){
        	$('#user_privileges_modal').modal('show');
        }
        
        $scope.showCreate = function(){
   	 		var nativeInstance = new SEMOSS.User();
   	 		$scope.activeInstance = nativeInstance;
       	 	$scope.mode = 'create';
        	$('#user_modal').modal('show');
        }

        $scope.create = function(){
        	SEMOSS.createUser($scope.activeInstance, function(token){
        		$scope.activeInstance.id = token.data.id;
        		
        		if (result){
        			$scope.instances.push(angular.copy($scope.activeInstance));
        			var nameIDPair = new SEMOSS.NameIDPair();
        			nameIDPair.id = token.data.id;
        			nameIDPair.name = $scope.activeInstance.name;
        			SEMOSS.userLookup[nameIDPair.id] = nameIDPair;
        			vm.dtInstance.rerender();
        		}
       	 	}, true);
        	$('user_modal').modal('hide');
        }

        $scope.listInstances = function () {
            $scope.loading = true;
            SEMOSS.listUsers(
                    function (users) {
    	                $scope.instances = [];
    	                for (var i=0; i<users.length; i++){
    	                	var nativeInstance = new SEMOSS.User();
    	                	nativeInstance.setAttributes(users[i]);
    	                	$scope.instances.push(nativeInstance);
    	                }
    	                $scope.loading = false;
    	                vm.dtInstance.rerender();
                    }, true);
        };
        
        $scope.deleteInstance = function(){
        	SEMOSS.deleteUser($scope.deleteID, function(result){
        		if (result){
        			$scope.instances.splice($scope.currentRow, 1);
        			vm.dtInstance.rerender();
        		}
       	 	}, true);
        	$('#user_delete_confirm').modal('hide');
        }
        
        $scope.showDelete = function(id){
        	$scope.deleteID = id;
        	$('#user_delete_confirm').modal('show');
        }
       
        // init immediately
        $scope.init();
       
    };
    

    UserManagementController.$inject = ['$scope', '$log'];
    
    SEMOSSAngular.app.controller('UserManagementController', UserManagementController)