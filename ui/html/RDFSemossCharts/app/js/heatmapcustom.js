var gridSize = 15;
var legendElementWidth = 60;
var buckets = 9;
var builderObject = {};
var valueArray = [];
var bar;
var width;
var height;

//Create the dataString global variable that is passed to both the data formation functions as well as the start function
var dataString = {};
dataString["dataSeries"] = {}; // we need this to be created so all our data can go in it
var domainArray = [0,100];

//For our actual data      
function dataBuilder (passedData) {
	var incrementalObject = jQuery.parseJSON(passedData);
	builderObject = _.extend(builderObject, incrementalObject);
}

//For our xAxisName, value etc.
function dimensionData (dataPiece, key) {
	dataString[key] = jQuery.parseJSON(dataPiece);
}

var dataRecieved = function(){};

function refreshDataFunction() {
	dataRecieved(builderObject);
}

function updateVisualizationCustom() {
	currentColor = $('#colorChooser').attr('value');

	domainArray = $('#slider').data('slider').getValue();
	
	setColorScale([ 0, buckets - 1, 100]);
	updateHeatmap(domainArray);
}

function updateHeatmap(domainArray) {
	d3.selectAll(".heat").style("fill", function(d) {
		if (d.Value >= domainArray[0] && d.Value <= domainArray[1]) {
			return colorScale(d.Value);
		} else {
			return "white";
		}
	});
	
	if(bar){
		bar.attr("fill", function(d) { return colorScale(d.Score) });
	}
	
	buildLegend("#chart svg g", 50, height+15, legendElementWidth, 20);
}

function start() {		
	var data = builderObject;
	var xAxisName = dataString.xAxisTitle;
	var yAxisName = dataString.yAxisTitle;
	var heatValue = dataString.value;
	var categoryArray = dataString.categories;
	var xAxisArray = [];
	var yAxisArray = [];
	var truncY = [];
	var truncX = [];
	var sliderArray = [0,100];
	var yAxisMatches = [];
	var margin = { top: 185, right: 150, bottom: 100, left: 150 };
	var heatMap; // we need at this level so dropdown can have access.
	var legend;

	/* Initialize tooltip */
	var tip = d3.tip()
	.attr('class', 'd3-tip')
	.html(function(d) { return "<div> <span class='light'>" + heatValue + ":</span> " + d.Value + "</div>" + "<div><span class='light'>" + xAxisName + ":</span> " + d.xAxisName + "</div>" + "<div> <span class='light'>" + yAxisName + ": </span>" + d.yAxisName + "</div>"; })

	/*----------Format Data-------------*/
	//On click of heatmap this function is called. this is just for the bar chart
	var popover = function(param) {
		var cellKey = param.Key;
		var thresh = {};
		for(var i = 0; i<categoryArray.length; i++) {
			if (document.getElementById(categoryArray[i] + "filter").value) {
				thresh[categoryArray[i]] = parseFloat(document.getElementById(categoryArray[i] + "filter").value);
			}
		}

		var chartData = java.barChartFunction( cellKey , JSON.stringify(categoryArray), JSON.stringify(thresh));

		var barData = [];
		barData.length = 0;
		barData = jQuery.parseJSON(chartData);
		barChart(barData, param.xAxisName, param.yAxisName);

	}

	/*----------Calculations-------------*/
	calculate();

	function calculate() {

		for (var key in data) {
			xAxisArray.push(data[key][xAxisName]);
			yAxisArray.push(data[key][yAxisName]);
			var round = Math.round(data[key][heatValue] * 100) / 100
			valueArray.push({"Key": key,"Value": round,"xAxis":data[key][xAxisName],"yAxis": data[key][yAxisName],"xAxisName":data[key][xAxisName],"yAxisName":data[key][yAxisName]});
		}

		var uniqueX = _.uniq(xAxisArray);
		var uniqueY = _.uniq(yAxisArray);
		xAxisArray = uniqueX.sort();
		yAxisArray = uniqueY.sort();

		/* Assign each name a number and place matrix coordinates inside of valueArray */

		//loop through data array, give every namea number. Heat values correspond to these numbers.  It's important that this comes after the sort. Same as before

		for (var i = 0; i<valueArray.length;i++) {
			for (var j = 0; j<xAxisArray.length; j++) {
				if (xAxisArray[j] == valueArray[i].xAxis) {
					valueArray[i].xAxis = j;
				}
			}
			for (var j = 0; j<yAxisArray.length; j++) {
				if (yAxisArray[j] == valueArray[i].yAxis) {
					valueArray[i].yAxis = j;
				}
			}
		};

		//truncate if labels get over a certain length.

		/* Truncate Values */
		for (var i = 0; i < yAxisArray.length; i++) {
			if (yAxisArray[i]) {
				if (yAxisArray[i].length > 20) {
					truncY.push(yAxisArray[i].substring(0, 20) + '...');
				} else {
					truncY.push(yAxisArray[i]);
				}
			}
		}

		for (var i = 0; i < xAxisArray.length; i++) {
			if (xAxisArray[i]) {
				if (xAxisArray[i].length > 30) {
					truncX.push(xAxisArray[i].substring(0, 30) + '...');
				} else {
					truncX.push(xAxisArray[i]);
				}
			}
		} 
	}

	width = xAxisArray.length * gridSize;
	height = yAxisArray.length * gridSize;

	var svg = d3.select("#chart").append("svg")
		.attr("width", width + margin.left + margin.right)
		.attr("height", height + margin.top + margin.bottom)
		.append("g")
		.attr("transform", "translate(" + margin.left + "," + margin.top + ")");

	update();
	function update() {
		width = xAxisArray.length * gridSize;
		height = yAxisArray.length * gridSize;
		
		setColorScale([ 0, buckets - 1, d3.max(valueArray, function (d) { return d.Value; })]);
//		setColorScale([ 0, buckets - 1, 100]);
		
		// Resize svg
		var sizeSvg = d3.select("#chart svg");
		sizeSvg.attr("width", width + margin.left + margin.right)
		.attr("height", height + margin.top + margin.bottom)

		yAxis = svg.selectAll(".yAxis")
		.data(truncY);
		yAxis
		.enter().append("text");
		yAxis
		.text(function (d) { return d; })
		.attr("x", 0)
		.attr("y", function (d, i) { return i * gridSize; })
		.style("text-anchor", "end")
		.attr("transform", "translate(-6," + gridSize / 1.5 + ")")
		.attr("class", "yAxis");
		yAxis.exit().remove();

		var xAxis = svg.selectAll(".xAxis")
		.data(truncX);
		xAxis
		.enter()
		.append("g");
		xAxis
		.enter()
		.append("text");
		xAxis
		.text(function(d) { return d; })
		.style("text-anchor", "start")
		.attr("x", 6)
		.attr("y", 7)
		.attr("class", "xAxis")
		.attr("transform", function(d, i) { return "translate(" + i * gridSize + ", -6)rotate(-45)" });
		xAxis.exit().remove();  

		heatMap = svg.selectAll(".heat")
		.data(valueArray);
		heatMap
		.enter().append("rect");
		heatMap
		.attr("x", function(d) { return (d.xAxis) * gridSize; })
		.attr("y", function(d) { return (d.yAxis) * gridSize; })
		.attr("class", "heat bordered")
		.attr("width", gridSize)
		.attr("height", gridSize)
		.style("fill", function(d) {
			if (d.Value >= domainArray[0] && d.Value <= domainArray[1]) {
				return colorScale(d.Value);
			} else { return "white"}
		})
		.on('mouseover', tip.show)
		.on('mouseout', tip.hide)
		.on('click', function(d){popover(d)});

		//passes data into popover. param is the label, which is the system-sys pair.

		heatMap.exit().remove();

		/* Invoke the tooltip in the context of your visualization */
		heatMap.call(tip);
		/*----------Horizontal and Vertical Lines-------------*/
		//vertical lines
		var vLine = svg.selectAll(".vline");
		vLine.remove();
		vLine = svg.selectAll(".vline")
		.data(d3.range(xAxisArray.length + 1));

		vLine.enter()
		.append("line");

		vLine
		.attr("class", "vline")
		.attr("x1", function (d) {
			return d * gridSize;
		})
		.attr("x2", function (d) {
			return d * gridSize;
		})
		.attr("y1", function (d) {
			return 0;
		})
		.attr("y2", function (d) {
			return height;
		})
		.style("stroke", "#eee");

		vLine.exit().remove();

		// horizontal lines
		var hLine = svg.selectAll(".hline");
		hLine.remove();
		hLine = svg.selectAll(".hline")
		.data(d3.range(yAxisArray.length + 1))
		hLine.enter()
		.append("line");
		hLine
		.attr("class","hline")
		.attr("y1", function (d) {
			return d * gridSize;
		})
		.attr("y2", function (d) {
			return d * gridSize;
		})
		.attr("x1", function (d) {
			return 0;
		})
		.attr("x2", function (d) {
			return width;
		})
		.style("stroke", "#eee");
		hLine.exit().remove();
	} // End of update

	initColorChooserAndAddToEndOfHtmlElementWithId("nav", updateVisualizationCustom, false);
	initSlider('slider', [0,100], updateVisualizationCustom);
	updateVisualizationCustom();

	var barW = 280;
	var barH = 190;

	var side = d3.select("#barCanvas")
	.append("svg")
	.attr("width", barW)
	.attr("height", barH);

	var cbx = d3.select("#checkboxes")
	.append("svg")
	.attr("width", barW)
	.attr("height", 180)
	.attr("class","cbxContainer");

	var ref = d3.select("#refreshBtn")
	.append("svg")
	.attr("width", 100)
	.attr("height", 50);

	var appBtn = d3.select("#appGridBtn")
	.append("svg")
	.attr("width", 100)
	.attr("height", 50);

	var selectedSystem = "";


	/*----------Checkboxes and Refresh-------------*/
	var refresh = function() { //function called when refresh button is clicked
		barChart([]);
		tip.hide();
		var refreshData = {};
		var parsedData = {};
		builderObject = {};
		data = {};

		xAxisArray.length = 0;
		yAxisArray.length = 0;
		valueArray.length = 0;
		truncY.length = 0;
		truncX.length = 0;
		var thresh = {};

		for(var i = 0; i<categoryArray.length; i++) {
			if (document.getElementById(categoryArray[i] + "filter").value) {
				thresh[categoryArray[i]] = parseFloat(document.getElementById(categoryArray[i] + "filter").value);
			}
		}

		java.refreshFunction(JSON.stringify(categoryArray), JSON.stringify(thresh))
	} //end of refresh function

	dataRecieved = function(refreshData) {
		data = refreshData;
		calculate();
		update();
	}

	//refresh button
	var refBtn = ref.selectAll("foreignObject")
	.data([1])
	.enter()
	.append("foreignObject")
	.attr("y", 0)
	.attr("x", 0)
	.attr("width", "140px")
	.attr("height", "60px")
	.append("xhtml:div")
	.html("<a>Refresh</a>")
	.attr("class","btn btn-success");
	refBtn.on("click", function(d){ refresh()});

//		this is where they select the checkboxes. When they click refresh, you only look at the checkboxes that were checked

	//On click of checkbox
	var checkbox = function(param){
		if(document.getElementById(param).checked) {
			categoryArray.push(param);
			document.getElementById(param + "filter").disabled = false;
		}else{
			categoryArray = _.without(categoryArray, param);
			document.getElementById(param + "filter").disabled = true;
			document.getElementById(param + "filter").value = "";
		}
	}

	cbx.selectAll("foreignObject.cbx")
	.data(categoryArray)
	.enter()
	.append("foreignObject")
	.attr("class","cbx")
	.attr("x", function (d,i) { return 0 })
	.attr("y",  function (d,i) { return i*25 })
	.attr("width", "13px")
	.attr("height", "18px")
	.append("xhtml:div")
	.html(function(d){ return ("<input type=checkbox checked='checked' class='cbx' id=" + d + "></input>")}) // give each element a unique id
	.on("click", function(d){checkbox(d);});

	cbx.selectAll("text.cbxLabels")
	.data(categoryArray)
	.enter()
	.append("text")
	.text(function(d){return d})
	.attr("class","cbxLabels")
	.attr("y", function(d,i){return ((i * 25) + 16)})
	.attr("x", 20);

	cbx.selectAll("foreignObject.filter")
	.data(categoryArray)
	.enter()
	.append("foreignObject")
	.attr("class","filter")
	.attr("class","cbx")
	.attr("x", function (d,i) { return 250 })
	.attr("y",  function (d,i) { return i*25 })
	.attr("width", "30px")
	.attr("height", "30px")
	.append("xhtml:div")
	.html(function(d){ return ("<input type=text class='filter input-sm' id=" + d + 'filter' + "></input>")}) // give each element a unique id
	;


	//popover generates data. barChart displays it.
	/*----------Bar Chart--------------*/
	var barChart = function(data, system1Name, system2Name){
		var barPadding = 1;
		var barHeight = 20;
		var barSpacing = barHeight + barPadding;

		bar = side.selectAll("rect.bar")
		.data(data);
		bar
		.enter()
		.append("rect");
		bar
		.attr("class","bar")
		.attr("y",function(d,i){return 40 + (i*(barHeight + barPadding))})
		.attr("x", 177)
		.attr("height", barHeight)
		.attr("fill", function(d) { return colorScale(d.Score) });
		bar.transition()
		.duration(600)
		.attr("width", function(d){ return d.Score * .8});
		bar.exit().remove();

		var barLabel = side.selectAll("text.labels")
		.data(data);
		barLabel
		.enter()
		.append("text");
		barLabel
		.text(function(d){return d.key})
		.attr("text-anchor", "end")
		.style("font-size", "9px")
		.attr("class","labels")
		.attr("y", function(d,i){return (i * barSpacing) + 52})
		.attr("x", 170);
		barLabel.exit().remove();

		var barValue = side.selectAll("text#barValue")
		.data(data);
		barValue
		.enter()
		.append("text");


		barValue
		.attr("text-anchor", "start")
		.attr("id","barValue");

		barValue.attr("class",function(d){
			if (d.Score > 30){
				return "light"
			} else{return "dark"}
		})
		.attr("y", function(d,i){return (i * barSpacing) + 55})
		.attr("x", 180)
		.text(function(d){return Math.round(d.Score * 100)/100});

		barValue.exit().remove();

		var system1 = side.selectAll("text.system1")
		.data(data);
		system1
		.enter()
		.append("text");
		system1
		.text(function(d){return xAxisName + ": " + system1Name})
		.attr("class","system1")
		.attr("text-anchor", "start")
		.attr("y", 10)
		.attr("x", 0);
		system1.exit().remove();
		var system2 = side.selectAll("text.system2")
		.data(data);
		system2
		.enter()
		.append("text");
		system2
		.text(function(d){return yAxisName + ": " + system2Name})
		.attr("class","system2")
		.attr("text-anchor", "start")
		.attr("y", 25)
		.attr("x", 0);
		system2.exit().remove();

	} //End bar chart function

	function systemSelection() {
		selectedSystem = this.options[this.selectedIndex].value
	}

	if (dataString.sysDup) {
		$("#header2").show();
		var dropDownArray = xAxisArray;
		dropDownArray.unshift("");

		/*----------App health grid dropdown-------------*/ 
		d3.select("#appGridDropDown").append("select")
		.attr("id", "select2DropDown2")
		.attr("class", "mySelect2v2")
		.selectAll("option")
		.data(dropDownArray)
		.enter()
		.append("option")
		.attr("value", function(d){ return d; }) /* This gives me the value */
		.text(function(d){ return d})
		;

		$("#select2DropDown2").select2({
			placeholder: "Select a System"
		});
		$("#select2DropDown2").on("change", systemSelection);

		var appGridBtn = appBtn.selectAll("foreignObject")
		.data([1])
		.enter()
		.append("foreignObject")
		.attr("y", 0)
		.attr("x", 0)
		.attr("width", "140px")
		.attr("height", "60px")
		.append("xhtml:div")
		.html("<a>Launch</a>")
		.attr("class","btn btn-success");


		appGridBtn.on("click", function(d){
			yAxisMatches = [];
			for(i=0; i<valueArray.length; i++) {
				if(i==0){
					yAxisMatches.push(selectedSystem)
				}
				if(valueArray[i].xAxisName === selectedSystem && valueArray[i].Value >= sliderArray[0] && valueArray[i].Value <= sliderArray[1] && valueArray[i].xAxisName !== valueArray[i].yAxisName)
				{
					yAxisMatches.push(valueArray[i].yAxisName)
				}
			}

			var myJsonString = JSON.stringify(yAxisMatches);

			if(selectedSystem != ""){
				healthGrid(myJsonString);
			}
		});
	} else {
		$("#header2").hide();
	}
}

