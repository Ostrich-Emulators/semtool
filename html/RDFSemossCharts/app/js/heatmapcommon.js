var currentColor = 'Red';

var colorsRed            = ["#FFFFCC","#FFEDA0","#FED976","#FEB24C","#FD8D3C","#FC4E2A","#E31A1C","#BD0026","#800026"];
var colorsBlue           = ["#F7FBFF","#DEEBF7","#C6DBEF","#9ECAE1","#6BAED6","#4292C6","#2171B5","#08519C","#08306B"];
var colorsGreen          = ["#F7FCF5","#E5F5E0","#C7E9C0","#A1D99B","#74C476","#41AB5D","#238B45","#006D2C","#00441B"];
var colorsTraffic        = ["#ae0e06","#e92e10","#fb741e","#fdc63f","#ffff57","#5cba24","#1e8b1f","#1e8b1f","#005715"];
var colorsTrafficReverse = ["#005715","#1e8b1f","#1e8b1f","#5cba24","#ffff57","#fdc63f","#fb741e","#e92e10","#ae0e06"];

function getColors() {
	if (currentColor == 'Green')
		return colorsGreen;
	if (currentColor == 'Blue')
		return colorsBlue;
	if (currentColor == 'Traffic')
		return colorsTraffic;
	if (currentColor == 'Traffic Reverse')
		return colorsTrafficReverse;

	return colorsRed;
}

//  Evaluating what places to round heatmap values to.
//  It seems better to have a difference within the sorted array > 10, than to chance
//  having a long mantissa with seemingly no regard for significant digits.
//
//  e.g.: 3.25 < 4  (decimals == 1) --> 32.5 < 33  (decimals == 2) --> 325 == 325 (stop).
function determineHowManyDecimalsToKeep(allValuesSorted) {
	var valueArrayDiff = allValuesSorted[Math.floor(allValuesSorted.length*3/4)]-allValuesSorted[Math.floor(allValuesSorted.length/4)];
	if(valueArrayDiff > 10) {
		return 0;
	}
	
	var decimals = 0;
	valueArrayDiff = valueArrayDiff.toPrecision(2);
	while(valueArrayDiff < Math.ceil(valueArrayDiff)){
		valueArrayDiff = valueArrayDiff*10;
		decimals++;
	}
	
	return decimals;
}

// Color Chooser
function initColorChooserAndAddToEndOfHtmlElementWithId(elementId) {
	d3.select("#" + elementId).append("select")
		.attr("id", "colorChooser")
		.style("width", "130px")
		.attr("class", "mySelect2")
		.selectAll("option")
		.data(["Red","Blue","Green","Traffic", "Traffic Reverse"])
		.enter()
		.append("option")
		.attr("value", function(d){ return d; }) /* This gives me the value */
		.text(function(d){ return d});
}

//Define the Data-Range Slider, and its "slide" handler:
function initSlider(sliderId, allValuesSorted, decimals) {
	$('#'+sliderId).slider({
		min: allValuesSorted[0],
		max: allValuesSorted[allValuesSorted.length-1],
		value:[allValuesSorted[0], allValuesSorted[allValuesSorted.length-1]],
		step:Math.pow(10,-1*decimals),
		formater: function(value) {
			return value.toFixed(decimals);
		}
	}).on('slide', function(){
		updateVisualization();
	});
	
	$('#'+sliderId).trigger('slide');

	d3.select("#min").append("text")
		.text("Min: " + allValuesSorted[0].toFixed(decimals))
		.attr("x", 20)
		.attr("y", 0);

	d3.select("#max").append("text")
		.text("Max: " + allValuesSorted[allValuesSorted.length-1].toFixed(decimals))
		.attr("x", 20)
		.attr("y", 0);
}

//Build an array of heat-map values that have unique colors,
//where each value is the smallest number having that color
//Use this data to build a new heat-map legend
function buildLegendHtml( sortedArr ) {
	var uniqueColor = colorScale(sortedArr[0]);
	var uniqueColorsArray = new Array(sortedArr[0], uniqueColor);
	
	for(i = 1; i < sortedArr.length; i++){
	   if(colorScale(sortedArr[i]) != uniqueColor){
	      uniqueColor = colorScale(sortedArr[i]);
	      uniqueColorsArray.push(new Array(sortedArr[i], uniqueColor));
	   }
	}
	
	uniqueColorsArray.sort(function(a, b) {
	   return a[0] - b[0];
	});
	
	var strLegend = '<table><tr>';
	for(i = 0; i < uniqueColorsArray.length; i++){
	   strLegend += '<td style="text-align: left;"><div style="background-color: '+uniqueColorsArray[i][1]+'; width: 50px; height: 20px;"></div>'+uniqueColorsArray[i][0]+'</td>';
	}
	strLegend += '</tr></table>';
	
	return strLegend;
}
