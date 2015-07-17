'use strict';

/* Controllers */

function chartitCtrl($scope, $http) {
    $scope.types = {};
    $scope.types[0] = [];
    $scope.instances = {};
    $scope.properties = {};
    $scope.values = {};
    $scope.selectedInstances = {};
    $scope.typesSelected = {};
    $scope.instancesSelected = {};
    $scope.instancesSelected[0] = [];
    $scope.propertiesSelected = {};
    $scope.propertiesSelected[0] = [];
    $scope.groupby = 'Property';
    $scope.grid = {selected: false};
    
    
    $scope.setJSONData = function (data) {
        $scope.$apply(function () {
            
            $scope.data = jQuery.parseJSON(data);
            
            var key;
            for (key in $scope.data.Nodes) {
                $scope.types[0].push({'name': $scope.data.Nodes[key][0].propHash.type, 'selected': false});
            }
            
        });
    };
    
    //----------Comment the below $http.get when using in Java
    /*$http.get('lib/longDataNames.json').success(function(jsonData) {
        $scope.data = jsonData;
    
        var key;
        for (key in $scope.data.Nodes) {
            $scope.types[0].push({'name': $scope.data.Nodes[key][0].propHash.type, 'selected': false});
        }
    });*/

    
    $scope.typesChecked = function(){
        if(this.type.selected == true){

            var dim = this.dimension.index;
            var type = this.type.name;
            $scope.instances[dim] = [];
            $scope.instancesSelected[dim] = [];
            $scope.propertiesSelected[dim] = [];
            $scope.properties[dim] = [];
            $scope.values[dim] = [];
            var typeInstances = [];
            this.dimension.selectedIntance = false;
            
            
            if(!$scope.typesSelected[dim]){
                $scope.typesSelected[dim] = [];
            }
    
            
            $scope.typesSelected[dim].push(type);
            for(var i=0; i<$scope.typesSelected[dim].length; i++){
                for(var j=0; j<$scope.data.Nodes[$scope.typesSelected[dim][i]].length; j++){
                    typeInstances.push({'name': $scope.data.Nodes[$scope.typesSelected[dim][i]][j], 'selected': false});
                }
            }
            $scope.instances[dim] = typeInstances;
            
        }else if(this.type.selected == false){
            
            var dim = this.dimension.index;
            var type = this.type.name;
            $scope.typesSelected[dim] = _.without($scope.typesSelected[dim], type);
            $scope.instances[dim] = [];
            $scope.instancesSelected[dim] = [];
            $scope.propertiesSelected[dim] = [];
            $scope.properties[dim] = [];
            $scope.values[dim] = [];
            this.dimension.selectedType = false;
            this.dimension.selectedInstance = false;
            
            var typeInstances = [];
            for(var i=0; i<$scope.typesSelected[dim].length; i++){
                for(var j=0; j<$scope.data.Nodes[$scope.typesSelected[dim][i]].length; j++){
                    typeInstances.push({'name': $scope.data.Nodes[$scope.typesSelected[dim][i]][j], 'selected': false});
                }
            }
            
            $scope.instances[dim] = typeInstances;
            
            if($scope.typesSelected[dim].length == 0){
                $scope.instances[dim] = [];
            }
        }
    }    
    
    $scope.instancesChecked = function(){
        if(this.instance.selected == true){

            var dim = this.dimension.index;
            var instance = this.instance.name;
            $scope.propertiesSelected[dim] = [];
            $scope.values[dim] = [];
            $scope.instancesSelected[dim].push(instance);
            
            var key;
            var reducedProps = [];
            $scope.properties[dim] = [];
            for(var i=0; i<$scope.instancesSelected[dim].length; i++){
                for (key in $scope.instancesSelected[dim][i].propHash) {
                    reducedProps.push(key);
                }
            }
            reducedProps = _.uniq(reducedProps);
            
            for(var i=0; i<reducedProps.length; i++){
                $scope.properties[dim].push({'name':reducedProps[i], 'selected': false});
            }
            
        }else if(this.instance.selected == false){
            //declare and set variables
            var dim = this.dimension.index;
            var instance = this.instance.name;
            var key;
            var reducedProps = [];
            $scope.properties[dim] = [];
            $scope.values[dim] = [];
            this.allInstances = false;
            this.dimension.selectedInstance = false;
            
            $scope.instancesSelected[dim] = _.without($scope.instancesSelected[dim], instance);
            
            for(var i=0; i<$scope.instancesSelected[dim].length; i++){
                for (key in $scope.instancesSelected[dim][i].propHash) {
                    reducedProps.push(key);
                }
            }
            
            reducedProps = _.uniq(reducedProps);
            
            for(var i=0; i<reducedProps.length; i++){
                $scope.properties[dim].push({'name':reducedProps[i], 'selected': false});
            }
            
            if($scope.instancesSelected[dim].length == 0){
                $scope.instancesSelected[dim] = [];
                $scope.propertiesSelected[dim] = [];
                $scope.values[dim] = [];
            }
            
        }
    }
    
    //when radio button is clicked, this function executes
    $scope.propertiesChecked = function(){
        //If a radio button is selected, the ng model value (propety.name) is always defined.
        if(this.property.name !== undefined){
            //var dim is the variable which holds which dimension is being used.
            var dim = this.dimension.index;
            //This takes the properties names which have been selected inside the dimension and adds them to the propertiesSelected array. 
            $scope.propertiesSelected[dim] = [];
            $scope.propertiesSelected[dim].push(this.property.name);
            //This defines the values array
            $scope.values[dim] = [];
            //This fills the values array with all of the names of the properties that have been selected
            for(var i=0; i<$scope.instancesSelected[dim].length; i++){
               $scope.values[dim].push({'name': $scope.instancesSelected[dim][i].propHash[this.property.name], 'selected': false});
            }
        }
    }
    
    $scope.selectAllTypes = function(){
        if(this.dimension.selectedType == true){
            //declare variables
            var dim = this.dimension.index;
            $scope.typesSelected[dim] = [];
            $scope.instances[dim] = [];
            $scope.instancesSelected[dim] = [];
            $scope.propertiesSelected[dim] = [];
            $scope.properties[dim] = [];
            $scope.values[dim] = [];
            var typeInstances = [];
            this.dimension.selectedInstance = false;
            
            //Loops
            for(var i=0; i<$scope.types[dim].length; i++){
                $scope.typesSelected[dim].push($scope.types[dim][i].name);
                $scope.types[dim][i].selected = true;
            }
            for(var i=0; i<$scope.typesSelected[dim].length; i++){
                for(var j=0; j<$scope.data.Nodes[$scope.typesSelected[dim][i]].length; j++){
                    typeInstances.push({'name': $scope.data.Nodes[$scope.typesSelected[dim][i]][j], 'selected': false});
                }
            }
            $scope.instances[dim] = typeInstances;
        }else{
            var dim = this.dimension.index;
            $scope.instances[dim] = [];
            $scope.instancesSelected[dim] = [];
            $scope.propertiesSelected[dim] = [];
            $scope.properties[dim] = [];
            $scope.values[dim] = [];
            $scope.instances[dim] = [];
            this.dimension.selectedInstance = false;
            
            for(var i=0; i<$scope.types[dim].length; i++){
                $scope.types[dim][i].selected = false;
            }
            
        }
    }
    
    $scope.selectAllInstances = function(){
        if(this.dimension.selectedInstance == true){
            //declare variables
            var dim = this.dimension.index;
            $scope.instancesSelected[dim] = [];
            $scope.properties[dim] = [];
            $scope.propertiesSelected[dim] = [];
            $scope.values[dim] = [];
            var key;
            var reducedProps = [];
            
            for(var i=0; i<$scope.instances[dim].length; i++){
                $scope.instancesSelected[dim].push($scope.instances[dim][i].name);
                $scope.instances[dim][i].selected = true;
            }
                
            for(var i=0; i<$scope.instancesSelected[dim].length; i++){
                for (key in $scope.instancesSelected[dim][i].propHash) {
                    reducedProps.push(key);
                }
            }
            reducedProps = _.uniq(reducedProps);
            
            for(var i=0; i<reducedProps.length; i++){
                $scope.properties[dim].push({'name':reducedProps[i], 'selected': false});
            }
            
        }else{
            var dim = this.dimension.index;
            $scope.properties[dim] = [];
            $scope.instancesSelected[dim] = [];
            $scope.propertiesSelected[dim] = [];
            $scope.values[dim] = [];
            this.dimension.selectedInstance = false;
            for(var i=0; i<$scope.instances[dim].length; i++){
                $scope.instances[dim][i].selected = false;
            }
        }
    }
    
    
    
    $scope.drawGraph = function(chartType){
        var wrapper;
        var hAxisTitle = '';
        var vAxisTitle = 'Count';
        var axisParams = [];
        var yAxesVal = [];
        var seriesVal = [];
        var series1Val = [];
        var series2Val = [];
        var reducedInstances = [];
        var colors = Highcharts.getOptions().colors;
        
        //The start of massaging the data
        if($scope.groupby == 'Instance'){
            //Declaring variables
            hAxisTitle = 'Property';
            //var reducedInstances = [];
            
            //loops through each dimension then each instance array and pushes all the selected instances into reducedInstances array
            for(var i=0; i<$scope.dimensions.length; i++){
                if($scope.instancesSelected[i]){
                    for(var j=0; j<$scope.instancesSelected[i].length; j++){
                        reducedInstances.push($scope.instancesSelected[i][j]);
                    }
                }
            }
            //returns all unique instances to reducedInstances
            reducedInstances = _.uniq(reducedInstances);
            
            //adds all the selected unique instances to the columns
            //Params pushed in order of dimensions added
            //Params applied in order of [x axis, y axis, 3rd dimension, ... ]
            for(var i=0; i<reducedInstances.length; i++){
            	axisParams.push(reducedInstances[i].propHash.label);
            }
            //set cols variable
            var cols = axisParams.length;
            
            //check if pie chart
            if(chartType == 'pie'){
                //check if first dimensions
                if($scope.dimensions.length ==1){
                    var instanceParams = [];
                    //loops through and adds every Instance + Property value to the pie chart
                    for(var j=0; j<reducedInstances.length; j++){
                        for(var i=0; i<$scope.dimensions.length; i++){
                            if($scope.propertiesSelected[i]){
                                if(reducedInstances[j].propHash[$scope.propertiesSelected[i][0]]){
                                    instanceParams.push([reducedInstances[j].propHash.label + '-' + $scope.propertiesSelected[i][0], reducedInstances[j].propHash[$scope.propertiesSelected[i][0]]]);
                                }else{
                                    instanceParams.push('', 0);
                                }
                            } 
                        }
                    }
                    seriesVal.push({'name': 'Count', 'data': instanceParams});
                    
                //all other dimensions start here
                }else{
                    //loop through to get the inner circles values into series1Val
                    for(var i=0; i<$scope.dimensions.length; i++){
                        if($scope.propertiesSelected[i]){
                            var totalPropVal = 0;
                            for(var j=0; j<cols; j++){
                                if(reducedInstances[j].propHash[$scope.propertiesSelected[i][0]]){
                                    totalPropVal += parseInt(reducedInstances[j].propHash[$scope.propertiesSelected[i][0]]);
                                }else{
                                    totalPropVal += 0;
                                }
                            }
                            series1Val.push({'name': $scope.propertiesSelected[i][0], 'y': totalPropVal, 'color': colors[i]});
                        }
                    }
                    
                    //loop through to get the outer circles values into series2Val
                    for(var j=0; j<$scope.dimensions.length; j++){
                        if($scope.propertiesSelected[j]){
                            for(var i=0; i<reducedInstances.length; i++){
                                if(reducedInstances[i].propHash[$scope.propertiesSelected[j][0]]){
                                    var brightness = 0.2 - (i / reducedInstances.length) /5;
                                    series2Val.push({'name': reducedInstances[i].propHash.label, 'y': reducedInstances[i].propHash[$scope.propertiesSelected[j][0]], 'color': Highcharts.Color(series1Val[j].color).brighten(brightness).get()});
                                }else{
                                    series2Val.push({'name': reducedInstances[i].propHash.label, 'y': 0});
                                }
                            }
                        }
                    }
                    
                    //setting up the series correctly with the 2 pie charts data sets: series1Val and series2Val
                    seriesVal = [{
                        name: 'Property',
                        data: series1Val,
                        size: '60%',
                        dataLabels: {
                            formatter: function() {
                                var shortName = this.point.name.length > 18 ? this.point.name.substring(0,18) + '...' : this.point.name;
                                return '<b>'+ shortName +':</b> '+ this.y;
                            },
                            color: 'white',
                            distance: -50
                        }
                    }, {
                        name: 'Instance',
                        data: series2Val,
                        size: '80%',
                        innerSize: '60%',
                        dataLabels: {
                            formatter: function() {
                                var shortName = this.point.name.length > 18 ? this.point.name.substring(0,18) + '...' : this.point.name;
                                // display only if larger than 1
                                return this.y > 0 ? '<b>'+ shortName +':</b> '+ this.y : null;
                            }
                        }
                    }];
                        
                }
            }else{
                for(var i=0; i<$scope.dimensions.length; i++){
                    if($scope.propertiesSelected[i]){
                        var instanceParams = [];
                        for(var j=0; j<cols; j++){
                            if(reducedInstances[j].propHash[$scope.propertiesSelected[i][0]]){
                                instanceParams.push(reducedInstances[j].propHash[$scope.propertiesSelected[i][0]]);
                            }else{
                                instanceParams.push(0);
                            }
                        }
                        seriesVal.push({'name': $scope.propertiesSelected[i][0], 'data': instanceParams});
                    }
                }
            }
            
        //if anything but instance is selected, the data is parsed below
        }else{
            
            for(var i=0; i<$scope.dimensions.length; i++){
                if($scope.propertiesSelected[i]){
                    for(var j=0; j<$scope.propertiesSelected[i].length; j++){
                    	axisParams.push($scope.propertiesSelected[i][0]);
                    }
                }
            }
            
            for(var i=0; i<$scope.dimensions.length; i++){
                if($scope.instancesSelected[i]){
                    for(var j=0; j<$scope.instancesSelected[i].length; j++){
                        reducedInstances.push($scope.instancesSelected[i][j]);
                    }
                }
            }
            reducedInstances = _.uniq(reducedInstances);
            
            var cols = axisParams.length;
            //check if chart type selected is 'pie'
            if(chartType == 'pie'){
                //check the number of dimensions
                if($scope.dimensions.length == 1){
                    for(var i=0; i<$scope.dimensions.length; i++){
                        if($scope.propertiesSelected[i]){
                            var propParams = [];
                            for(var j=0; j<reducedInstances.length; j++){
                                if(reducedInstances[j].propHash[$scope.propertiesSelected[i][0]]){
                                    propParams.push([reducedInstances[j].propHash.label, reducedInstances[j].propHash[$scope.propertiesSelected[i][0]]]);
                                }else{
                                    propParams.push(0);
                                }
                            }
                            seriesVal.push({'name': $scope.propertiesSelected[i][0], 'data': propParams});
                        }
                    }
                //for all pie charts with dimensions more than 1
                }else{
                    //loop to set series1Val with inner circle values
                    for(var i=0; i<reducedInstances.length; i++){
                        var totalInstanceVal = 0;
                        for(var j=0; j<cols; j++){
                            if($scope.propertiesSelected[j]){
                                if(reducedInstances[i].propHash[$scope.propertiesSelected[j][0]]){
                                    totalInstanceVal += parseInt(reducedInstances[i].propHash[$scope.propertiesSelected[j][0]]);
                                }else{
                                    totalInstanceVal += 0;
                                }
                            }
                        }
                        series1Val.push({'name': reducedInstances[i].propHash.label, 'y': totalInstanceVal, 'color': colors[i]});
                    }
                    
                    //loop to set series2Val with outer circle values
                    for(var j=0; j<reducedInstances.length; j++){
                        for(var i=0; i<$scope.dimensions.length; i++){
                            if($scope.propertiesSelected[i]){
                                if(reducedInstances[j].propHash[$scope.propertiesSelected[i][0]]){
                                    var brightness = 0.2 - (i / $scope.dimensions.length) /5;
                                    series2Val.push({'name': $scope.propertiesSelected[i][0], 'y': reducedInstances[j].propHash[$scope.propertiesSelected[i][0]], 'color': Highcharts.Color(series1Val[j].color).brighten(brightness).get()});
                                }else{
                                    series2Val.push({'name': $scope.propertiesSelected[i][0], 'y': 0});
                                }
                            }
                        }
                    }
                    
                    //setting up the series correctly with the 2 pie charts data sets
                    seriesVal = [{
                        name: 'Instance',
                        data: series1Val,
                        size: '60%',
                        dataLabels: {
                            formatter: function() {
                                var shortName = this.point.name.length > 18 ? this.point.name.substring(0,18) + '...' : this.point.name;
                                return '<b>'+ shortName +':</b> '+ this.y;
                            },
                            color: 'white',
                            distance: -30
                        }
                    }, {
                        name: 'Property',
                        data: series2Val,
                        size: '80%',
                        innerSize: '60%',
                        dataLabels: {
                            formatter: function() {
                                var shortName = this.point.name.length > 18 ? this.point.name.substring(0,18) + '...' : this.point.name;
                                // display only if larger than 1
                                return this.y > 0 ? '<b>'+ shortName +':</b> '+ this.y: null;
                            }
                        }
                    }];
                    
                }
            //all other chart types
            }else if(chartType == 'scatter' && $scope.dimensions.length == 2){
                var propertyVals = [];
                for(var i=0; i<$scope.dimensions.length; i++){
                    if($scope.propertiesSelected[i]){
                        propertyVals[i] = [];
                        for(var j=0; j<reducedInstances.length; j++){
                            if(reducedInstances[j].propHash[$scope.propertiesSelected[i][0]]){
                                propertyVals[i].push(reducedInstances[j].propHash[$scope.propertiesSelected[i][0]]);
                            }else{
                                propertyVals[i].push(0);
                            }
                        }
                    }
                }
                var data = [];
                for(var i=0; i<reducedInstances.length; i++){
                     data.push({name: reducedInstances[i].propHash.label, x: propertyVals[0][i], y: propertyVals[1][i]});
                }
                seriesVal.push({'name': 'Instances', 'data': data});
            //sets the data for bubble charts with 3 dimensions
            }else if(chartType == 'bubble' && $scope.dimensions.length == 3){
                var propertyVals = [];
                for(var i=0; i<$scope.dimensions.length; i++){
                    if($scope.propertiesSelected[i]){
                        propertyVals[i] = [];
                        for(var j=0; j<reducedInstances.length; j++){
                            if(reducedInstances[j].propHash[$scope.propertiesSelected[i][0]]){
                                propertyVals[i].push(reducedInstances[j].propHash[$scope.propertiesSelected[i][0]]);
                            }else{
                                propertyVals[i].push(0);
                            }
                        }
                    }
                }
                var data = [];
                for(var i=0; i<reducedInstances.length; i++){
                     //data.push({name: reducedInstances[i].propHash.label, x: propertyVals[0][i], y: propertyVals[1][i], marker: { radius: propertyVals[2][i]}});
                    data.push({name: reducedInstances[i].propHash.label, data: [[propertyVals[0][i], propertyVals[1][i],propertyVals[2][i]]]});
                }
                //seriesVal.push({'name': 'Instances', 'data': data});
                seriesVal = data;
            //all other chart types data is set here
            }else{
                //loop to set series value with name and data
                for(var i=0; i<reducedInstances.length; i++){
                    var propParams = [];
                    for(var j=0; j<cols; j++){
                        if($scope.propertiesSelected[j]){
                            if(reducedInstances[i].propHash[$scope.propertiesSelected[j][0]]){
                                propParams.push(reducedInstances[i].propHash[$scope.propertiesSelected[j][0]]);
                            }else{
                                propParams.push(0);
                            }
                        }
                    }
                    seriesVal.push({'name': reducedInstances[i].propHash.label, 'data': propParams});
                }
            }
        //end of the '$scope.groupby' else
        }        

        createChart(chartType, axisParams, seriesVal);
        
        function createChart(cType, axisParams, dataSeries){
            //creating the chart
            jQuery('#visualization').highcharts({
                chart: {
                    type: cType
                },
                credits: {
                    text: ''
                },
                title: {
                    text: ''
                },
                xAxis: getxAxisOptions(axisParams),
                yAxis: getyAxisOptions(axisParams),
                plotOptions: {
                    pie: {
                        dataLabels: getDataLabels(),
                    },
                    series: {
                        cursor: 'pointer',
                        point: {
                            events: {
                                click: function() {
                                    clickChartPoint(this);
                                }
                            }
                        }
                    }
                },
                legend: getLegend(),
                tooltip: getTooltipOptions(),
                series: dataSeries
            });
        }
        
        function getxAxisOptions(axisParams){
            //check to see which charts is selected and how many dimensions
            if(chartType == 'column' || chartType == 'line' || chartType == 'bar' || (chartType == 'scatter' && $scope.dimensions.length == 1)){
                return {
                    categories: axisParams,
                    labels: {
                        rotation: getRotation(),
                        align: 'right',
                        formatter : function() {
                                        return this.value.length > 16 ? this.value.substring(0,16) + '...' : this.value;
                                    }
                    }
                }
            //check to see which charts is selected and how many dimensions
            }else if ((chartType == 'scatter' && $scope.dimensions.length > 1 && $scope.groupby == 'Property') || (chartType == 'bubble')){
                //
                if($scope.grid.selected == true){
                    var xlineVal = getxLineVal();
                    //adds a line for the quadrant grid and returns the title of the xAxis
                    return {
                        plotLines: [{
                            color: 'blue',
                            width: 2,
                            value: xlineVal
                        }],
                        title: {
                            text: axisParams[0]
                        },
                    };
                }else{
                    //returns the xAxis title
                    return {
                        title: {
                            text: axisParams[0]
                        },
                    };
                }
                
            //all other graph types
            }else{
                return {categories: axisParams};
            }
        }
        
        function getyAxisOptions(axisParams){
            if((chartType == 'scatter' && $scope.dimensions.length > 1 && $scope.groupby == 'Property') || (chartType == 'bubble')){
                if($scope.grid.selected == true){
                    var ylineVal = getyLineVal();
                    //adds a line for the quadrant grid and returns the title of the xAxis
                    return {
                        plotLines: [{
                            color: 'blue',
                            width: 2,
                            value: ylineVal
                        }],
                        title: {
                            text: axisParams[1]
                        },
                    };
                }else{
                    return {
                    	title: {
                    		text: axisParams[1]
                    	},
                    	enabled: true
                    };
                }
            }else{
                return {
                	title: {
                		text: "Values"
                	},
                	enabled: true
                };
            }
        }
        
        function getTooltipOptions(){
            if(chartType == 'scatter' && $scope.dimensions.length == 2 && $scope.groupby == 'Property'){
                return {
                        formatter: function(){

                            var xVal = this.x.toString().substring(0,6);
                            var yVal = this.y.toString().substring(0,6);
                            var shortKey = this.key.length > 25 ? this.key.substring(0,25) + '...' : this.key;
                            
                            var dividedxAxisName = '';
                            var brokenxAxisArray = this.series.xAxis.axisTitle.text.replace(/_/g, ' ');
                            brokenxAxisArray = brokenxAxisArray.replace(/.{25}\S*\s+/g, "$&@").split(/\s+@/);
                            for (var i = 0; i<brokenxAxisArray.length; i++) {
                                dividedxAxisName += '<b>' + brokenxAxisArray[i] + '</b>';
                                if(i!==brokenxAxisArray.length-1) {
                                    dividedxAxisName += '<br/>';
                                }
                            }

                            var dividedyAxisName = '';
                            var brokenyAxisArray = this.series.yAxis.axisTitle.text.replace(/_/g, ' ');
                            brokenyAxisArray = brokenyAxisArray.replace(/.{25}\S*\s+/g, "$&@").split(/\s+@/);
                            for (var i = 0; i<brokenyAxisArray.length; i++) {
                                dividedyAxisName += '<b>' + brokenyAxisArray[i] + '</b>';
                                if(i!==brokenyAxisArray.length-1) {
                                    dividedyAxisName += '<br/>';
                                }
                            }
                            
                            return '<b>' + shortKey + '</b><br/>' + dividedxAxisName + ': ' + xVal + '<br/>' + dividedyAxisName + ': ' + yVal + '<br/>';
                        }
                    };
            }else if(chartType == 'bubble' && $scope.dimensions.length == 3 && $scope.groupby == 'Property'){
                 return {
                        formatter: function(){


                            var xVal = this.x.toString().substring(0,6);
                            var yVal = this.y.toString().substring(0,6);
                            var zVal = this.point.options.z.toString().substring(0,11);
                            var shortKey = this.series.userOptions.name.length > 18 ? this.series.userOptions.name.substring(0,18) + '...' : this.series.userOptions.name;
                            
                            var dividedxAxisName = '';
                            var brokenxAxisArray = this.series.xAxis.axisTitle.text.replace(/_/g, ' ');
                            brokenxAxisArray = brokenxAxisArray.replace(/.{25}\S*\s+/g, "$&@").split(/\s+@/);
                            for (var i = 0; i<brokenxAxisArray.length; i++) {
                                dividedxAxisName += '<b>' + brokenxAxisArray[i] + '</b>';
                                if(i!==brokenxAxisArray.length-1) {
                                    dividedxAxisName += '<br/>';
                                }
                            }

                            var dividedyAxisName = '';
                            var brokenyAxisArray = this.series.yAxis.axisTitle.text.replace(/_/g, ' ');
                            brokenyAxisArray = brokenyAxisArray.replace(/.{25}\S*\s+/g, "$&@").split(/\s+@/);
                            for (var i = 0; i<brokenyAxisArray.length; i++) {
                                dividedyAxisName += '<b>' + brokenyAxisArray[i] + '</b>';
                                if(i!==brokenyAxisArray.length-1) {
                                    dividedyAxisName += '<br/>';
                                }
                            }

                            var dividedzAxisName = '';
                            var brokenzAxisArray = $scope.propertiesSelected[2][0].replace(/_/g, ' ');
                            brokenzAxisArray = brokenzAxisArray.replace(/.{25}\S*\s+/g, "$&@").split(/\s+@/);
                            for (var i = 0; i<brokenzAxisArray.length; i++) {
                                dividedzAxisName += '<b>' + brokenzAxisArray[i] + '</b>';
                                if(i!==brokenzAxisArray.length-1) {
                                    dividedzAxisName += '<br/>';
                                }
                            }
                            
                            return '<b>' + shortKey + '</b><br/>' + dividedxAxisName + ': ' + xVal + '<br/>' + dividedyAxisName + ': ' + yVal + '<br/>' + dividedzAxisName + ' (radius): ' + zVal;
                        }
                    };
            }else{
                if(chartType == 'column' || chartType == 'bar' || chartType == 'scatter' || chartType == 'line'){
                    return {
                        enabled: true,
                        formatter: function(){
                            var shortKey = this.key.length > 25 ? this.key.substring(0,25) + '...' : this.key;
                            var shortName = this.series.name.length > 18 ? this.series.name.substring(0,18) + '...' : this.series.name;
                            return '<b>' + shortKey + '</b><br/><b>' + shortName + '</b>: ' + this.y;
                        }
                    };
                }else if(chartType == 'pie' && $scope.dimensions.length == 1){
                    return {
                        enabled: true,
                        formatter: function(){
                            var shortKey = this.key.length > 25 ? this.key.substring(0,25) + '...' : this.key;
                            var shortName = this.series.name.length > 18 ? this.series.name.substring(0,18) + '...' : this.series.name;
                            return '<b>' + shortKey + '</b><br/><b>' + shortName + '</b>: ' + this.y;
                        }
                    };
                }else if(chartType == 'pie' && $scope.dimensions.length == 2){
                    return {
                        enabled: true,
                        formatter: function(){
                            var shortKey = this.key.length > 25 ? this.key.substring(0,25) + '...' : this.key;
                            var shortName = this.series.name.length > 18 ? this.series.name.substring(0,18) + '...' : this.series.name;
                            return '<b>' + shortKey + '</b><br/><b>' + shortName + '</b>: ' + this.y;
                        }
                    };
                }
            }
        }
        
        function getRotation(){
            if(chartType == 'column' || chartType == 'line' || chartType == 'scatter'){
                return -45;
            }else{
                return 0;
            }
        }
        
        function getLegend(){
            if(chartType == 'bubble'){
                return {enabled: false};
            }else{
                return {enabled: true,
                        labelFormatter: function() {
                        return this.name.length > 15 ? this.name.substring(0,15) + '...' : this.name;
                    }
                };
            }
        }
        
        function getDataLabels(){
            if(chartType == 'pie'){
                return{
                    enabled: true,
                    formatter: function() {
                        var shortName = this.point.name.length > 20 ? this.point.name.substring(0,20) + '...' : this.point.name;
                        return '<b>'+ shortName +':</b> '+ this.y;
                    },
                    color: 'black',
                    distance: -30
                }
            }else{
                return {enabled: false};
            }
        }
        
        //returns the value on the x axis where the vertical line of the application health grid will be locatied
        function getxLineVal(){
            return '';
        }
        //returns the value on the y axis where the horizontal line of the application health grid will be locatied
        function getxLineVal(){
            return '';
        }
        
        
        function clickChartPoint(that){
            if(chartType == 'pie' && $scope.dimensions.length > 1){
                if($scope.groupby == 'Property'){
                    var  reducedInstances = [];
                    var axisParams = [];
                    var seriesVal = [];
                    //loops through each dimension then each instance array and pushes all the selected instances into reducedInstances array
                    for(var i=0; i<$scope.dimensions.length; i++){
                        if($scope.instancesSelected[i]){
                            for(var j=0; j<$scope.instancesSelected[i].length; j++){
                                reducedInstances.push($scope.instancesSelected[i][j]);
                            }
                        }
                    }
                    //returns all unique instances to reducedInstances
                    reducedInstances = _.uniq(reducedInstances);
                    reducedInstances = _.filter(reducedInstances, function(inst){return inst ? inst.propHash.label == that.name : null});
                    
                    //checks to see if reducedInstances is empty and will stop the function if so
                    if(reducedInstances.length == 0){
                        return;
                    }
                    
                    //adds all the selected unique instances to the columns
                    for(var i=0; i<reducedInstances.length; i++){
                        axisParams.push(reducedInstances[i].propHash.label);
                    }
                    
                    for(var j=0; j<reducedInstances.length; j++){
                        if($scope.propertiesSelected[i]){
                            var propParams = [];
                            for(var i=0; i<$scope.dimensions.length; i++){
                                if(reducedInstances[j].propHash[$scope.propertiesSelected[i][0]]){
                                    propParams.push([$scope.propertiesSelected[i][0], reducedInstances[j].propHash[$scope.propertiesSelected[i][0]]]);
                                }else{
                                    propParams.push(0);
                                }
                            }
                            seriesVal.push({'name': reducedInstances[j].propHash.label, 'data': propParams});
                        }
                    }
                    createChart(chartType, axisParams, seriesVal);
                }else if($scope.groupby == 'Instance'){
                    var  reducedInstances = [];
                    var axisParams = [];
                    var seriesVal = [];
                    
                    for(var i=0; i<$scope.dimensions.length; i++){
                        if($scope.instancesSelected[i]){
                            for(var j=0; j<$scope.instancesSelected[i].length; j++){
                                reducedInstances.push($scope.instancesSelected[i][j]);
                            }
                        }
                    }
                    reducedInstances = _.uniq(reducedInstances);
                    


                    var propParams = [];
                    for(var j=0; j<reducedInstances.length; j++){
                        if(reducedInstances[j].propHash[that.name]){
                            propParams.push([reducedInstances[j].propHash.label, reducedInstances[j].propHash[that.name]]);
                        }else{
                            return;
                        }
                    }
                    seriesVal.push({'name': that.name, 'data': propParams});


                    createChart(chartType, that.name, seriesVal);
                }
            }
        }
        
    }
    
    /*------when to disable chart types-------*/
    $scope.isPieDisabled = function(){
        if(($scope.groupby =='Instance' && $scope.dimensions.length == 1) || ($scope.groupby == undefined && $scope.dimensions.length == 1) || $scope.dimensions.length > 2){
            return true;
        }else{
            return false;
        }
    }
    
    $scope.isScatterDisabled = function(){
        if($scope.groupby == 'Instance' || $scope.dimensions.length > 2){
            return true;
        }else{
            return false;
        }
    }
    
    $scope.isBubbleDisabled = function(){
        if($scope.groupby == 'Property' && $scope.dimensions.length == 3){
            return false;
        }else{
            return true;
        }
    }
    
    $scope.isDeleteShow = function(){
        if(this.dimension.index > 0){
            return true;
        }else{
            return false;
        }
    }
    
    function forLoop(array1, array2){
        var returnArray = [];
        for(var i=0; i<array1.length; i++){
            for(var j=0; j<array2[array1[i]].length; j++){
                returnArray.push({'name': array2[array1[i]][j], 'selected': false});
            }
        }
        return returnArray;
    }
    
     /*---------Accordion---------*/
    $scope.oneAtATime = false;
    $scope.dimensions = [{name: 'Dimension 1', index: 0, selectedType: false, selectedInstance: false}];
     
    //add new accordion group
    $scope.addDimension = function() {
        var newDimensionNo = $scope.dimensions.length + 1;
        $scope.dimensions.push({name: 'Dimension ' + newDimensionNo, index: $scope.dimensions.length, selectedType: false, selectedInstance: false});
        
        var key;
        $scope.types[$scope.dimensions.length -1] = [];
        for (key in $scope.data.Nodes) {
            $scope.types[$scope.dimensions.length -1].push({'name': $scope.data.Nodes[key][0].propHash.type, 'selected': false});
        }
        
    };
    
    //delete accordion group
    $scope.deleteDim = function(){
        $scope.dimensions.splice(this.dimension.index, 1);
        
        for(var i=0; i<$scope.dimensions.length; i++){
            $scope.dimensions[i].name = "Dimension " + (i+1);
            $scope.dimensions[i].index = i;
        }
    }
}


