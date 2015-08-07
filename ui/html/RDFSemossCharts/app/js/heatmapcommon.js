var colorScale;
var currentColor;
var decimalsToKeep;
var valueArray = [];

var colorHash = {};
colorHash["Red"]             = ["#FFFFCC","#FFEDA0","#FED976","#FEB24C","#FD8D3C","#FC4E2A","#E31A1C","#BD0026","#800026"];
colorHash["Blue"]            = ["#F7FBFF","#DEEBF7","#C6DBEF","#9ECAE1","#6BAED6","#4292C6","#2171B5","#08519C","#08306B"];
colorHash["Green"]           = ["#F7FCF5","#E5F5E0","#C7E9C0","#A1D99B","#74C476","#41AB5D","#238B45","#006D2C","#00441B"];
colorHash["Traffic"]         = ["#AE0E06","#E92E10","#FB741E","#FDC63F","#FFFF57","#5CBA24","#1E8B1F","#005715"];
colorHash["Traffic Reverse"] = ["#005715","#1E8B1F","#5CBA24","#FFFF57","#FDC63F","#FB741E","#E92E10","#AE0E06"];

function getColors() {
	var returnVar = colorHash[currentColor];
	if (returnVar == null)
		returnVar = colorHash["Red"];
	
	return returnVar;
}

function setColorScale(domainArray) {
	colorScale = d3.scale.quantile()
		.domain(domainArray)
		.range( getColors() );
}

//  Evaluating what places to round heatmap values to.
//  It seems better to have a difference within the sorted array > 10, than to chance
//  having a long mantissa with seemingly no regard for significant digits.
//
//  e.g.: 3.25 < 4  (decimalsToKeep == 1) --> 32.5 < 33  (decimalsToKeep == 2) --> 325 == 325 (stop).
function determineHowManyDecimalsToKeep(allValuesSorted) {
	var val1 = allValuesSorted[Math.floor(allValuesSorted.length*3/4)];
	var val2 = allValuesSorted[Math.floor(allValuesSorted.length*1/4)];
	var valueArrayDiff = (val1 - val2).toPrecision(2);
	
	decimalsToKeep = 0;
	if(valueArrayDiff > 10) {
		return;
	}
	
	while(valueArrayDiff < Math.ceil(valueArrayDiff)){
		valueArrayDiff = valueArrayDiff*10;
		decimalsToKeep++;
	}
}

function roundNumber(inputNumber) {
	return inputNumber.toFixed(decimalsToKeep);
}

// Color Chooser
function initColorChooserAndAddToEndOfHtmlElementWithId(elementId, functionToCallOnChange) {
	var id = "colorChooser";
	
	d3.select("#" + elementId).append("select")
		.attr("id", id)
		.style("width", "130px")
		.attr("class", "mySelect2")
		.selectAll("option")
		.data(["Red","Blue","Green","Traffic", "Traffic Reverse"])
		.enter()
		.append("option")
		.attr("value", function(d){ return d; }) /* This gives me the value */
		.text(function(d){ return d});
	
	$("#" + id).select2();
	$("#" + id).on("change", functionToCallOnChange);
}

//Define the Data-Range Slider, and its "slide" handler:
function initSlider(sliderId, allValuesSorted) {
	determineHowManyDecimalsToKeep(allValuesSorted);
	
	$('#'+sliderId).slider({
		min: allValuesSorted[0],
		max: allValuesSorted[allValuesSorted.length-1],
		value:[allValuesSorted[0], allValuesSorted[allValuesSorted.length-1]],
		step:Math.pow(10,-1*decimalsToKeep),
		formater: function(value) {
			return roundNumber(value);
		}
	}).on('slide', function(){
		updateVisualization();
	});
	
	$('#'+sliderId).trigger('slide');

	d3.select("#min").append("text")
		.text("Min: " + roundNumber(allValuesSorted[0]))
		.attr("x", 20)
		.attr("y", 0);

	d3.select("#max").append("text")
		.text("Max: " + roundNumber(allValuesSorted[allValuesSorted.length-1]))
		.attr("x", 20)
		.attr("y", 0);
}

//Change Handler for the Alternate Color Selector. Also called whenever the data-range slider
//is moved (and when the heat-map is initially opened). Re-adjust the color scale of data-points
//on the chart, as well as the legend.
function updateVisualization() {
	currentColor = $('#colorChooser').attr('value');

	var valueArrayUpdated = [];
	var domainArray = $('#slider').data('slider').getValue();
	for(i = 0; i < valueArray.length; i++){
		if(valueArray[i] >= domainArray[0] && valueArray[i] <= domainArray[1]){
			valueArrayUpdated.push(valueArray[i]);
		}
	}
	
	setColorScale(valueArrayUpdated);
	updateHeatmap(domainArray);
}

function buildLegend(legendSelector, startingX, startingY, legendElementWidth, legendElementHeight) {
	d3.select(legendSelector).selectAll(".legend").remove();
	
	var legend = d3.select(legendSelector).selectAll(".legend")
		.data([0].concat(colorScale.quantiles()), function(d) { return d; })
		.enter().append("g")
		.attr("class", "legend");

	legend.append("rect")
		.attr("x", function(d, i) { return startingX + (legendElementWidth * i); })
		.attr("y", startingY)
		.attr("width", legendElementWidth)
		.attr("height", legendElementHeight)
		.style("fill", function(d, i) { return getColors()[i]; });

	legend.append("text")
		.attr("class", "legend-text")
		.text(function(d) { return Math.round(d); })
		.attr("x", function(d, i) { return startingX + (legendElementWidth * i); })
		.attr("y", startingY + legendElementHeight + 15);
}