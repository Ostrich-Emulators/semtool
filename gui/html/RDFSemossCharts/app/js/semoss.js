var runOutsideOfApp = false;

//uncomment the following for testing outside of SEMOSS
//runOutsideOfApp = true;

$( document ).ready(function() {
	if (!runOutsideOfApp)
		alert("document:loaded");
	else 
		runOutsideApp();
});

function disable(selector){
    $(selector).prop("disabled",true);
}

function enable(selector){
    $(selector).prop("disabled",false);
}

function resizeFont(incr) {
  $( ".userCanResize, body svg" ).find( "tspan, text" ).each(function( index ) {
    doTheResize( $(this), incr );
  });
  
  $( ".userCanResize" ).each(function( index ) {
    doTheResize( $(this), incr );
  });
}

function doTheResize(element, incr) {
  var size = element.css("font-size");
  var newSize = parseInt(size.replace(/px/, "")) + incr;
  
  element.css("font-size", newSize + "px");
}

function invalid( item ){
	return (item === undefined || item === null);
}