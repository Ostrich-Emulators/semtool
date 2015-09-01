var SEMOSSAngular = [];

SEMOSSAngular.app = angular.module('semoss', ['datatables', 'ui.bootstrap']);

angular.module('semoss').config(['$controllerProvider', function($controllerProvider) {
    $controllerProvider.allowGlobals();
 }]);