function runOutsideApp() {
	dataBuilder('{"LA7-DS":{"App2":"DS","Score":34.769230769230774,"App1":"LA7"},"PX-NDF":{"App2":"NDF","Score":29.523809523809522,"App1":"PX"},"PSS-TIU":{"App2":"TIU","Score":42.09090909090909,"App1":"PSS"},"PSIV-OR":{"App2":"OR","Score":42.45454545454545,"App1":"PSIV"},"TIUI-BCMA":{"App2":"BCMA","Score":55.0,"App1":"TIUI"},"PSU-GMTS":{"App2":"GMTS","Score":32.183908045977006,"App1":"PSU"},"TIU-LA7":{"App2":"LA7","Score":33.93939393939394,"App1":"TIU"},"VBECS-GMRV":{"App2":"GMRV","Score":33.33333333333333,"App1":"VBECS"},"PSD-PXRM":{"App2":"PXRM","Score":49.285714285714285,"App1":"PSD"},"PXRM-MAG":{"App2":"MAG","Score":30.723981900452486,"App1":"PXRM"},"DS-NDF":{"App2":"NDF","Score":23.81818181818182,"App1":"DS"},"GMRV-LREPI":{"App2":"LREPI","Score":40.66666666666667,"App1":"GMRV"},"GMTS-BCMA":{"App2":"BCMA","Score":50.66666666666667,"App1":"GMTS"},"PSU-PSX":{"App2":"PSX","Score":22.75862068965517,"App1":"PSU"},"MAG-BCMA":{"App2":"BCMA","Score":35.23809523809524,"App1":"MAG"},"LREPI-MAG":{"App2":"MAG","Score":39.047619047619044,"App1":"LREPI"},"GMPL-TIUI":{"App2":"TIUI","Score":41.666666666666664,"App1":"GMPL"},"PSU-PSS":{"App2":"PSS","Score":33.56321839080459,"App1":"PSU"},"NDF-BCMA":{"App2":"BCMA","Score":37.0,"App1":"NDF"},"PSS-TIUI":{"App2":"TIUI","Score":36.27272727272727,"App1":"PSS"},"VBECS-GMRA":{"App2":"GMRA","Score":34.44444444444444,"App1":"VBECS"},"MAG-PSX":{"App2":"PSX","Score":22.857142857142858,"App1":"MAG"},"PSU-PSO":{"App2":"PSO","Score":39.770114942528735,"App1":"PSU"},"LREPI-PSGW":{"App2":"PSGW","Score":31.26984126984127,"App1":"LREPI"},"USR-MAG":{"App2":"MAG","Score":53.33333333333333,"App1":"USR"},"PSIV-GMPL":{"App2":"GMPL","Score":26.727272727272727,"App1":"PSIV"},"MAG-PSU":{"App2":"PSU","Score":37.142857142857146,"App1":"MAG"},"MAG-PSS":{"App2":"PSS","Score":40.0,"App1":"MAG"},"PSU-PSJ":{"App2":"PSJ","Score":36.20689655172414,"App1":"PSU"},"OR-GMTS":{"App2":"GMTS","Score":40.63492063492063,"App1":"OR"},"ML-GMPL":{"App2":"GMPL","Score":34.714285714285715,"App1":"ML"},"GMTS-NDF":{"App2":"NDF","Score":25.0,"App1":"GMTS"},"MAG-PSO":{"App2":"PSO","Score":40.47619047619048,"App1":"MAG"},"PSU-PSD":{"App2":"PSD","Score":27.47126436781609,"App1":"PSU"},"USR-ML":{"App2":"ML","Score":46.666666666666664,"App1":"USR"},"OR-ML":{"App2":"ML","Score":51.52092352092352,"App1":"OR"},"MAG-PSJ":{"App2":"PSJ","Score":35.714285714285715,"App1":"MAG"},"PSU-PSA":{"App2":"PSA","Score":29.42528735632184,"App1":"PSU"},"NDF-MAG":{"App2":"MAG","Score":42.0,"App1":"NDF"},"PSJ-DS":{"App2":"DS","Score":33.15789473684211,"App1":"PSJ"},"MAG-PSD":{"App2":"PSD","Score":33.80952380952381,"App1":"MAG"},"MAG-PSA":{"App2":"PSA","Score":35.23809523809524,"App1":"MAG"},"TIUI-PXRM":{"App2":"PXRM","Score":56.0,"App1":"TIUI"},"PSS-IBD":{"App2":"IBD","Score":34.45454545454545,"App1":"PSS"},"BCMA-DS":{"App2":"DS","Score":36.25,"App1":"BCMA"},"BCMA-MAG":{"App2":"MAG","Score":36.25,"App1":"BCMA"},"USR-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":33.33333333333333,"App1":"USR"},"VBECS-MAG":{"App2":"MAG","Score":36.666666666666664,"App1":"VBECS"},"PSU-GMRV":{"App2":"GMRV","Score":30.229885057471265,"App1":"PSU"},"PSIV-LREPI":{"App2":"LREPI","Score":33.63636363636364,"App1":"PSIV"},"GMPL-LRAP":{"App2":"LRAP","Score":41.111111111111114,"App1":"GMPL"},"PSIV-ML":{"App2":"ML","Score":49.18181818181819,"App1":"PSIV"},"PSJ-GMTS":{"App2":"GMTS","Score":32.10526315789474,"App1":"PSJ"},"USR-LR":{"App2":"LR","Score":36.666666666666664,"App1":"USR"},"PSS-LRAP":{"App2":"LRAP","Score":44.72727272727273,"App1":"PSS"},"OR-LR":{"App2":"LR","Score":27.77777777777778,"App1":"OR"},"GMTS-PXRM":{"App2":"PXRM","Score":55.83333333333333,"App1":"GMTS"},"MAG-PXRM":{"App2":"PXRM","Score":39.04761904761905,"App1":"MAG"},"DS-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":23.636363636363637,"App1":"DS"},"PSA-PSIV":{"App2":"PSIV","Score":55.0,"App1":"PSA"},"NDF-PXRM":{"App2":"PXRM","Score":50.0,"App1":"NDF"},"IBD-TIUI":{"App2":"TIUI","Score":52.5,"App1":"IBD"},"VBECS-GMPL":{"App2":"GMPL","Score":40.0,"App1":"VBECS"},"TIUI-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":28.0,"App1":"TIUI"},"GMRV-PSIV":{"App2":"PSIV","Score":53.81818181818182,"App1":"GMRV"},"PSU-GMRA":{"App2":"GMRA","Score":40.229885057471265,"App1":"PSU"},"GMRV-TIU":{"App2":"TIU","Score":46.20295983086681,"App1":"GMRV"},"PSX-PSIV":{"App2":"PSIV","Score":40.0,"App1":"PSX"},"PSIV-LR":{"App2":"LR","Score":23.90909090909091,"App1":"PSIV"},"IBD-PSX":{"App2":"PSX","Score":20.0,"App1":"IBD"},"PSIV-TIU":{"App2":"TIU","Score":29.727272727272727,"App1":"PSIV"},"OR-GMRV":{"App2":"GMRV","Score":50.7936507936508,"App1":"OR"},"IBD-PSU":{"App2":"PSU","Score":32.5,"App1":"IBD"},"IBD-PSS":{"App2":"PSS","Score":42.5,"App1":"IBD"},"PXRM-GMTS":{"App2":"GMTS","Score":38.05018510900864,"App1":"PXRM"},"GMPL-VBECS":{"App2":"VBECS","Score":40.0,"App1":"GMPL"},"IBD-PSO":{"App2":"PSO","Score":37.5,"App1":"IBD"},"OR-USR":{"App2":"USR","Score":31.11111111111111,"App1":"OR"},"IBD-PSJ":{"App2":"PSJ","Score":35.0,"App1":"IBD"},"LRAP-TIUI":{"App2":"TIUI","Score":27.936507936507937,"App1":"LRAP"},"TIU-PX":{"App2":"PX","Score":44.17490118577075,"App1":"TIU"},"IBD-PSD":{"App2":"PSD","Score":37.5,"App1":"IBD"},"OR-GMRA":{"App2":"GMRA","Score":54.6031746031746,"App1":"OR"},"IBD-PSA":{"App2":"PSA","Score":37.5,"App1":"IBD"},"PSX-PX":{"App2":"PX","Score":30.0,"App1":"PSX"},"IBD-LREPI":{"App2":"LREPI","Score":40.0,"App1":"IBD"},"PSD-LREPI":{"App2":"LREPI","Score":38.57142857142857,"App1":"PSD"},"NLT \u0026 LOINC-PX":{"App2":"PX","Score":22.857142857142858,"App1":"NLT \u0026 LOINC"},"IBD-LRAP":{"App2":"LRAP","Score":37.5,"App1":"IBD"},"PSJ-PSX":{"App2":"PSX","Score":26.05263157894737,"App1":"PSJ"},"PSJ-PSU":{"App2":"PSU","Score":38.15789473684211,"App1":"PSJ"},"PSJ-GMRV":{"App2":"GMRV","Score":39.60526315789474,"App1":"PSJ"},"USR-LREPI":{"App2":"LREPI","Score":33.33333333333333,"App1":"USR"},"GMPL-PX":{"App2":"PX","Score":40.55555555555556,"App1":"GMPL"},"PSJ-PSS":{"App2":"PSS","Score":42.54385964912281,"App1":"PSJ"},"OR-LA7":{"App2":"LA7","Score":42.22222222222222,"App1":"OR"},"PSJ-PSO":{"App2":"PSO","Score":48.71517027863777,"App1":"PSJ"},"LRAP-NDF":{"App2":"NDF","Score":27.3015873015873,"App1":"LRAP"},"PSU-GMPL":{"App2":"GMPL","Score":27.47126436781609,"App1":"PSU"},"PSX-PSU":{"App2":"PSU","Score":40.0,"App1":"PSX"},"PSX-PSS":{"App2":"PSS","Score":35.0,"App1":"PSX"},"PSA-PSGW":{"App2":"PSGW","Score":48.0,"App1":"PSA"},"PSX-PSO":{"App2":"PSO","Score":40.0,"App1":"PSX"},"PSJ-PSD":{"App2":"PSD","Score":30.87719298245614,"App1":"PSJ"},"GMRV-PSGW":{"App2":"PSGW","Score":34.0,"App1":"GMRV"},"PSJ-PSA":{"App2":"PSA","Score":29.210526315789473,"App1":"PSJ"},"GMRV-IBD":{"App2":"IBD","Score":35.81818181818182,"App1":"GMRV"},"PSJ-GMRA":{"App2":"GMRA","Score":44.64912280701754,"App1":"PSJ"},"PSX-PSJ":{"App2":"PSJ","Score":55.0,"App1":"PSX"},"PSGW-DS":{"App2":"DS","Score":34.0,"App1":"PSGW"},"TIU-OR":{"App2":"OR","Score":40.34090909090909,"App1":"TIU"},"PSX-PSGW":{"App2":"PSGW","Score":35.0,"App1":"PSX"},"GMRA-MAG":{"App2":"MAG","Score":40.0,"App1":"GMRA"},"PSX-PSD":{"App2":"PSD","Score":30.0,"App1":"PSX"},"PSX-OR":{"App2":"OR","Score":35.0,"App1":"PSX"},"PSIV-IBD":{"App2":"IBD","Score":23.90909090909091,"App1":"PSIV"},"PSX-PSA":{"App2":"PSA","Score":25.0,"App1":"PSX"},"NLT \u0026 LOINC-OR":{"App2":"OR","Score":54.285714285714285,"App1":"NLT \u0026 LOINC"},"PXRM-GMRV":{"App2":"GMRV","Score":36.17852735499794,"App1":"PXRM"},"GMPL-OR":{"App2":"OR","Score":41.666666666666664,"App1":"GMPL"},"OR-GMPL":{"App2":"GMPL","Score":39.523809523809526,"App1":"OR"},"ML-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":30.57142857142857,"App1":"ML"},"PSGW-NDF":{"App2":"NDF","Score":38.0,"App1":"PSGW"},"IBD-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":25.0,"App1":"IBD"},"PSA-MAG":{"App2":"MAG","Score":46.0,"App1":"PSA"},"TIU-PSX":{"App2":"PSX","Score":21.81818181818182,"App1":"TIU"},"PSS-PX":{"App2":"PX","Score":44.27272727272727,"App1":"PSS"},"TIU-PSU":{"App2":"PSU","Score":39.128787878787875,"App1":"TIU"},"PX-DS":{"App2":"DS","Score":46.3003663003663,"App1":"PX"},"TIU-PSS":{"App2":"PSS","Score":37.00757575757576,"App1":"TIU"},"PSJ-VBECS":{"App2":"VBECS","Score":31.05263157894737,"App1":"PSJ"},"PXRM-GMRA":{"App2":"GMRA","Score":38.07897984368573,"App1":"PXRM"},"TIU-PSO":{"App2":"PSO","Score":44.54545454545455,"App1":"TIU"},"TIUI-USR":{"App2":"USR","Score":30.0,"App1":"TIUI"},"PSO-MAG":{"App2":"MAG","Score":32.85714285714286,"App1":"PSO"},"PSU-LREPI":{"App2":"LREPI","Score":31.60919540229885,"App1":"PSU"},"TIU-PSJ":{"App2":"PSJ","Score":42.42424242424242,"App1":"TIU"},"NDF-DS":{"App2":"DS","Score":33.0,"App1":"NDF"},"PSIV-TIUI":{"App2":"TIUI","Score":24.81818181818182,"App1":"PSIV"},"TIU-PSD":{"App2":"PSD","Score":30.303030303030305,"App1":"TIU"},"ML-MAG":{"App2":"MAG","Score":40.285714285714285,"App1":"ML"},"ML-TIUI":{"App2":"TIUI","Score":34.714285714285715,"App1":"ML"},"PX-TIU":{"App2":"TIU","Score":50.227106227106226,"App1":"PX"},"PSJ-GMPL":{"App2":"GMPL","Score":26.49122807017544,"App1":"PSJ"},"TIU-PSA":{"App2":"PSA","Score":28.484848484848484,"App1":"TIU"},"MAG-LREPI":{"App2":"LREPI","Score":38.095238095238095,"App1":"MAG"},"LR-DS":{"App2":"DS","Score":56.0,"App1":"LR"},"LA7-PSIV":{"App2":"PSIV","Score":45.230769230769226,"App1":"LA7"},"TIUI-LA7":{"App2":"LA7","Score":41.0,"App1":"TIUI"},"PSS-OR":{"App2":"OR","Score":51.09090909090909,"App1":"PSS"},"VBECS-LREPI":{"App2":"LREPI","Score":32.22222222222222,"App1":"VBECS"},"TIU-ML":{"App2":"ML","Score":34.88636363636364,"App1":"TIU"},"DS-TIU":{"App2":"TIU","Score":58.02597402597402,"App1":"DS"},"PSX-ML":{"App2":"ML","Score":35.0,"App1":"PSX"},"NLT \u0026 LOINC-ML":{"App2":"ML","Score":48.57142857142857,"App1":"NLT \u0026 LOINC"},"LRAP-LREPI":{"App2":"LREPI","Score":43.96825396825397,"App1":"LRAP"},"GMPL-ML":{"App2":"ML","Score":36.111111111111114,"App1":"GMPL"},"PXRM-GMPL":{"App2":"GMPL","Score":31.501439736733857,"App1":"PXRM"},"PSIV-LRAP":{"App2":"LRAP","Score":32.54545454545455,"App1":"PSIV"},"ML-LRAP":{"App2":"LRAP","Score":41.42857142857143,"App1":"ML"},"VBECS-TIUI":{"App2":"TIUI","Score":30.0,"App1":"VBECS"},"TIU-LR":{"App2":"LR","Score":32.259552042160735,"App1":"TIU"},"GMTS-TIU":{"App2":"TIU","Score":58.212643678160916,"App1":"GMTS"},"LA7-NDF":{"App2":"NDF","Score":29.53846153846154,"App1":"LA7"},"USR-BCMA":{"App2":"BCMA","Score":46.666666666666664,"App1":"USR"},"TIU-VBECS":{"App2":"VBECS","Score":32.42424242424242,"App1":"TIU"},"PSX-LR":{"App2":"LR","Score":45.0,"App1":"PSX"},"NLT \u0026 LOINC-LR":{"App2":"LR","Score":35.714285714285715,"App1":"NLT \u0026 LOINC"},"LREPI-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":28.444444444444443,"App1":"LREPI"},"PX-IBD":{"App2":"IBD","Score":37.10622710622711,"App1":"PX"},"GMPL-LR":{"App2":"LR","Score":25.555555555555557,"App1":"GMPL"},"PSA-VBECS":{"App2":"VBECS","Score":34.0,"App1":"PSA"},"PSS-USR":{"App2":"USR","Score":29.81818181818182,"App1":"PSS"},"GMPL-NDF":{"App2":"NDF","Score":23.333333333333332,"App1":"GMPL"},"LA7-PSGW":{"App2":"PSGW","Score":32.61538461538461,"App1":"LA7"},"DS-IBD":{"App2":"IBD","Score":26.545454545454547,"App1":"DS"},"VBECS-LRAP":{"App2":"LRAP","Score":41.111111111111114,"App1":"VBECS"},"PSS-ML":{"App2":"ML","Score":60.09090909090909,"App1":"PSS"},"LR-MAG":{"App2":"MAG","Score":56.0,"App1":"LR"},"PSD-MAG":{"App2":"MAG","Score":46.42857142857143,"App1":"PSD"},"PSS-LA7":{"App2":"LA7","Score":44.72727272727273,"App1":"PSS"},"LREPI-GMTS":{"App2":"GMTS","Score":32.38095238095238,"App1":"LREPI"},"PSU-TIUI":{"App2":"TIUI","Score":26.091954022988503,"App1":"PSU"},"USR-PXRM":{"App2":"PXRM","Score":40.0,"App1":"USR"},"OR-PSX":{"App2":"PSX","Score":26.666666666666664,"App1":"OR"},"PX-PSIV":{"App2":"PSIV","Score":45.714285714285715,"App1":"PX"},"OR-PSU":{"App2":"PSU","Score":38.888888888888886,"App1":"OR"},"GMTS-IBD":{"App2":"IBD","Score":39.0,"App1":"GMTS"},"OR-PSS":{"App2":"PSS","Score":46.82539682539682,"App1":"OR"},"NLT \u0026 LOINC-LREPI":{"App2":"LREPI","Score":41.42857142857143,"App1":"NLT \u0026 LOINC"},"NLT \u0026 LOINC-NDF":{"App2":"NDF","Score":25.714285714285715,"App1":"NLT \u0026 LOINC"},"PSS-LR":{"App2":"LR","Score":31.272727272727273,"App1":"PSS"},"OR-PSO":{"App2":"PSO","Score":55.55555555555556,"App1":"OR"},"DS-BCMA":{"App2":"BCMA","Score":32.18181818181819,"App1":"DS"},"GMRA-VBECS":{"App2":"VBECS","Score":32.85714285714286,"App1":"GMRA"},"OR-PSJ":{"App2":"PSJ","Score":65.39682539682539,"App1":"OR"},"USR-DS":{"App2":"DS","Score":33.33333333333333,"App1":"USR"},"OR-DS":{"App2":"DS","Score":36.666666666666664,"App1":"OR"},"NLT \u0026 LOINC-BCMA":{"App2":"BCMA","Score":57.14285714285714,"App1":"NLT \u0026 LOINC"},"OR-PSD":{"App2":"PSD","Score":38.888888888888886,"App1":"OR"},"LR-BCMA":{"App2":"BCMA","Score":45.33333333333333,"App1":"LR"},"OR-PSA":{"App2":"PSA","Score":34.44444444444444,"App1":"OR"},"LR-LREPI":{"App2":"LREPI","Score":32.0,"App1":"LR"},"OR-TIUI":{"App2":"TIUI","Score":36.19047619047619,"App1":"OR"},"PSU-LRAP":{"App2":"LRAP","Score":30.229885057471265,"App1":"PSU"},"PSIV-DS":{"App2":"DS","Score":31.727272727272727,"App1":"PSIV"},"LRAP-TIU":{"App2":"TIU","Score":39.682539682539684,"App1":"LRAP"},"GMRA-PSIV":{"App2":"PSIV","Score":45.714285714285715,"App1":"GMRA"},"PSIV-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":26.90909090909091,"App1":"PSIV"},"LREPI-GMRV":{"App2":"GMRV","Score":31.746031746031747,"App1":"LREPI"},"GMRV-USR":{"App2":"USR","Score":32.66666666666667,"App1":"GMRV"},"PSJ-TIUI":{"App2":"TIUI","Score":28.771929824561404,"App1":"PSJ"},"PSD-PX":{"App2":"PX","Score":40.714285714285715,"App1":"PSD"},"BCMA-PSIV":{"App2":"PSIV","Score":56.25,"App1":"BCMA"},"PX-PSGW":{"App2":"PSGW","Score":28.095238095238095,"App1":"PX"},"PSIV-USR":{"App2":"USR","Score":23.81818181818182,"App1":"PSIV"},"DS-PXRM":{"App2":"PXRM","Score":43.45454545454545,"App1":"DS"},"OR-LRAP":{"App2":"LRAP","Score":45.55555555555556,"App1":"OR"},"LREPI-GMRA":{"App2":"GMRA","Score":34.12698412698413,"App1":"LREPI"},"GMTS-LREPI":{"App2":"LREPI","Score":38.0,"App1":"GMTS"},"NLT \u0026 LOINC-PXRM":{"App2":"PXRM","Score":37.14285714285714,"App1":"NLT \u0026 LOINC"},"LR-PXRM":{"App2":"PXRM","Score":56.666666666666664,"App1":"LR"},"PSGW-TIU":{"App2":"TIU","Score":34.5,"App1":"PSGW"},"GMRV-LA7":{"App2":"LA7","Score":49.81818181818182,"App1":"GMRV"},"PX-VBECS":{"App2":"VBECS","Score":35.23809523809524,"App1":"PX"},"TIUI-PSX":{"App2":"PSX","Score":24.0,"App1":"TIUI"},"TIUI-PSU":{"App2":"PSU","Score":41.0,"App1":"TIUI"},"TIUI-PSS":{"App2":"PSS","Score":43.0,"App1":"TIUI"},"PSIV-LA7":{"App2":"LA7","Score":29.636363636363637,"App1":"PSIV"},"PSGW-LREPI":{"App2":"LREPI","Score":40.5,"App1":"PSGW"},"TIUI-PSO":{"App2":"PSO","Score":50.0,"App1":"TIUI"},"PXRM-TIUI":{"App2":"TIUI","Score":34.520773344302754,"App1":"PXRM"},"TIUI-PSJ":{"App2":"PSJ","Score":46.0,"App1":"TIUI"},"PSGW-PSIV":{"App2":"PSIV","Score":60.0,"App1":"PSGW"},"PSD-OR":{"App2":"OR","Score":46.42857142857143,"App1":"PSD"},"PSA-GMTS":{"App2":"GMTS","Score":38.0,"App1":"PSA"},"LRAP-IBD":{"App2":"IBD","Score":26.507936507936506,"App1":"LRAP"},"TIUI-PSD":{"App2":"PSD","Score":29.0,"App1":"TIUI"},"PSJ-LRAP":{"App2":"LRAP","Score":33.59649122807018,"App1":"PSJ"},"GMRV-GMTS":{"App2":"GMTS","Score":56.121212121212125,"App1":"GMRV"},"GMRA-PSGW":{"App2":"PSGW","Score":36.285714285714285,"App1":"GMRA"},"TIUI-PSA":{"App2":"PSA","Score":29.0,"App1":"TIUI"},"GMPL-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":22.22222222222222,"App1":"GMPL"},"PSX-GMTS":{"App2":"GMTS","Score":25.0,"App1":"PSX"},"PSU-MAG":{"App2":"MAG","Score":34.25287356321839,"App1":"PSU"},"PSO-PSIV":{"App2":"PSIV","Score":59.12925170068027,"App1":"PSO"},"LREPI-GMPL":{"App2":"GMPL","Score":36.34920634920635,"App1":"LREPI"},"BCMA-PSGW":{"App2":"PSGW","Score":30.0,"App1":"BCMA"},"GMPL-BCMA":{"App2":"BCMA","Score":41.666666666666664,"App1":"GMPL"},"PSS-BCMA":{"App2":"BCMA","Score":61.54545454545455,"App1":"PSS"},"PXRM-LRAP":{"App2":"LRAP","Score":28.959276018099548,"App1":"PXRM"},"TIU-PSIV":{"App2":"PSIV","Score":42.72727272727273,"App1":"TIU"},"GMRA-PX":{"App2":"PX","Score":42.77351916376307,"App1":"GMRA"},"PSGW-IBD":{"App2":"IBD","Score":22.5,"App1":"PSGW"},"LA7-TIU":{"App2":"TIU","Score":34.15384615384615,"App1":"LA7"},"PSA-GMRV":{"App2":"GMRV","Score":38.0,"App1":"PSA"},"PSS-PSX":{"App2":"PSX","Score":25.454545454545453,"App1":"PSS"},"PSU-PX":{"App2":"PX","Score":28.850574712643677,"App1":"PSU"},"PXRM-PX":{"App2":"PX","Score":41.56668738020427,"App1":"PXRM"},"PSS-PSU":{"App2":"PSU","Score":46.18181818181818,"App1":"PSS"},"PSD-ML":{"App2":"ML","Score":51.42857142857143,"App1":"PSD"},"PX-USR":{"App2":"USR","Score":26.666666666666664,"App1":"PX"},"PSX-GMRV":{"App2":"GMRV","Score":35.0,"App1":"PSX"},"PSS-PSO":{"App2":"PSO","Score":57.36363636363637,"App1":"PSS"},"GMPL-TIU":{"App2":"TIU","Score":43.888888888888886,"App1":"GMPL"},"PSS-PSJ":{"App2":"PSJ","Score":60.54545454545455,"App1":"PSS"},"GMRA-OR":{"App2":"OR","Score":45.42857142857143,"App1":"GMRA"},"PSA-GMRA":{"App2":"GMRA","Score":56.0,"App1":"PSA"},"MAG-PX":{"App2":"PX","Score":30.476190476190474,"App1":"MAG"},"PSS-PSD":{"App2":"PSD","Score":45.09090909090909,"App1":"PSS"},"GMRV-GMRA":{"App2":"GMRA","Score":59.75757575757576,"App1":"GMRV"},"PSO-PSGW":{"App2":"PSGW","Score":37.61904761904762,"App1":"PSO"},"PSD-PSIV":{"App2":"PSIV","Score":54.285714285714285,"App1":"PSD"},"PSS-PSA":{"App2":"PSA","Score":45.45454545454545,"App1":"PSS"},"IBD-MAG":{"App2":"MAG","Score":42.5,"App1":"IBD"},"GMPL-PXRM":{"App2":"PXRM","Score":51.666666666666664,"App1":"GMPL"},"PSS-PXRM":{"App2":"PXRM","Score":57.0,"App1":"PSS"},"IBD-BCMA":{"App2":"BCMA","Score":47.5,"App1":"IBD"},"DS-USR":{"App2":"USR","Score":22.90909090909091,"App1":"DS"},"PSD-LR":{"App2":"LR","Score":27.857142857142858,"App1":"PSD"},"PSX-GMRA":{"App2":"GMRA","Score":40.0,"App1":"PSX"},"PX-LA7":{"App2":"LA7","Score":37.61904761904762,"App1":"PX"},"TIU-DS":{"App2":"DS","Score":41.81488801054019,"App1":"TIU"},"PSX-DS":{"App2":"DS","Score":35.0,"App1":"PSX"},"TIU-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":26.704545454545453,"App1":"TIU"},"TIU-PSGW":{"App2":"PSGW","Score":31.81818181818182,"App1":"TIU"},"PXRM-NDF":{"App2":"NDF","Score":26.018099547511312,"App1":"PXRM"},"NLT \u0026 LOINC-DS":{"App2":"DS","Score":31.428571428571427,"App1":"NLT \u0026 LOINC"},"PXRM-OR":{"App2":"OR","Score":32.0896750308515,"App1":"PXRM"},"PSU-OR":{"App2":"OR","Score":28.160919540229884,"App1":"PSU"},"LA7-LREPI":{"App2":"LREPI","Score":37.230769230769226,"App1":"LA7"},"GMPL-DS":{"App2":"DS","Score":45.55555555555556,"App1":"GMPL"},"LREPI-NDF":{"App2":"NDF","Score":25.07936507936508,"App1":"LREPI"},"LA7-IBD":{"App2":"IBD","Score":27.076923076923077,"App1":"LA7"},"GMTS-USR":{"App2":"USR","Score":25.0,"App1":"GMTS"},"PSJ-MAG":{"App2":"MAG","Score":33.15789473684211,"App1":"PSJ"},"NLT \u0026 LOINC-TIU":{"App2":"TIU","Score":38.57142857142857,"App1":"NLT \u0026 LOINC"},"DS-LA7":{"App2":"LA7","Score":28.363636363636363,"App1":"DS"},"MAG-OR":{"App2":"OR","Score":36.666666666666664,"App1":"MAG"},"USR-NDF":{"App2":"NDF","Score":46.666666666666664,"App1":"USR"},"LRAP-BCMA":{"App2":"BCMA","Score":34.44444444444444,"App1":"LRAP"},"PSX-MAG":{"App2":"MAG","Score":35.0,"App1":"PSX"},"LA7-GMTS":{"App2":"GMTS","Score":37.230769230769226,"App1":"LA7"},"PSA-GMPL":{"App2":"GMPL","Score":43.0,"App1":"PSA"},"TIUI-PSIV":{"App2":"PSIV","Score":42.0,"App1":"TIUI"},"GMPL-IBD":{"App2":"IBD","Score":36.111111111111114,"App1":"GMPL"},"GMRV-GMPL":{"App2":"GMPL","Score":48.484848484848484,"App1":"GMRV"},"GMTS-LA7":{"App2":"LA7","Score":38.0,"App1":"GMTS"},"LRAP-PX":{"App2":"PX","Score":30.158730158730158,"App1":"LRAP"},"PSX-GMPL":{"App2":"GMPL","Score":25.0,"App1":"PSX"},"IBD-PXRM":{"App2":"PXRM","Score":50.0,"App1":"IBD"},"ML-LREPI":{"App2":"LREPI","Score":35.42857142857143,"App1":"ML"},"GMRA-ML":{"App2":"ML","Score":40.57142857142857,"App1":"GMRA"},"PSD-PSGW":{"App2":"PSGW","Score":38.57142857142857,"App1":"PSD"},"GMTS-PSIV":{"App2":"PSIV","Score":39.33333333333333,"App1":"GMTS"},"BCMA-NDF":{"App2":"NDF","Score":26.25,"App1":"BCMA"},"MAG-PSIV":{"App2":"PSIV","Score":42.857142857142854,"App1":"MAG"},"VBECS-NDF":{"App2":"NDF","Score":28.88888888888889,"App1":"VBECS"},"GMRV-PSX":{"App2":"PSX","Score":24.0,"App1":"GMRV"},"NDF-PSIV":{"App2":"PSIV","Score":46.0,"App1":"NDF"},"GMRV-PSU":{"App2":"PSU","Score":43.33333333333333,"App1":"GMRV"},"GMRV-PSS":{"App2":"PSS","Score":51.63636363636364,"App1":"GMRV"},"PSS-DS":{"App2":"DS","Score":40.72727272727273,"App1":"PSS"},"PSIV-PSX":{"App2":"PSX","Score":24.0,"App1":"PSIV"},"GMRV-PSO":{"App2":"PSO","Score":56.0,"App1":"GMRV"},"PSIV-PSU":{"App2":"PSU","Score":39.54545454545455,"App1":"PSIV"},"PSIV-PSS":{"App2":"PSS","Score":42.54545454545455,"App1":"PSIV"},"GMRV-PSJ":{"App2":"PSJ","Score":53.45454545454545,"App1":"GMRV"},"TIU-MAG":{"App2":"MAG","Score":37.57575757575758,"App1":"TIU"},"PSIV-PSO":{"App2":"PSO","Score":53.4992784992785,"App1":"PSIV"},"GMRA-LR":{"App2":"LR","Score":31.142857142857142,"App1":"GMRA"},"PXRM-ML":{"App2":"ML","Score":29.962978198272317,"App1":"PXRM"},"GMRV-VBECS":{"App2":"VBECS","Score":38.0,"App1":"GMRV"},"PSU-ML":{"App2":"ML","Score":30.804597701149426,"App1":"PSU"},"GMRV-PSD":{"App2":"PSD","Score":45.333333333333336,"App1":"GMRV"},"PSIV-PSJ":{"App2":"PSJ","Score":65.72438672438672,"App1":"PSIV"},"LRAP-PXRM":{"App2":"PXRM","Score":38.730158730158735,"App1":"LRAP"},"NLT \u0026 LOINC-IBD":{"App2":"IBD","Score":25.714285714285715,"App1":"NLT \u0026 LOINC"},"GMRV-PSA":{"App2":"PSA","Score":42.66666666666667,"App1":"GMRV"},"LRAP-OR":{"App2":"OR","Score":36.666666666666664,"App1":"LRAP"},"PSIV-PSD":{"App2":"PSD","Score":28.636363636363637,"App1":"PSIV"},"PSIV-PSA":{"App2":"PSA","Score":27.545454545454547,"App1":"PSIV"},"LREPI-TIUI":{"App2":"TIUI","Score":26.19047619047619,"App1":"LREPI"},"MAG-ML":{"App2":"ML","Score":34.76190476190476,"App1":"MAG"},"LA7-GMRV":{"App2":"GMRV","Score":57.230769230769226,"App1":"LA7"},"TIUI-PSGW":{"App2":"PSGW","Score":34.0,"App1":"TIUI"},"PXRM-LR":{"App2":"LR","Score":28.116001645413412,"App1":"PXRM"},"PSU-LR":{"App2":"LR","Score":26.781609195402297,"App1":"PSU"},"PSIV-BCMA":{"App2":"BCMA","Score":51.45454545454545,"App1":"PSIV"},"ML-BCMA":{"App2":"BCMA","Score":64.28571428571428,"App1":"ML"},"GMTS-PSGW":{"App2":"PSGW","Score":26.333333333333336,"App1":"GMTS"},"LRAP-USR":{"App2":"USR","Score":27.3015873015873,"App1":"LRAP"},"LA7-GMRA":{"App2":"GMRA","Score":41.230769230769226,"App1":"LA7"},"MAG-LR":{"App2":"LR","Score":30.476190476190474,"App1":"MAG"},"MAG-PSGW":{"App2":"PSGW","Score":30.476190476190474,"App1":"MAG"},"NDF-PSGW":{"App2":"PSGW","Score":46.0,"App1":"NDF"},"PSIV-VBECS":{"App2":"VBECS","Score":30.90909090909091,"App1":"PSIV"},"PX-GMTS":{"App2":"GMTS","Score":50.70695970695971,"App1":"PX"},"TIUI-LREPI":{"App2":"LREPI","Score":37.0,"App1":"TIUI"},"LREPI-LRAP":{"App2":"LRAP","Score":39.714285714285715,"App1":"LREPI"},"LRAP-LA7":{"App2":"LA7","Score":33.80952380952381,"App1":"LRAP"},"GMRA-NDF":{"App2":"NDF","Score":30.0,"App1":"GMRA"},"OR-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":34.44444444444444,"App1":"OR"},"PSGW-USR":{"App2":"USR","Score":33.0,"App1":"PSGW"},"LRAP-ML":{"App2":"ML","Score":36.82539682539682,"App1":"LRAP"},"VBECS-BCMA":{"App2":"BCMA","Score":34.44444444444444,"App1":"VBECS"},"LREPI-PX":{"App2":"PX","Score":27.3015873015873,"App1":"LREPI"},"PSIV-PXRM":{"App2":"PXRM","Score":39.36363636363636,"App1":"PSIV"},"ML-PXRM":{"App2":"PXRM","Score":48.14285714285714,"App1":"ML"},"GMRA-GMTS":{"App2":"GMTS","Score":43.14285714285714,"App1":"GMRA"},"LA7-GMPL":{"App2":"GMPL","Score":32.61538461538461,"App1":"LA7"},"PXRM-LREPI":{"App2":"LREPI","Score":34.080625257095846,"App1":"PXRM"},"PSA-NDF":{"App2":"NDF","Score":33.0,"App1":"PSA"},"PX-PSX":{"App2":"PSX","Score":22.857142857142858,"App1":"PX"},"PX-PSU":{"App2":"PSU","Score":38.095238095238095,"App1":"PX"},"PX-PSS":{"App2":"PSS","Score":47.252747252747255,"App1":"PX"},"PSGW-LA7":{"App2":"LA7","Score":35.5,"App1":"PSGW"},"PX-PSO":{"App2":"PSO","Score":50.0,"App1":"PX"},"PSO-NDF":{"App2":"NDF","Score":25.476190476190474,"App1":"PSO"},"LRAP-LR":{"App2":"LR","Score":33.96825396825397,"App1":"LRAP"},"PSD-VBECS":{"App2":"VBECS","Score":33.57142857142857,"App1":"PSD"},"IBD-VBECS":{"App2":"VBECS","Score":27.5,"App1":"IBD"},"VBECS-PX":{"App2":"PX","Score":33.33333333333333,"App1":"VBECS"},"BCMA-LREPI":{"App2":"LREPI","Score":35.0,"App1":"BCMA"},"PX-PSJ":{"App2":"PSJ","Score":44.285714285714285,"App1":"PX"},"USR-VBECS":{"App2":"VBECS","Score":33.33333333333333,"App1":"USR"},"PX-GMRV":{"App2":"GMRV","Score":39.87728937728937,"App1":"PX"},"BCMA-GMTS":{"App2":"GMTS","Score":42.5,"App1":"BCMA"},"ML-NDF":{"App2":"NDF","Score":32.57142857142857,"App1":"ML"},"PX-PSD":{"App2":"PSD","Score":42.85714285714286,"App1":"PX"},"PX-PSA":{"App2":"PSA","Score":41.42857142857143,"App1":"PX"},"DS-PSX":{"App2":"PSX","Score":22.727272727272727,"App1":"DS"},"DS-PSU":{"App2":"PSU","Score":32.0,"App1":"DS"},"PSA-TIUI":{"App2":"TIUI","Score":29.0,"App1":"PSA"},"LREPI-OR":{"App2":"OR","Score":33.01587301587301,"App1":"LREPI"},"DS-PSS":{"App2":"PSS","Score":30.363636363636363,"App1":"DS"},"OR-MAG":{"App2":"MAG","Score":45.55555555555556,"App1":"OR"},"GMRV-TIUI":{"App2":"TIUI","Score":42.20295983086681,"App1":"GMRV"},"DS-PSO":{"App2":"PSO","Score":36.72727272727273,"App1":"DS"},"DS-PSJ":{"App2":"PSJ","Score":31.454545454545453,"App1":"DS"},"PX-GMRA":{"App2":"GMRA","Score":52.86813186813187,"App1":"PX"},"PSX-TIUI":{"App2":"TIUI","Score":25.0,"App1":"PSX"},"VBECS-PXRM":{"App2":"PXRM","Score":38.888888888888886,"App1":"VBECS"},"GMRA-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":27.142857142857142,"App1":"GMRA"},"PXRM-TIU":{"App2":"TIU","Score":36.87371452077334,"App1":"PXRM"},"PSA-PX":{"App2":"PX","Score":37.0,"App1":"PSA"},"PSU-BCMA":{"App2":"BCMA","Score":34.25287356321839,"App1":"PSU"},"DS-PSD":{"App2":"PSD","Score":26.727272727272727,"App1":"DS"},"GMTS-PSX":{"App2":"PSX","Score":21.333333333333332,"App1":"GMTS"},"DS-PSA":{"App2":"PSA","Score":26.727272727272727,"App1":"DS"},"VBECS-OR":{"App2":"OR","Score":30.0,"App1":"VBECS"},"GMTS-PSU":{"App2":"PSU","Score":40.66666666666667,"App1":"GMTS"},"LREPI-TIU":{"App2":"TIU","Score":35.269841269841265,"App1":"LREPI"},"GMTS-PSS":{"App2":"PSS","Score":39.166666666666664,"App1":"GMTS"},"PSGW-GMTS":{"App2":"GMTS","Score":26.5,"App1":"PSGW"},"GMTS-PSO":{"App2":"PSO","Score":49.666666666666664,"App1":"GMTS"},"LA7-USR":{"App2":"USR","Score":29.53846153846154,"App1":"LA7"},"GMRA-GMRV":{"App2":"GMRV","Score":47.14285714285714,"App1":"GMRA"},"GMTS-PSJ":{"App2":"PSJ","Score":38.0,"App1":"GMTS"},"PXRM-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":23.529411764705884,"App1":"PXRM"},"USR-TIU":{"App2":"TIU","Score":40.0,"App1":"USR"},"GMTS-PSD":{"App2":"PSD","Score":32.66666666666667,"App1":"GMTS"},"GMTS-PSA":{"App2":"PSA","Score":32.66666666666667,"App1":"GMTS"},"PSA-LRAP":{"App2":"LRAP","Score":37.0,"App1":"PSA"},"OR-BCMA":{"App2":"BCMA","Score":61.40836940836941,"App1":"OR"},"GMRV-LRAP":{"App2":"LRAP","Score":50.66666666666667,"App1":"GMRV"},"PSD-DS":{"App2":"DS","Score":38.57142857142857,"App1":"PSD"},"NDF-TIU":{"App2":"TIU","Score":30.0,"App1":"NDF"},"GMPL-USR":{"App2":"USR","Score":23.333333333333332,"App1":"GMPL"},"BCMA-GMRV":{"App2":"GMRV","Score":50.16,"App1":"BCMA"},"PSO-GMTS":{"App2":"GMTS","Score":32.85714285714286,"App1":"PSO"},"PSU-VBECS":{"App2":"VBECS","Score":32.98850574712644,"App1":"PSU"},"PSA-OR":{"App2":"OR","Score":42.0,"App1":"PSA"},"PSX-LRAP":{"App2":"LRAP","Score":35.0,"App1":"PSX"},"PX-GMPL":{"App2":"GMPL","Score":40.586080586080584,"App1":"PX"},"BCMA-TIU":{"App2":"TIU","Score":42.5,"App1":"BCMA"},"VBECS-TIU":{"App2":"TIU","Score":32.22222222222222,"App1":"VBECS"},"TIU-GMTS":{"App2":"GMTS","Score":48.72364953886693,"App1":"TIU"},"MAG-VBECS":{"App2":"VBECS","Score":31.904761904761905,"App1":"MAG"},"PSU-PXRM":{"App2":"PXRM","Score":34.367816091954026,"App1":"PSU"},"LREPI-ML":{"App2":"ML","Score":30.158730158730158,"App1":"LREPI"},"BCMA-GMRA":{"App2":"GMRA","Score":52.5,"App1":"BCMA"},"GMPL-LA7":{"App2":"LA7","Score":33.33333333333333,"App1":"GMPL"},"PXRM-IBD":{"App2":"IBD","Score":26.886055121349237,"App1":"PXRM"},"PSJ-BCMA":{"App2":"BCMA","Score":52.76315789473684,"App1":"PSJ"},"TIUI-MAG":{"App2":"MAG","Score":41.0,"App1":"TIUI"},"VBECS-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":24.444444444444443,"App1":"VBECS"},"LR-NDF":{"App2":"NDF","Score":34.0,"App1":"LR"},"PSD-NDF":{"App2":"NDF","Score":35.714285714285715,"App1":"PSD"},"USR-PSIV":{"App2":"PSIV","Score":46.666666666666664,"App1":"USR"},"LREPI-IBD":{"App2":"IBD","Score":27.3015873015873,"App1":"LREPI"},"PSGW-GMRV":{"App2":"GMRV","Score":31.5,"App1":"PSGW"},"VBECS-ML":{"App2":"ML","Score":31.11111111111111,"App1":"VBECS"},"NLT \u0026 LOINC-USR":{"App2":"USR","Score":25.714285714285715,"App1":"NLT \u0026 LOINC"},"LRAP-VBECS":{"App2":"VBECS","Score":34.44444444444444,"App1":"LRAP"},"USR-IBD":{"App2":"IBD","Score":20.0,"App1":"USR"},"LREPI-LR":{"App2":"LR","Score":23.333333333333332,"App1":"LREPI"},"NDF-LREPI":{"App2":"LREPI","Score":33.0,"App1":"NDF"},"OR-PXRM":{"App2":"PXRM","Score":51.74603174603175,"App1":"OR"},"GMRA-GMPL":{"App2":"GMPL","Score":38.285714285714285,"App1":"GMRA"},"PSGW-GMRA":{"App2":"GMRA","Score":48.666666666666664,"App1":"PSGW"},"NDF-IBD":{"App2":"IBD","Score":20.0,"App1":"NDF"},"PSO-GMRV":{"App2":"GMRV","Score":31.904761904761905,"App1":"PSO"},"PXRM-BCMA":{"App2":"BCMA","Score":39.56396544631839,"App1":"PXRM"},"PSD-GMTS":{"App2":"GMTS","Score":35.714285714285715,"App1":"PSD"},"LRAP-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":40.476190476190474,"App1":"LRAP"},"LRAP-PSX":{"App2":"PSX","Score":24.285714285714285,"App1":"LRAP"},"VBECS-LR":{"App2":"LR","Score":31.11111111111111,"App1":"VBECS"},"LRAP-PSU":{"App2":"PSU","Score":36.507936507936506,"App1":"LRAP"},"NLT \u0026 LOINC-LA7":{"App2":"LA7","Score":51.42857142857143,"App1":"NLT \u0026 LOINC"},"LRAP-PSS":{"App2":"PSS","Score":36.666666666666664,"App1":"LRAP"},"PSA-ML":{"App2":"ML","Score":51.0,"App1":"PSA"},"LRAP-PSO":{"App2":"PSO","Score":43.17460317460318,"App1":"LRAP"},"BCMA-IBD":{"App2":"IBD","Score":31.25,"App1":"BCMA"},"BCMA-GMPL":{"App2":"GMPL","Score":33.75,"App1":"BCMA"},"VBECS-IBD":{"App2":"IBD","Score":23.333333333333332,"App1":"VBECS"},"TIU-GMRV":{"App2":"GMRV","Score":39.82213438735178,"App1":"TIU"},"GMRA-DS":{"App2":"DS","Score":42.0,"App1":"GMRA"},"LRAP-PSJ":{"App2":"PSJ","Score":38.25396825396825,"App1":"LRAP"},"PSJ-PXRM":{"App2":"PXRM","Score":41.31578947368421,"App1":"PSJ"},"OR-LREPI":{"App2":"LREPI","Score":38.888888888888886,"App1":"OR"},"LA7-TIUI":{"App2":"TIUI","Score":30.153846153846153,"App1":"LA7"},"PSO-GMRA":{"App2":"GMRA","Score":36.904761904761905,"App1":"PSO"},"LRAP-PSD":{"App2":"PSD","Score":30.952380952380953,"App1":"LRAP"},"LRAP-PSA":{"App2":"PSA","Score":28.095238095238095,"App1":"LRAP"},"PSX-LREPI":{"App2":"LREPI","Score":30.0,"App1":"PSX"},"USR-PSGW":{"App2":"PSGW","Score":46.666666666666664,"App1":"USR"},"PSA-LR":{"App2":"LR","Score":29.0,"App1":"PSA"},"IBD-PX":{"App2":"PX","Score":57.5,"App1":"IBD"},"DS-PSIV":{"App2":"PSIV","Score":34.18181818181819,"App1":"DS"},"TIU-GMRA":{"App2":"GMRA","Score":53.47002635046113,"App1":"TIU"},"PSGW-PSX":{"App2":"PSX","Score":27.5,"App1":"PSGW"},"GMRA-TIU":{"App2":"TIU","Score":52.404181184668985,"App1":"GMRA"},"PXRM-DS":{"App2":"DS","Score":36.15384615384615,"App1":"PXRM"},"PSU-DS":{"App2":"DS","Score":30.919540229885058,"App1":"PSU"},"PSGW-PSU":{"App2":"PSU","Score":43.0,"App1":"PSGW"},"NLT \u0026 LOINC-PSIV":{"App2":"PSIV","Score":57.14285714285714,"App1":"NLT \u0026 LOINC"},"PSGW-PSS":{"App2":"PSS","Score":35.5,"App1":"PSGW"},"PSGW-GMPL":{"App2":"GMPL","Score":44.01032448377581,"App1":"PSGW"},"TIUI-GMTS":{"App2":"GMTS","Score":62.0,"App1":"TIUI"},"LR-PSIV":{"App2":"PSIV","Score":42.0,"App1":"LR"},"PSGW-PSO":{"App2":"PSO","Score":57.666666666666664,"App1":"PSGW"},"PSS-MAG":{"App2":"MAG","Score":48.72727272727273,"App1":"PSS"},"PSGW-PSJ":{"App2":"PSJ","Score":47.5,"App1":"PSGW"},"MAG-DS":{"App2":"DS","Score":37.142857142857146,"App1":"MAG"},"NLT \u0026 LOINC-VBECS":{"App2":"VBECS","Score":31.428571428571427,"App1":"NLT \u0026 LOINC"},"PSGW-PSD":{"App2":"PSD","Score":35.5,"App1":"PSGW"},"PSD-GMRV":{"App2":"GMRV","Score":41.42857142857143,"App1":"PSD"},"LA7-LRAP":{"App2":"LRAP","Score":39.69230769230769,"App1":"LA7"},"MAG-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":26.666666666666664,"App1":"MAG"},"PSGW-PSA":{"App2":"PSA","Score":39.666666666666664,"App1":"PSGW"},"PSA-TIU":{"App2":"TIU","Score":33.0,"App1":"PSA"},"MAG-GMTS":{"App2":"GMTS","Score":34.285714285714285,"App1":"MAG"},"PSS-LREPI":{"App2":"LREPI","Score":40.72727272727273,"App1":"PSS"},"PX-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":21.428571428571427,"App1":"PX"},"NDF-GMTS":{"App2":"GMTS","Score":25.0,"App1":"NDF"},"PSO-GMPL":{"App2":"GMPL","Score":36.666666666666664,"App1":"PSO"},"IBD-OR":{"App2":"OR","Score":42.5,"App1":"IBD"},"PSO-TIU":{"App2":"TIU","Score":30.714285714285715,"App1":"PSO"},"LR-VBECS":{"App2":"VBECS","Score":46.0,"App1":"LR"},"PSD-GMRA":{"App2":"GMRA","Score":51.42857142857143,"App1":"PSD"},"ML-TIU":{"App2":"TIU","Score":36.714285714285715,"App1":"ML"},"TIU-GMPL":{"App2":"GMPL","Score":38.52272727272727,"App1":"TIU"},"PSU-NDF":{"App2":"NDF","Score":26.781609195402297,"App1":"PSU"},"DS-PSGW":{"App2":"PSGW","Score":25.636363636363637,"App1":"DS"},"GMRA-IBD":{"App2":"IBD","Score":30.57142857142857,"App1":"GMRA"},"MAG-NDF":{"App2":"NDF","Score":29.523809523809522,"App1":"MAG"},"NLT \u0026 LOINC-PSGW":{"App2":"PSGW","Score":25.714285714285715,"App1":"NLT \u0026 LOINC"},"TIUI-GMRV":{"App2":"GMRV","Score":46.81818181818182,"App1":"TIUI"},"PSO-LREPI":{"App2":"LREPI","Score":35.23809523809524,"App1":"PSO"},"LR-PSGW":{"App2":"PSGW","Score":34.0,"App1":"LR"},"PX-TIUI":{"App2":"TIUI","Score":47.298534798534796,"App1":"PX"},"LA7-PSX":{"App2":"PSX","Score":23.076923076923077,"App1":"LA7"},"LA7-PSU":{"App2":"PSU","Score":36.30769230769231,"App1":"LA7"},"LA7-PSS":{"App2":"PSS","Score":42.769230769230774,"App1":"LA7"},"LRAP-DS":{"App2":"DS","Score":37.301587301587304,"App1":"LRAP"},"LA7-PSO":{"App2":"PSO","Score":49.84615384615385,"App1":"LA7"},"GMTS-GMRV":{"App2":"GMRV","Score":48.16666666666667,"App1":"GMTS"},"GMTS-VBECS":{"App2":"VBECS","Score":33.0,"App1":"GMTS"},"PSA-IBD":{"App2":"IBD","Score":39.0,"App1":"PSA"},"MAG-GMRV":{"App2":"GMRV","Score":35.23809523809524,"App1":"MAG"},"LA7-PSJ":{"App2":"PSJ","Score":45.230769230769226,"App1":"LA7"},"TIUI-GMRA":{"App2":"GMRA","Score":61.63636363636364,"App1":"TIUI"},"NDF-GMRV":{"App2":"GMRV","Score":33.0,"App1":"NDF"},"LA7-PSD":{"App2":"PSD","Score":38.15384615384615,"App1":"LA7"},"PSD-GMPL":{"App2":"GMPL","Score":32.85714285714286,"App1":"PSD"},"GMPL-PSX":{"App2":"PSX","Score":22.22222222222222,"App1":"GMPL"},"LA7-PSA":{"App2":"PSA","Score":35.07692307692308,"App1":"LA7"},"PSO-IBD":{"App2":"IBD","Score":23.80952380952381,"App1":"PSO"},"GMPL-PSU":{"App2":"PSU","Score":36.666666666666664,"App1":"GMPL"},"GMPL-PSS":{"App2":"PSS","Score":38.33333333333333,"App1":"GMPL"},"GMRV-MAG":{"App2":"MAG","Score":52.0,"App1":"GMRV"},"PSGW-VBECS":{"App2":"VBECS","Score":34.0,"App1":"PSGW"},"IBD-ML":{"App2":"ML","Score":40.0,"App1":"IBD"},"GMTS-GMRA":{"App2":"GMRA","Score":48.16666666666667,"App1":"GMTS"},"GMPL-PSO":{"App2":"PSO","Score":50.55555555555556,"App1":"GMPL"},"ML-IBD":{"App2":"IBD","Score":32.714285714285715,"App1":"ML"},"MAG-GMRA":{"App2":"GMRA","Score":37.61904761904762,"App1":"MAG"},"GMPL-PSJ":{"App2":"PSJ","Score":34.44444444444444,"App1":"GMPL"},"PSIV-MAG":{"App2":"MAG","Score":33.63636363636364,"App1":"PSIV"},"GMRA-TIUI":{"App2":"TIUI","Score":41.832752613240416,"App1":"GMRA"},"NDF-GMRA":{"App2":"GMRA","Score":42.0,"App1":"NDF"},"PX-LRAP":{"App2":"LRAP","Score":39.047619047619044,"App1":"PX"},"DS-PX":{"App2":"PX","Score":41.27272727272727,"App1":"DS"},"GMPL-PSD":{"App2":"PSD","Score":28.88888888888889,"App1":"GMPL"},"GMPL-PSIV":{"App2":"PSIV","Score":38.888888888888886,"App1":"GMPL"},"GMPL-PSA":{"App2":"PSA","Score":33.888888888888886,"App1":"GMPL"},"PSS-PSIV":{"App2":"PSIV","Score":64.54545454545455,"App1":"PSS"},"IBD-LR":{"App2":"LR","Score":22.5,"App1":"IBD"},"IBD-NDF":{"App2":"NDF","Score":20.0,"App1":"IBD"},"BCMA-TIUI":{"App2":"TIUI","Score":37.5,"App1":"BCMA"},"PXRM-USR":{"App2":"USR","Score":22.714932126696834,"App1":"PXRM"},"DS-LREPI":{"App2":"LREPI","Score":31.09090909090909,"App1":"DS"},"LR-TIU":{"App2":"TIU","Score":51.468468468468465,"App1":"LR"},"NLT \u0026 LOINC-PSX":{"App2":"PSX","Score":28.57142857142857,"App1":"NLT \u0026 LOINC"},"PSD-TIU":{"App2":"TIU","Score":35.714285714285715,"App1":"PSD"},"NLT \u0026 LOINC-PSU":{"App2":"PSU","Score":37.14285714285714,"App1":"NLT \u0026 LOINC"},"TIUI-GMPL":{"App2":"GMPL","Score":49.0,"App1":"TIUI"},"LREPI-USR":{"App2":"USR","Score":23.96825396825397,"App1":"LREPI"},"NLT \u0026 LOINC-PSS":{"App2":"PSS","Score":34.285714285714285,"App1":"NLT \u0026 LOINC"},"NLT \u0026 LOINC-PSO":{"App2":"PSO","Score":54.285714285714285,"App1":"NLT \u0026 LOINC"},"LREPI-BCMA":{"App2":"BCMA","Score":35.23809523809524,"App1":"LREPI"},"NLT \u0026 LOINC-PSJ":{"App2":"PSJ","Score":51.42857142857143,"App1":"NLT \u0026 LOINC"},"DS-OR":{"App2":"OR","Score":27.454545454545453,"App1":"DS"},"GMRA-LRAP":{"App2":"LRAP","Score":41.42857142857143,"App1":"GMRA"},"GMTS-GMPL":{"App2":"GMPL","Score":44.166666666666664,"App1":"GMTS"},"NLT \u0026 LOINC-PSD":{"App2":"PSD","Score":28.57142857142857,"App1":"NLT \u0026 LOINC"},"PXRM-LA7":{"App2":"LA7","Score":28.3710407239819,"App1":"PXRM"},"PSJ-NDF":{"App2":"NDF","Score":26.49122807017544,"App1":"PSJ"},"MAG-GMPL":{"App2":"GMPL","Score":32.38095238095238,"App1":"MAG"},"NLT \u0026 LOINC-PSA":{"App2":"PSA","Score":22.857142857142858,"App1":"NLT \u0026 LOINC"},"NDF-GMPL":{"App2":"GMPL","Score":25.0,"App1":"NDF"},"LREPI-LA7":{"App2":"LA7","Score":32.38095238095238,"App1":"LREPI"},"PSGW-TIUI":{"App2":"TIUI","Score":30.5,"App1":"PSGW"},"NDF-USR":{"App2":"USR","Score":38.0,"App1":"NDF"},"PSX-NDF":{"App2":"NDF","Score":55.0,"App1":"PSX"},"IBD-PSIV":{"App2":"PSIV","Score":37.5,"App1":"IBD"},"BCMA-LRAP":{"App2":"LRAP","Score":33.75,"App1":"BCMA"},"USR-LA7":{"App2":"LA7","Score":40.0,"App1":"USR"},"GMTS-PX":{"App2":"PX","Score":53.04597701149425,"App1":"GMTS"},"GMPL-PSGW":{"App2":"PSGW","Score":39.429429429429426,"App1":"GMPL"},"BCMA-USR":{"App2":"USR","Score":27.5,"App1":"BCMA"},"PSS-PSGW":{"App2":"PSGW","Score":33.45454545454545,"App1":"PSS"},"VBECS-USR":{"App2":"USR","Score":27.77777777777778,"App1":"VBECS"},"GMRV-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":28.0,"App1":"GMRV"},"TIUI-PX":{"App2":"PX","Score":59.54545454545455,"App1":"TIUI"},"NDF-LA7":{"App2":"LA7","Score":34.0,"App1":"NDF"},"PSO-TIUI":{"App2":"TIUI","Score":26.904761904761905,"App1":"PSO"},"LREPI-DS":{"App2":"DS","Score":33.96825396825397,"App1":"LREPI"},"LR-IBD":{"App2":"IBD","Score":24.0,"App1":"LR"},"PSD-IBD":{"App2":"IBD","Score":33.57142857142857,"App1":"PSD"},"LREPI-PXRM":{"App2":"PXRM","Score":47.01587301587301,"App1":"LREPI"},"LRAP-PSIV":{"App2":"PSIV","Score":42.53968253968254,"App1":"LRAP"},"BCMA-LA7":{"App2":"LA7","Score":35.0,"App1":"BCMA"},"VBECS-LA7":{"App2":"LA7","Score":34.44444444444444,"App1":"VBECS"},"PSGW-LRAP":{"App2":"LRAP","Score":39.5,"App1":"PSGW"},"TIU-TIUI":{"App2":"TIUI","Score":59.27206851119894,"App1":"TIU"},"PX-MAG":{"App2":"MAG","Score":39.047619047619044,"App1":"PX"},"TIU-NDF":{"App2":"NDF","Score":26.666666666666664,"App1":"TIU"},"VBECS-DS":{"App2":"DS","Score":44.44444444444444,"App1":"VBECS"},"GMTS-OR":{"App2":"OR","Score":39.166666666666664,"App1":"GMTS"},"LA7-VBECS":{"App2":"VBECS","Score":34.769230769230774,"App1":"LA7"},"DS-ML":{"App2":"ML","Score":27.636363636363637,"App1":"DS"},"ML-PX":{"App2":"PX","Score":36.714285714285715,"App1":"ML"},"TIUI-OR":{"App2":"OR","Score":44.0,"App1":"TIUI"},"PSA-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":25.0,"App1":"PSA"},"PSO-LRAP":{"App2":"LRAP","Score":33.80952380952381,"App1":"PSO"},"DS-MAG":{"App2":"MAG","Score":34.0,"App1":"DS"},"IBD-PSGW":{"App2":"PSGW","Score":22.5,"App1":"IBD"},"NDF-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":28.0,"App1":"NDF"},"PSA-DS":{"App2":"DS","Score":43.0,"App1":"PSA"},"DS-LR":{"App2":"LR","Score":27.636363636363637,"App1":"DS"},"TIU-LRAP":{"App2":"LRAP","Score":38.82575757575758,"App1":"TIU"},"USR-GMTS":{"App2":"GMTS","Score":26.666666666666664,"App1":"USR"},"PSD-TIUI":{"App2":"TIUI","Score":27.857142857142858,"App1":"PSD"},"ML-VBECS":{"App2":"VBECS","Score":33.42857142857143,"App1":"ML"},"GMPL-LREPI":{"App2":"LREPI","Score":45.55555555555556,"App1":"GMPL"},"PSA-BCMA":{"App2":"BCMA","Score":47.0,"App1":"PSA"},"GMTS-MAG":{"App2":"MAG","Score":40.66666666666667,"App1":"GMTS"},"GMRV-BCMA":{"App2":"BCMA","Score":62.88935870331219,"App1":"GMRV"},"PSU-TIU":{"App2":"TIU","Score":38.85057471264368,"App1":"PSU"},"ML-OR":{"App2":"OR","Score":55.50965250965251,"App1":"ML"},"LRAP-PSGW":{"App2":"PSGW","Score":30.952380952380953,"App1":"LRAP"},"PSX-BCMA":{"App2":"BCMA","Score":40.0,"App1":"PSX"},"MAG-TIU":{"App2":"TIU","Score":32.38095238095238,"App1":"MAG"},"GMRV-PX":{"App2":"PX","Score":47.25299506694856,"App1":"GMRV"},"GMRA-USR":{"App2":"USR","Score":28.57142857142857,"App1":"GMRA"},"GMTS-ML":{"App2":"ML","Score":36.5,"App1":"GMTS"},"PSU-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":24.137931034482758,"App1":"PSU"},"ML-PSIV":{"App2":"PSIV","Score":70.14285714285714,"App1":"ML"},"PSA-USR":{"App2":"USR","Score":28.0,"App1":"PSA"},"GMRA-LA7":{"App2":"LA7","Score":37.14285714285714,"App1":"GMRA"},"PSO-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":25.238095238095237,"App1":"PSO"},"PSD-LRAP":{"App2":"LRAP","Score":43.57142857142857,"App1":"PSD"},"TIUI-ML":{"App2":"ML","Score":40.0,"App1":"TIUI"},"PSO-USR":{"App2":"USR","Score":24.76190476190476,"App1":"PSO"},"GMTS-LR":{"App2":"LR","Score":37.189655172413794,"App1":"GMTS"},"GMRV-OR":{"App2":"OR","Score":54.78787878787878,"App1":"GMRV"},"BCMA-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":30.0,"App1":"BCMA"},"PSA-PXRM":{"App2":"PXRM","Score":47.0,"App1":"PSA"},"USR-GMRV":{"App2":"GMRV","Score":40.0,"App1":"USR"},"PSO-PX":{"App2":"PX","Score":30.0,"App1":"PSO"},"GMRV-PXRM":{"App2":"PXRM","Score":62.78787878787879,"App1":"GMRV"},"LA7-PX":{"App2":"PX","Score":32.61538461538461,"App1":"LA7"},"ML-USR":{"App2":"USR","Score":28.857142857142858,"App1":"ML"},"GMTS-TIUI":{"App2":"TIUI","Score":51.666666666666664,"App1":"GMTS"},"DS-GMTS":{"App2":"GMTS","Score":35.81818181818182,"App1":"DS"},"GMTS-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":27.833333333333336,"App1":"GMTS"},"PSA-LA7":{"App2":"LA7","Score":42.0,"App1":"PSA"},"MAG-TIUI":{"App2":"TIUI","Score":27.142857142857142,"App1":"MAG"},"PSU-IBD":{"App2":"IBD","Score":23.448275862068964,"App1":"PSU"},"TIUI-LR":{"App2":"LR","Score":37.0,"App1":"TIUI"},"PSX-PXRM":{"App2":"PXRM","Score":40.0,"App1":"PSX"},"NDF-TIUI":{"App2":"TIUI","Score":25.0,"App1":"NDF"},"NLT \u0026 LOINC-GMTS":{"App2":"GMTS","Score":41.42857142857143,"App1":"NLT \u0026 LOINC"},"LR-GMTS":{"App2":"GMTS","Score":52.13513513513514,"App1":"LR"},"PXRM-PSX":{"App2":"PSX","Score":22.352941176470587,"App1":"PXRM"},"PSO-LA7":{"App2":"LA7","Score":33.095238095238095,"App1":"PSO"},"MAG-IBD":{"App2":"IBD","Score":28.095238095238095,"App1":"MAG"},"PXRM-PSU":{"App2":"PSU","Score":30.950226244343888,"App1":"PXRM"},"USR-GMRA":{"App2":"GMRA","Score":46.666666666666664,"App1":"USR"},"PXRM-PSS":{"App2":"PSS","Score":32.90415466886055,"App1":"PXRM"},"IBD-TIU":{"App2":"TIU","Score":60.0,"App1":"IBD"},"VBECS-PSIV":{"App2":"PSIV","Score":37.77777777777778,"App1":"VBECS"},"PSJ-LREPI":{"App2":"LREPI","Score":30.87719298245614,"App1":"PSJ"},"TIUI-VBECS":{"App2":"VBECS","Score":37.0,"App1":"TIUI"},"OR-NDF":{"App2":"NDF","Score":31.11111111111111,"App1":"OR"},"LREPI-PSX":{"App2":"PSX","Score":22.22222222222222,"App1":"LREPI"},"PXRM-PSO":{"App2":"PSO","Score":38.05429864253394,"App1":"PXRM"},"PSJ-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":27.719298245614034,"App1":"PSJ"},"ML-LA7":{"App2":"LA7","Score":39.42857142857143,"App1":"ML"},"LREPI-PSU":{"App2":"PSU","Score":36.19047619047619,"App1":"LREPI"},"LREPI-PSS":{"App2":"PSS","Score":33.492063492063494,"App1":"LREPI"},"PXRM-PSJ":{"App2":"PSJ","Score":34.52488687782805,"App1":"PXRM"},"TIUI-LRAP":{"App2":"LRAP","Score":41.0,"App1":"TIUI"},"PSD-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":28.57142857142857,"App1":"PSD"},"LREPI-PSO":{"App2":"PSO","Score":44.76190476190476,"App1":"LREPI"},"USR-PSX":{"App2":"PSX","Score":33.33333333333333,"App1":"USR"},"PSIV-PSGW":{"App2":"PSGW","Score":32.54545454545455,"App1":"PSIV"},"PXRM-PSD":{"App2":"PSD","Score":27.556561085972852,"App1":"PXRM"},"USR-PSU":{"App2":"PSU","Score":40.0,"App1":"USR"},"PSO-OR":{"App2":"OR","Score":35.0,"App1":"PSO"},"LREPI-PSJ":{"App2":"PSJ","Score":35.87301587301587,"App1":"LREPI"},"PXRM-PSA":{"App2":"PSA","Score":26.380090497737555,"App1":"PXRM"},"LA7-OR":{"App2":"OR","Score":42.15384615384615,"App1":"LA7"},"ML-PSGW":{"App2":"PSGW","Score":37.42857142857143,"App1":"ML"},"USR-PSS":{"App2":"PSS","Score":40.0,"App1":"USR"},"LRAP-MAG":{"App2":"MAG","Score":41.74603174603175,"App1":"LRAP"},"USR-PSO":{"App2":"PSO","Score":46.666666666666664,"App1":"USR"},"LREPI-PSD":{"App2":"PSD","Score":29.047619047619047,"App1":"LREPI"},"GMTS-LRAP":{"App2":"LRAP","Score":40.5,"App1":"GMTS"},"ML-LR":{"App2":"LR","Score":30.57142857142857,"App1":"ML"},"LREPI-PSA":{"App2":"PSA","Score":26.825396825396822,"App1":"LREPI"},"USR-PSJ":{"App2":"PSJ","Score":40.0,"App1":"USR"},"MAG-LRAP":{"App2":"LRAP","Score":41.904761904761905,"App1":"MAG"},"NDF-PSX":{"App2":"PSX","Score":37.0,"App1":"NDF"},"PSJ-TIU":{"App2":"TIU","Score":32.54385964912281,"App1":"PSJ"},"NDF-LRAP":{"App2":"LRAP","Score":38.0,"App1":"NDF"},"NDF-PSU":{"App2":"PSU","Score":45.0,"App1":"NDF"},"USR-PSD":{"App2":"PSD","Score":40.0,"App1":"USR"},"NDF-PSS":{"App2":"PSS","Score":38.0,"App1":"NDF"},"USR-PSA":{"App2":"PSA","Score":33.33333333333333,"App1":"USR"},"NDF-PSO":{"App2":"PSO","Score":42.0,"App1":"NDF"},"PXRM-VBECS":{"App2":"VBECS","Score":28.009049773755656,"App1":"PXRM"},"PSJ-PX":{"App2":"PX","Score":28.157894736842106,"App1":"PSJ"},"PSX-TIU":{"App2":"TIU","Score":25.0,"App1":"PSX"},"DS-GMRV":{"App2":"GMRV","Score":28.363636363636363,"App1":"DS"},"IBD-DS":{"App2":"DS","Score":42.5,"App1":"IBD"},"GMRV-ML":{"App2":"ML","Score":48.96969696969697,"App1":"GMRV"},"BCMA-PSX":{"App2":"PSX","Score":25.0,"App1":"BCMA"},"NDF-PSJ":{"App2":"PSJ","Score":43.0,"App1":"NDF"},"VBECS-PSX":{"App2":"PSX","Score":23.333333333333332,"App1":"VBECS"},"BCMA-PSU":{"App2":"PSU","Score":38.75,"App1":"BCMA"},"VBECS-PSU":{"App2":"PSU","Score":42.22222222222222,"App1":"VBECS"},"BCMA-PSS":{"App2":"PSS","Score":45.0,"App1":"BCMA"},"NDF-PSD":{"App2":"PSD","Score":38.0,"App1":"NDF"},"NLT \u0026 LOINC-GMRV":{"App2":"GMRV","Score":37.14285714285714,"App1":"NLT \u0026 LOINC"},"VBECS-PSS":{"App2":"PSS","Score":35.55555555555556,"App1":"VBECS"},"USR-GMPL":{"App2":"GMPL","Score":26.666666666666664,"App1":"USR"},"BCMA-PSO":{"App2":"PSO","Score":53.75,"App1":"BCMA"},"NDF-PSA":{"App2":"PSA","Score":34.0,"App1":"NDF"},"BCMA-VBECS":{"App2":"VBECS","Score":31.25,"App1":"BCMA"},"BCMA-PX":{"App2":"PX","Score":37.5,"App1":"BCMA"},"VBECS-PSO":{"App2":"PSO","Score":41.11111111111111,"App1":"VBECS"},"LR-GMRV":{"App2":"GMRV","Score":44.666666666666664,"App1":"LR"},"LA7-BCMA":{"App2":"BCMA","Score":41.230769230769226,"App1":"LA7"},"PSU-PSIV":{"App2":"PSIV","Score":47.01149425287356,"App1":"PSU"},"BCMA-PSJ":{"App2":"PSJ","Score":56.25,"App1":"BCMA"},"PSGW-MAG":{"App2":"MAG","Score":38.0,"App1":"PSGW"},"VBECS-PSJ":{"App2":"PSJ","Score":33.33333333333333,"App1":"VBECS"},"VBECS-PSGW":{"App2":"PSGW","Score":31.11111111111111,"App1":"VBECS"},"DS-GMRA":{"App2":"GMRA","Score":43.09090909090909,"App1":"DS"},"BCMA-PSD":{"App2":"PSD","Score":33.75,"App1":"BCMA"},"VBECS-PSD":{"App2":"PSD","Score":30.0,"App1":"VBECS"},"TIU-LREPI":{"App2":"LREPI","Score":37.00757575757576,"App1":"TIU"},"BCMA-PSA":{"App2":"PSA","Score":31.25,"App1":"BCMA"},"PSX-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":35.0,"App1":"PSX"},"VBECS-PSA":{"App2":"PSA","Score":28.88888888888889,"App1":"VBECS"},"GMRV-LR":{"App2":"LR","Score":36.303030303030305,"App1":"GMRV"},"NLT \u0026 LOINC-GMRA":{"App2":"GMRA","Score":34.285714285714285,"App1":"NLT \u0026 LOINC"},"LR-USR":{"App2":"USR","Score":33.33333333333333,"App1":"LR"},"PSD-USR":{"App2":"USR","Score":32.85714285714286,"App1":"PSD"},"LR-GMRA":{"App2":"GMRA","Score":48.666666666666664,"App1":"LR"},"PSJ-OR":{"App2":"OR","Score":47.14912280701755,"App1":"PSJ"},"PSA-LREPI":{"App2":"LREPI","Score":33.0,"App1":"PSA"},"TIUI-NDF":{"App2":"NDF","Score":25.0,"App1":"TIUI"},"PSO-ML":{"App2":"ML","Score":40.23809523809524,"App1":"PSO"},"LA7-ML":{"App2":"ML","Score":42.15384615384615,"App1":"LA7"},"OR-PSIV":{"App2":"PSIV","Score":61.26984126984127,"App1":"OR"},"BCMA-OR":{"App2":"OR","Score":49.230000000000004,"App1":"BCMA"},"PSJ-IBD":{"App2":"IBD","Score":24.385964912280702,"App1":"PSJ"},"LR-LA7":{"App2":"LA7","Score":46.0,"App1":"LR"},"PSD-LA7":{"App2":"LA7","Score":46.42857142857143,"App1":"PSD"},"PSX-IBD":{"App2":"IBD","Score":20.0,"App1":"PSX"},"GMPL-GMTS":{"App2":"GMTS","Score":46.111111111111114,"App1":"GMPL"},"PSS-GMTS":{"App2":"GMTS","Score":42.09090909090909,"App1":"PSS"},"LA7-PXRM":{"App2":"PXRM","Score":41.84615384615385,"App1":"LA7"},"PSS-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":29.09090909090909,"App1":"PSS"},"PSO-LR":{"App2":"LR","Score":24.523809523809526,"App1":"PSO"},"LA7-LR":{"App2":"LR","Score":30.153846153846153,"App1":"LA7"},"DS-GMPL":{"App2":"GMPL","Score":32.36363636363636,"App1":"DS"},"PSU-PSGW":{"App2":"PSGW","Score":30.804597701149426,"App1":"PSU"},"PSJ-PSIV":{"App2":"PSIV","Score":64.07120743034056,"App1":"PSJ"},"NLT \u0026 LOINC-GMPL":{"App2":"GMPL","Score":22.857142857142858,"App1":"NLT \u0026 LOINC"},"LR-GMPL":{"App2":"GMPL","Score":34.0,"App1":"LR"},"GMRA-LREPI":{"App2":"LREPI","Score":35.714285714285715,"App1":"GMRA"},"LA7-MAG":{"App2":"MAG","Score":44.30769230769231,"App1":"LA7"},"GMRA-PSX":{"App2":"PSX","Score":25.714285714285715,"App1":"GMRA"},"GMRA-PSU":{"App2":"PSU","Score":39.14285714285714,"App1":"GMRA"},"GMRA-PSS":{"App2":"PSS","Score":43.42857142857143,"App1":"GMRA"},"PSGW-PX":{"App2":"PX","Score":26.5,"App1":"PSGW"},"GMRA-PSO":{"App2":"PSO","Score":51.42857142857143,"App1":"GMRA"},"TIU-IBD":{"App2":"IBD","Score":35.60606060606061,"App1":"TIU"},"PSJ-ML":{"App2":"ML","Score":44.842621259029926,"App1":"PSJ"},"PX-BCMA":{"App2":"BCMA","Score":48.79120879120879,"App1":"PX"},"GMRA-PSJ":{"App2":"PSJ","Score":52.57142857142857,"App1":"GMRA"},"OR-PSGW":{"App2":"PSGW","Score":36.666666666666664,"App1":"OR"},"GMPL-MAG":{"App2":"MAG","Score":40.0,"App1":"GMPL"},"GMRA-PSD":{"App2":"PSD","Score":37.14285714285714,"App1":"GMRA"},"LREPI-VBECS":{"App2":"VBECS","Score":32.22222222222222,"App1":"LREPI"},"PXRM-PSIV":{"App2":"PSIV","Score":38.41628959276018,"App1":"PXRM"},"PSA-PSX":{"App2":"PSX","Score":25.0,"App1":"PSA"},"GMRA-PSA":{"App2":"PSA","Score":37.14285714285714,"App1":"GMRA"},"PSA-PSU":{"App2":"PSU","Score":48.0,"App1":"PSA"},"BCMA-ML":{"App2":"ML","Score":48.75,"App1":"BCMA"},"IBD-GMTS":{"App2":"GMTS","Score":57.5,"App1":"IBD"},"PSA-PSS":{"App2":"PSS","Score":55.0,"App1":"PSA"},"GMPL-GMRV":{"App2":"GMRV","Score":42.77777777777778,"App1":"GMPL"},"PSS-NDF":{"App2":"NDF","Score":31.636363636363637,"App1":"PSS"},"PSA-PSO":{"App2":"PSO","Score":66.0,"App1":"PSA"},"PSS-GMRV":{"App2":"GMRV","Score":48.90909090909091,"App1":"PSS"},"PSO-PSX":{"App2":"PSX","Score":22.857142857142858,"App1":"PSO"},"NDF-VBECS":{"App2":"VBECS","Score":33.0,"App1":"NDF"},"PSA-PSJ":{"App2":"PSJ","Score":51.0,"App1":"PSA"},"PSO-PSU":{"App2":"PSU","Score":36.904761904761905,"App1":"PSO"},"PSJ-LR":{"App2":"LR","Score":26.05263157894737,"App1":"PSJ"},"PSO-PSS":{"App2":"PSS","Score":39.76190476190476,"App1":"PSO"},"PSA-PSD":{"App2":"PSD","Score":51.0,"App1":"PSA"},"ML-PSX":{"App2":"PSX","Score":28.57142857142857,"App1":"ML"},"PSGW-OR":{"App2":"OR","Score":39.5,"App1":"PSGW"},"ML-PSU":{"App2":"PSU","Score":41.14285714285714,"App1":"ML"},"PSJ-PSGW":{"App2":"PSGW","Score":30.263157894736842,"App1":"PSJ"},"PSO-PSJ":{"App2":"PSJ","Score":53.176870748299315,"App1":"PSO"},"ML-PSS":{"App2":"PSS","Score":57.285714285714285,"App1":"ML"},"NDF-PX":{"App2":"PX","Score":33.0,"App1":"NDF"},"GMPL-GMRA":{"App2":"GMRA","Score":48.888888888888886,"App1":"GMPL"},"BCMA-LR":{"App2":"LR","Score":28.75,"App1":"BCMA"},"GMRA-BCMA":{"App2":"BCMA","Score":53.714285714285715,"App1":"GMRA"},"ML-PSO":{"App2":"PSO","Score":51.285714285714285,"App1":"ML"},"PSS-GMRA":{"App2":"GMRA","Score":53.72727272727273,"App1":"PSS"},"PSO-PSD":{"App2":"PSD","Score":30.238095238095237,"App1":"PSO"},"PSO-PSA":{"App2":"PSA","Score":36.19047619047619,"App1":"PSO"},"LRAP-GMTS":{"App2":"GMTS","Score":39.682539682539684,"App1":"LRAP"},"ML-PSJ":{"App2":"PSJ","Score":66.36679536679537,"App1":"ML"},"NLT \u0026 LOINC-MAG":{"App2":"MAG","Score":40.0,"App1":"NLT \u0026 LOINC"},"LR-PX":{"App2":"PX","Score":44.666666666666664,"App1":"LR"},"PSU-USR":{"App2":"USR","Score":24.71264367816092,"App1":"PSU"},"ML-PSD":{"App2":"PSD","Score":39.42857142857143,"App1":"ML"},"PX-LREPI":{"App2":"LREPI","Score":32.38095238095238,"App1":"PX"},"OR-VBECS":{"App2":"VBECS","Score":30.0,"App1":"OR"},"PX-PXRM":{"App2":"PXRM","Score":61.269230769230774,"App1":"PX"},"ML-PSA":{"App2":"PSA","Score":36.57142857142857,"App1":"ML"},"MAG-USR":{"App2":"USR","Score":29.523809523809522,"App1":"MAG"},"OR-TIU":{"App2":"TIU","Score":43.96825396825397,"App1":"OR"},"PX-OR":{"App2":"OR","Score":37.72893772893772,"App1":"PX"},"PSX-VBECS":{"App2":"VBECS","Score":35.0,"App1":"PSX"},"USR-TIUI":{"App2":"TIUI","Score":33.33333333333333,"App1":"USR"},"PXRM-PSGW":{"App2":"PSGW","Score":26.60633484162896,"App1":"PXRM"},"IBD-GMRV":{"App2":"GMRV","Score":42.5,"App1":"IBD"},"NDF-OR":{"App2":"OR","Score":38.0,"App1":"NDF"},"PSU-LA7":{"App2":"LA7","Score":28.850574712643677,"App1":"PSU"},"GMTS-DS":{"App2":"DS","Score":47.333333333333336,"App1":"GMTS"},"MAG-LA7":{"App2":"LA7","Score":37.61904761904762,"App1":"MAG"},"LR-OR":{"App2":"OR","Score":38.0,"App1":"LR"},"LA7-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":30.153846153846153,"App1":"LA7"},"TIUI-DS":{"App2":"DS","Score":48.0,"App1":"TIUI"},"GMRA-PXRM":{"App2":"PXRM","Score":53.14285714285714,"App1":"GMRA"},"PSS-GMPL":{"App2":"GMPL","Score":38.45454545454545,"App1":"PSS"},"PSGW-BCMA":{"App2":"BCMA","Score":38.0,"App1":"PSGW"},"IBD-GMRA":{"App2":"GMRA","Score":45.0,"App1":"IBD"},"PSGW-ML":{"App2":"ML","Score":42.0,"App1":"PSGW"},"PSS-VBECS":{"App2":"VBECS","Score":38.54545454545455,"App1":"PSS"},"LRAP-GMRV":{"App2":"GMRV","Score":35.87301587301587,"App1":"LRAP"},"GMRV-NDF":{"App2":"NDF","Score":32.66666666666667,"App1":"GMRV"},"USR-LRAP":{"App2":"LRAP","Score":46.666666666666664,"App1":"USR"},"BCMA-PXRM":{"App2":"PXRM","Score":55.0,"App1":"BCMA"},"PSIV-NDF":{"App2":"NDF","Score":25.81818181818182,"App1":"PSIV"},"PSO-BCMA":{"App2":"BCMA","Score":46.19047619047619,"App1":"PSO"},"LR-PSX":{"App2":"PSX","Score":27.333333333333332,"App1":"LR"},"OR-IBD":{"App2":"IBD","Score":32.857142857142854,"App1":"OR"},"PSD-PSX":{"App2":"PSX","Score":25.714285714285715,"App1":"PSD"},"IBD-USR":{"App2":"USR","Score":20.0,"App1":"IBD"},"LR-PSU":{"App2":"PSU","Score":50.0,"App1":"LR"},"PSGW-LR":{"App2":"LR","Score":26.5,"App1":"PSGW"},"LRAP-GMRA":{"App2":"GMRA","Score":38.888888888888886,"App1":"LRAP"},"PSD-PSU":{"App2":"PSU","Score":42.14285714285714,"App1":"PSD"},"LR-PSS":{"App2":"PSS","Score":46.0,"App1":"LR"},"PSIV-GMTS":{"App2":"GMTS","Score":28.81818181818182,"App1":"PSIV"},"PSD-PSS":{"App2":"PSS","Score":54.285714285714285,"App1":"PSD"},"ML-DS":{"App2":"DS","Score":35.42857142857143,"App1":"ML"},"DS-TIUI":{"App2":"TIUI","Score":37.63636363636364,"App1":"DS"},"LR-PSO":{"App2":"PSO","Score":46.0,"App1":"LR"},"ML-GMTS":{"App2":"GMTS","Score":37.57142857142857,"App1":"ML"},"PSGW-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":25.0,"App1":"PSGW"},"PSD-PSO":{"App2":"PSO","Score":54.285714285714285,"App1":"PSD"},"PX-ML":{"App2":"ML","Score":44.395604395604394,"App1":"PX"},"LR-PSJ":{"App2":"PSJ","Score":42.0,"App1":"LR"},"NLT \u0026 LOINC-TIUI":{"App2":"TIUI","Score":25.714285714285715,"App1":"NLT \u0026 LOINC"},"TIU-BCMA":{"App2":"BCMA","Score":47.04545454545455,"App1":"TIU"},"PSD-PSJ":{"App2":"PSJ","Score":51.42857142857143,"App1":"PSD"},"LR-TIUI":{"App2":"TIUI","Score":44.666666666666664,"App1":"LR"},"TIUI-TIU":{"App2":"TIU","Score":93.63636363636363,"App1":"TIUI"},"LR-PSD":{"App2":"PSD","Score":34.0,"App1":"LR"},"NDF-ML":{"App2":"ML","Score":42.0,"App1":"NDF"},"PSO-VBECS":{"App2":"VBECS","Score":30.952380952380953,"App1":"PSO"},"LR-PSA":{"App2":"PSA","Score":34.0,"App1":"LR"},"IBD-GMPL":{"App2":"GMPL","Score":50.0,"App1":"IBD"},"PSD-PSA":{"App2":"PSA","Score":48.57142857142857,"App1":"PSD"},"PSGW-PXRM":{"App2":"PXRM","Score":43.0,"App1":"PSGW"},"IBD-LA7":{"App2":"LA7","Score":35.0,"App1":"IBD"},"PSJ-USR":{"App2":"USR","Score":23.771929824561404,"App1":"PSJ"},"LR-NLT \u0026 LOINC":{"App2":"NLT \u0026 LOINC","Score":31.333333333333332,"App1":"LR"},"LR-ML":{"App2":"ML","Score":42.0,"App1":"LR"},"PX-LR":{"App2":"LR","Score":32.6007326007326,"App1":"PX"},"USR-PX":{"App2":"PX","Score":26.666666666666664,"App1":"USR"},"PSX-USR":{"App2":"USR","Score":30.0,"App1":"PSX"},"OR-PX":{"App2":"PX","Score":33.96825396825397,"App1":"OR"},"NDF-LR":{"App2":"LR","Score":29.0,"App1":"NDF"},"GMRV-DS":{"App2":"DS","Score":39.33333333333333,"App1":"GMRV"},"DS-LRAP":{"App2":"LRAP","Score":32.18181818181819,"App1":"DS"},"VBECS-GMTS":{"App2":"GMTS","Score":33.33333333333333,"App1":"VBECS"},"PSO-PXRM":{"App2":"PXRM","Score":40.714285714285715,"App1":"PSO"},"LRAP-GMPL":{"App2":"GMPL","Score":33.80952380952381,"App1":"LRAP"},"PSJ-LA7":{"App2":"LA7","Score":34.21052631578947,"App1":"PSJ"},"NLT \u0026 LOINC-LRAP":{"App2":"LRAP","Score":54.285714285714285,"App1":"NLT \u0026 LOINC"},"PSD-BCMA":{"App2":"BCMA","Score":49.285714285714285,"App1":"PSD"},"LR-LRAP":{"App2":"LRAP","Score":51.33333333333333,"App1":"LR"},"PSIV-PX":{"App2":"PX","Score":26.727272727272727,"App1":"PSIV"},"PSIV-GMRV":{"App2":"GMRV","Score":35.81818181818182,"App1":"PSIV"},"PSX-LA7":{"App2":"LA7","Score":30.0,"App1":"PSX"},"TIU-PXRM":{"App2":"PXRM","Score":47.46212121212121,"App1":"TIU"},"ML-GMRV":{"App2":"GMRV","Score":45.42857142857143,"App1":"ML"},"LREPI-PSIV":{"App2":"PSIV","Score":42.53968253968254,"App1":"LREPI"},"USR-OR":{"App2":"OR","Score":46.666666666666664,"App1":"USR"},"TIUI-IBD":{"App2":"IBD","Score":37.0,"App1":"TIUI"},"TIU-USR":{"App2":"USR","Score":30.0,"App1":"TIU"},"PSIV-GMRA":{"App2":"GMRA","Score":32.54545454545455,"App1":"PSIV"},"DS-VBECS":{"App2":"VBECS","Score":33.09090909090909,"App1":"DS"},"ML-GMRA":{"App2":"GMRA","Score":47.285714285714285,"App1":"ML"},"PSO-DS":{"App2":"DS","Score":33.33333333333333,"App1":"PSO"}}');
	dimensionData('["Processes_Supported","Direct_Activities_Supported","BLUs_Supported","Tasks_Supported","Data_Objects_Supported"]', 'categories');
	dimensionData('"Score"', 'value');
	dimensionData('"App1"', 'xAxisTitle');
	dimensionData('"Application Duplication"', 'title');
	dimensionData('"App2"', 'yAxisTitle');
	start();	
}