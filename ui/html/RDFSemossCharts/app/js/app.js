'use strict';

angular.module('rdfgraph', ['ui', 'ui.bootstrap']).
  config(['$routeProvider', function($routeProvider) {
    $routeProvider.
		when('/chartit', {
			controller: chartitCtrl,
			templateUrl: 'chartit.html'
		}).
		when('/gridscatterchart', {
			controller: SingleChartCtrl,
			templateUrl: 'gridscatterchart.html'
		});
  }]);
