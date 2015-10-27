    // ---------------------------------
    // User Management Controller
    // ---------------------------------
	
	var UserManagementController = function ($scope, $log) {
    	$scope.instances = [];
    	$scope.activeInstance = null;
    	$scope.activeInstancePrivileges = [];
        $scope.loading = false;
        $scope.instanceLoading = false;
        $scope.deleteID = null;
        $scope.allDatabases = [];
        
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
            $scope.getDatabases();
        };
        
        $scope.rowSelected = function(index){
        	$scope.currentRow = index;
        	$scope.activeInstance = $scope.instances[index];
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
        
        $scope.showPrivileges = function(username){
        	$scope.getAccesses(username);
        	$('#user_privileges_modal').modal('show');
        }
        
        $scope.createAccess = function(){
        	$scope.activeInstancePrivileges.push(new SEMOSS.DBPrivilege());
        	$scope.$apply();
        }
        
        $scope.setAccesses = function(){
       	 	SEMOSS.setAccesses($scope.activeInstance.username, $scope.activeInstancePrivileges, function(result){
       	 		if (result){
        			$scope.instances.splice($scope.currentRow, 1, angular.copy($scope.activeInstance));
        		}
       	 	});
       	 	$scope.$apply();
        	$('#user_privileges_modal').modal('hide');
        }
        
        $scope.getAccesses = function(username){
        	SEMOSS.getAccesses(username, function (accesses){
        		$scope.activeInstancePrivileges = [];
        		for(var uri in accesses) {
        			var accessLevel = accesses[uri];
                	var nativeInstance = new SEMOSS.DBPrivilege();
                	nativeInstance.access = accessLevel;
                	nativeInstance.uri = decodeURIComponent(uri);
                	$scope.activeInstancePrivileges.push(nativeInstance);
                }
                $scope.$apply();
        	});
        }
        
        $scope.showCreate = function(){
   	 		var nativeInstance = new SEMOSS.User();
   	 		$scope.activeInstance = nativeInstance;
       	 	$scope.mode = 'create';
        	$('#user_modal').modal('show');
        }

        $scope.createInstance = function(){
        	SEMOSS.createUser($scope.activeInstance, function(result){
        		if (result){
        			$scope.instances.push(angular.copy($scope.activeInstance));
        			vm.dtInstance.rerender();
        		}
       	 	});
        	$('#user_modal').modal('hide');
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
        
        $scope.showPrivleges = function(username){
        	$scope.privileges = [];
        	$('#user_privileges_modal').modal('show');
        }
        
        $scope.getDatabases = function () {
            SEMOSS.listDatabases(
                    function (databases) {
    	                //var returnedInstances = JSON.parse(token.data);
    	                $scope.allDatabases = [];
    	                for (var i=0; i<databases.length; i++){
    	                	var nativeInstance = new SEMOSS.DbInfo();
    	                	nativeInstance.setAttributes(databases[i]);
    	                	$scope.allDatabases.push(nativeInstance.serverUrl);
    	                }
                    }, true);
        };
       
        // init immediately
        $scope.init();
       
    };
    

    UserManagementController.$inject = ['$scope', '$log'];
    
    SEMOSSAngular.app.controller('UserManagementController', UserManagementController)