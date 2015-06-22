'use strict';

angular.module('rdfgraph', ['ui', 'ui.bootstrap']).
  config(['$routeProvider', function($routeProvider) {
    $routeProvider.
		when('/chartit', {
			controller: chartitCtrl,
			templateUrl: 'chartit.html'
		}).
		when('/singlechartgrid', {
			controller: SingleChartCtrl,
			templateUrl: 'singlechartgrid.html'
		});
  }]);