function SingleChartCtrl($scope, $http) {
	alert("in SingleChartCtrl");
    
    //creates object that holds all graph option values
    var graphOptions = {
        xAxis: '',
        yAxis: '',
        zAxis: '',
        chartTitle: '',
        chartType: '',
        stackType: '',
        xAxisCategories: '',
        xLineVal: '',
        yLineVal: '',
        xLineWidth: '',
        yLineWidth: '',
        xMax: undefined,
        xMin: undefined,
        xInterval: undefined,
        yMax: undefined,
        yMin: undefined,
        yInterval: undefined,
        series: []
    };
    $scope.yExtraLegend = [];
    $scope.xExtraLegend = [];
    

    //function used to interact with the inline javascript
    $scope.setJSONData = function (data) {
        $scope.$apply(function () {
            graphOptions.series = [];
            var jsonData = jQuery.parseJSON(data);

            setChartData(jsonData);
        });
    };
    
    //----------Comment the below $http.get when using in Java
    /*$http.get('lib/singleChartGridData.json').success(function(jsonData) {
        setChartData(jsonData);
    });*/
    
    //takes the "data" parameter passed and sorts all the data appropriately
    function setChartData(data) {

        if (data.yLineLegend) {
            //declare the scope variable that will hold all the extra legend information
            for (var i=0; i<data.yLineLegend.length; i++) {
                if( Object.prototype.toString.call( data.yLineVal ) === '[object Array]' ) {
                    $scope.yExtraLegend.push(data.yLineVal[i] + " = " + data.yLineLegend[i]);
                }else {
                    $scope.yExtraLegend.push(data.yLineVal + " = " + data.yLineLegend[i]);
                }
                
            }
        }
        
        if (data.xLineLegend) {
            //declare the scope variable that will hold all the extra legend information
            for (var i=0; i<data.xLineLegend.length; i++) {
                if( Object.prototype.toString.call( data.xLineVal ) === '[object Array]' ) {
                    $scope.xExtraLegend.push(data.xLineVal[i] + " = " + data.xLineLegend[i]);
                }else {
                    $scope.xExtraLegend.push(data.xLineVal + " = " + data.xLineLegend[i]);
                }
                
            }
        }
        


        //declare and set variables
        graphOptions.xLineLabel = data.xLineLabel;
        graphOptions.yLineLabel = data.yLineLabel;
        graphOptions.xAxis = data.xAxisTitle;
        graphOptions.yAxis = data.yAxisTitle;
        graphOptions.zAxis = data.zAxisTitle;
        graphOptions.chartTitle = data.title;
        $scope.chartTitle = data.title;
        graphOptions.chartType = data.type;
        graphOptions.stackType = data.stack;
        graphOptions.xAxisCategories = data.xAxis;

        //set values if they are not null
        if(data.xAxisMax || data.xAxisMax == 0){
            graphOptions.xMax = data.xAxisMax;
        }
        if(data.xAxisMin || data.xAxisMin == 0){
            graphOptions.xMin = data.xAxisMin;
        }
        if(data.xAxisInterval || data.xAxisInterval == 0){
            graphOptions.xInterval = data.xAxisInterval;
        }
        if(data.yAxisMax || data.yAxisMax == 0){
            graphOptions.yMax = data.yAxisMax;
        }
        if(data.yAxisMin || data.yAxisMin == 0){
            graphOptions.yMin = data.yAxisMin;
        }
        if(data.yAxisInterval || data.yAxisInterval == 0){
            graphOptions.yInterval = data.yAxisInterval;
        }
        if(data.xLineVal || data.xLineVal == 0){
            graphOptions.xLineVal = data.xLineVal;
            graphOptions.xLineWidth = 1;
        }
        if(data.yLineVal || data.yLineVal == 0){
            graphOptions.yLineVal = data.yLineVal;
            graphOptions.yLineWidth = 1;
        }

        //loops through each object within dataSeries
        for (var key in data.dataSeries) {

            //check to see whether the xAxis values are already defined
            if(graphOptions.xAxisCategories){
                //checks to see if the color option is available or not and sets it appropriately
                if(data.colorSeries){
                    graphOptions.series.push({name: key, color: data.colorSeries[key], data: data.dataSeries[key]});
                } else {
                    graphOptions.series.push({name: key, data: data.dataSeries[key]});
                }
            } else {

                var series = [];

                if (graphOptions.zAxisTitle) {
                    //organize the data by z value so that the smallest points are plotted last
                    var sortedData = _.sortBy(data.dataSeries[key], function(point){
                                            return -1*point[2]; 
                                        }
                                    );
                } else {
                    var sortedData = data.dataSeries[key];
                }

                //dependent on the length of the data.dataSeries[key] array, the data needs to be set up differently
                for (var i = 0; i< sortedData.length; i++) {

                    if (sortedData[i].length == 2) {

                        //set the data series and add to series array
                        series.push({x: sortedData[i][0], y: sortedData[i][1]});
                    }
                    if (sortedData[i].length == 3 && !graphOptions.zAxisTitle) {

                        //set the data series and add to series array
                        series.push({x: sortedData[i][0], y: sortedData[i][1], name: sortedData[i][2]});
                    }
                    if (sortedData[i].length == 3 && graphOptions.zAxisTitle) {

                        //set the data series and add to series array
                        series.push({x: sortedData[i][0], y: sortedData[i][1], z: sortedData[i][2]});
                    }
                    if (sortedData[i].length == 4) {
                        
                        //set the data series and add to series array
                        series.push({x: sortedData[i][0], y: sortedData[i][1], z: sortedData[i][2], name: sortedData[i][3]});
                    }
                }

                //checks to see if the color option is available or not and sets it appropriately
                if(data.colorSeries){
                    graphOptions.series.push({name: key, color: data.colorSeries[key], data: series});
                } else {
                    graphOptions.series.push({name: key, data: series});
                }
            }
        }

        //get the median for the x and y axis
        if(graphOptions.chartType == 'scatter' || graphOptions.chartType == 'bubble') {
            //if xline is empty get the median
        	if(!data.xLineVal)
        	{
	            graphOptions.xLineVal = getMedian(sortedData, 0);
    	        graphOptions.xLineWidth = 1;
    	    }
            //if yline is empty get the median
    	    if(!data.yLineVal)
    	    {
                graphOptions.yLineVal = getMedian(sortedData, 1);
                graphOptions.yLineWidth = 2;
            }
        }
        
        $scope.createChart();
    }

    function updateChartOption(option, newValue){
        graphOptions[option] = newValue;
    }

    function getMedian(array, sortIdx) {
        var medianData = _.sortBy(array, function(point){
                                            return point[sortIdx]; 
                                        }
                                    );

        var half = Math.floor(medianData.length/2);

        if(medianData.length % 2){
            return medianData[half][sortIdx];
        } else {
            return (medianData[half-1][sortIdx] + medianData[half][sortIdx]) / 2.0;
        }
            
    }


    //creates alliance health grid chart
    $scope.createChart = function (newType) {
        if(newType){
            graphOptions.chartType = newType
        }
    	
        if(newType === 'bubble' && graphOptions.series[0].data[0].length < 3) {
            graphOptions.chartType = 'scatter';
        }
        
        function getRotationExceptionForBarchart() {
            if(graphOptions.chartType == 'column'){
                return false;
            }else{
                return -45;
            }
        }

        //creating the chart
        jQuery('#visualization').highcharts({
            chart: {
                type: graphOptions.chartType,
            },
            credits: {
                text: ''
            },
            title: {
                text: graphOptions.chartTitle
            },
            plotOptions: {
            	spline: {
                    marker: {
                        enabled: false
                    }
                },
				column: {
                stacking: graphOptions.stackType
				}
            },
            xAxis: {
                gridLineWidth: getxGridLineWidth(),
                max: graphOptions.xMax,
                min: graphOptions.xMin,
                tickInterval: graphOptions.xInterval,
                labels: {
                    rotation: getRotationExceptionForBarchart(),
                    align: 'right',
                    formatter: function(){
                        return this.value;       
                    }
                },
                title: {
                    text: graphOptions.xAxis
                },
                categories: graphOptions.xAxisCategories,
                plotLines: getXAxisPlotLines()
            },
            yAxis: {
                gridLineWidth: getyGridLineWidth(),
                max: graphOptions.yMax,
                min: graphOptions.yMin,
                tickInterval: graphOptions.yInterval,
                title: {
                    text: graphOptions.yAxis
                },
                plotLines: getYAxisPlotLines()
            },
            tooltip: getToolTip(),
            series: graphOptions.series
        });
        
    }//end of createChart()

    function getToolTip(){
        if (graphOptions.chartType == 'scatter'){
            return {
                formatter: function(){
                    var xVal = this.x.toString().substring(0,6);
                    var yVal = this.y.toString().substring(0,6);
                    var shortKey = '';
                    var brokenKeyArray = this.key.replace(/.{25}\S*\s+/g, "$&@").split(/\s+@/);
                    for (var i = 0; i<brokenKeyArray.length; i++) {
                        shortKey += '<b>' + brokenKeyArray[i] + '</b><br/>';
                    }
                    
                    return '<b>' + shortKey + '<b>' + graphOptions.xAxis + '</b>: ' + xVal + '<br/><b>' + graphOptions.yAxis + '</b>: ' + yVal + '<br/>';
                }
            };
        }else if (graphOptions.chartType == 'bubble'){
            return{
                formatter: function(){
                    var xVal = this.x.toString().substring(0,6);
                    var yVal = this.y.toString().substring(0,6);
                    var zInfo = '';
                    var shortKey = '';
                    if (graphOptions.zAxis){
                        zInfo = '<br/><b>' + graphOptions.zAxis + '</b> (radius): ' + this.point.options.z;
                    }

                    var brokenKeyArray = this.key.replace(/.{25}\S*\s+/g, "$&@").split(/\s+@/);
                    for (var i = 0; i<brokenKeyArray.length; i++) {
                        shortKey += '<b>' + brokenKeyArray[i] + '</b><br/>';
                    }
                    
                    return '<b>' + shortKey + '<b>' + graphOptions.xAxis + '</b>: ' + xVal + '<br/><b>' + graphOptions.yAxis + '</b>: ' + yVal + zInfo;
                }
            };
        } else {
            return {enabled: true};
        }
    }

    function getXAxisPlotLines(){
        if( Object.prototype.toString.call( graphOptions.xLineVal ) === '[object Array]' ) {
            var returnLines = [];
            var xLabel = '';
            for(var i=0; i<graphOptions.xLineVal.length; i++) {
                if(graphOptions.xLineLabel == "true") {
                    if(graphOptions.xMin == graphOptions.xLineVal[i] || graphOptions.xMax == graphOptions.xLineVal[i]) {
                        xLabel = '';
                    } else {
                        xLabel = graphOptions.xLineVal[i];
                    }
                }
                returnLines.push({
                    color: 'black',
                    width: graphOptions.xLineWidth,
                    value: graphOptions.xLineVal[i],
                    label: {
                        text: xLabel,
                        verticalAlign: 'bottom',
                        textAlign: 'right',
                        rotation: 0,
                        y: -5,
                        x: -2
                    }
                })
            }
            return returnLines;
        } else {
            var xLabel = '';
            if(graphOptions.xLineLabel == "true") {
                if(graphOptions.xMin == graphOptions.xLineVal || graphOptions.xMax == graphOptions.xLineVal[i]) {
                    xLabel = '';
                } else {
                    xLabel = graphOptions.xLineVal;
                }
            }
            return [{
                color: 'black',
                width: graphOptions.xLineWidth,
                value: graphOptions.xLineVal,
                label: {
                    text: xLabel,
                    verticalAlign: 'bottom',
                    textAlign: 'right',
                    rotation: 0,
                    y: -5,
                    x: -2
                }
            }];
        }        
    }

    function getYAxisPlotLines(){
        if( Object.prototype.toString.call( graphOptions.yLineVal ) === '[object Array]' ) {
            var returnLines = [];
            var yLabel = '';
            for(var i=0; i<graphOptions.yLineVal.length; i++) {
                if(graphOptions.yLineLabel == "true") {
                    if(graphOptions.yMin == graphOptions.yLineVal[i] || graphOptions.yMax == graphOptions.yLineVal[i]) {
                        yLabel = '';
                    } else {
                        yLabel = graphOptions.yLineVal[i];
                    }
                }
                returnLines.push({
                    color: 'black',
                    width: graphOptions.yLineWidth,
                    value: graphOptions.yLineVal[i],
                    label: {
                        text: yLabel,
                        verticalAlign: 'bottom',
                        textAlign: 'right',
                        y: -5
                    }
                })
            }
            return returnLines;
        } else {
            var yLabel = '';
            if(graphOptions.yLineLabel == "true") {
                if(graphOptions.yMin == graphOptions.yLineVal || graphOptions.yMax == graphOptions.yLineVal[i]) {
                    yLabel = '';
                } else {
                    yLabel = graphOptions.yLineVal;
                }
            }
            return [{
                color: 'black',
                width: graphOptions.yLineWidth,
                value: graphOptions.yLineVal,
                label: {
                        text: yLabel,
                        verticalAlign: 'bottom',
                        textAlign: 'right',
                        y: -5
                    }
            }];
        }        
    }

    function getxGridLineWidth() {
        if (graphOptions.xLineVal) {
            return 0;
        } else{
            return 1;
        } 
    }

    function getyGridLineWidth() {
        if (graphOptions.yLineVal) {
            return 0;
        } else{
            return 1;
        } 
    }
    
}

