					
					var SEMOSS = [];
					
					SEMOSS.DbInfo = function(){};
					
					SEMOSS.DbInfo.prototype.name = null;
					SEMOSS.DbInfo.prototype.serverUrl = null;
					SEMOSS.DbInfo.prototype.dataUrl = null;
					SEMOSS.DbInfo.prototype.insightsUrl = null;
					
					SEMOSS.DbInfo.prototype.setAttributes = function(object){
						this.name = object['name'];
						this.serverUrl = object['serverUrl'];
						this.dataUrl = object['dataUrl'];
						this.insightsUrl = object['insightsUrl'];
					}

					SEMOSS.createDatabase = function (knowledgebase, callBackFunction, asynchronous){
						var json = SEMOSS.encodeParamObject(knowledgebase);
						var url = 'databases/create' + json;
						if (asynchronous == undefined){
							asynchronous = true;
						}
						$.ajax({
							type : "POST",
							url : url,
							async: asynchronous,
							success : function(response) {
								var token = SEMOSS.processResponse(response);
								callBackFunction(token);
							}
						});
					}
					
					SEMOSS.updateDatabase = function (knowledgebase, callBackFunction, asynchronous){
						var json = SEMOSS.encodeParamObject(knowledgebase);
						var url = 'databases/create' + json;
						if (asynchronous == undefined){
							asynchronous = true;
						}
						$.ajax({
							type : "GET",
							url : url,
							success : function(response) {
								var token = SEMOSS.processResponse(response);
								callBackFunction(token);
							}
						});
					}
					
					SEMOSS.deleteDatabase = function (id, callBackFunction, asynchronous){
						var url = 'databases/delete/' + id;
						if (asynchronous == undefined){
							asynchronous = true;
						}
						$.ajax({
							type : "POST",
							url : url,
							success : function(response) {
								var token = SEMOSS.processResponse(response);
								callBackFunction(token);
							}
						});
					}
					
					SEMOSS.getDatabase = function (id, callBackFunction, asynchronous){
						var url = 'databases/' + id;
						if (asynchronous == undefined){
							asynchronous = true;
						}
						$.ajax({
							type : "GET",
							url : url,
							success : function(response) {
								callBackFunction(response);
							}
						});
					}
					
					SEMOSS.listDatabases = function (callBackFunction, asynchronous){
						var url = 'databases/';
						if (asynchronous == undefined){
							asynchronous = true;
						}
						$.ajax({
							type : "GET",
							beforeSend: function (request)
				            {
								//var username = "rbobko";
								//var password = "password";
								//var plainCreds = username + ":" + password;
								//var encodedCreds = Base64.encode(plainCreds);
								//request.setRequestHeader("Authorization", "Basic " + encodedCreds);
				            },
							url : url,
							success : function(response) {
								//var token = SEMOSS.processResponse(response);
								callBackFunction(response);
							}
						});
					}
					
					
					SEMOSS.User= function(){};
					
					SEMOSS.User.prototype.username = null;
					SEMOSS.User.prototype.displayName = null;
					SEMOSS.User.prototype.email = null;
					SEMOSS.User.prototype.role = null;
					
					SEMOSS.User.prototype.setAttributes = function(object){
						this.username = object['username'];
						this.displayName = object['properties'].USER_FULLNAME;
						this.email = object['properties'].USER_EMAIL;

					}

					
					SEMOSS.listUsers = function (callBackFunction){
						var url = 'users/';
						$.ajax({
							type : "GET",
							url : url,
							success : function(response) {
								callBackFunction(response);
							}
						});
					}
					
					SEMOSS.getUser = function (username, callBackFunction){
						var url = 'users/' + username;
						$.ajax({
							type : "GET",
							url : url,
							success : function(response) {
								callBackFunction(response);
							}
						});
					}
					
					SEMOSS.setAccesses = function (username, map, callBackFunction){
						var encoding = JSON.stringify(map);
						var url = 'users/accesses/' + username + '/' + encoding;
						$.ajax({
							type : "GET",
							url : url,
							success : function(response) {
								callBackFunction(response);
							}
						});
					}
					


					SEMOSS.processResponse = function(response) {
						var unescapedString = unescape(response);
						var token = JSON.parse(unescapedString);
						SEMOSS.checkErrors(token);
						return token;
					}

					
					SEMOSS.encodeParamObject = function (object){
						var json = encodeURIComponent(JSON.stringify(object));
						var find = '%2F';
						var re = new RegExp(find, 'g');
						json = json.replace(re, '%252F');
						var secondFind = '%5F';
						var re2 = new RegExp(secondFind, 'g');
						json = json.replace(re2, '%255C');
						return json;
					}

					SEMOSS.processResponse = function(response) {
						var unescapedString = unescape(response);
						var token = JSON.parse(unescapedString);
						SEMOSS.checkErrors(token);
						return token;
					}


/**
					 * Checks for errors and warnings in a communication token
					 * that came back from the server after a REST call.
					 * @return False if the operation was successful, even with warnings
					 * or true if it did in fact fail.
					 */
					SEMOSS.processAnyFailures = function (token, successMessage){
						var status = token.result;
						if (token.result == DTConstants.SUCCESS){
							$('#success_message_panel').show();
							$('#failure_message_panel').hide();
							$('#warning_message_panel').hide();
							$('#success_message_area').html(successMessage);
							setTimeout(function(){ $('#success_message_panel').hide({'duration':1000}) }, 2000);
							console.log(successMessage);
							return false;
						}
						else if (token.result  == DTConstants.SUCCESS_WITH_WARNINGS){
							$('#warning_message_panel').show();
							$('#failure_message_panel').hide();
							$('#success_message_panel').hide();
							$('#warning_message_area').html(token.message);
							setTimeout(function(){ $('#warning_message_panel').hide({'duration':1000}) }, 2000);
							console.log(successMessage);
							return false;					
						}
						else if (token.result  == DTConstants.FAILURE){
							$('#failure_message_panel').show();
							$('#warning_message_panel').hide();
							$('#success_message_panel').hide();
							$('#failure_message_area').html(token.message);
							setTimeout(function(){ $('#failure_message_panel').hide({'duration':1000}) }, 2000);
							console.log(successMessage);
							return true;
						}
					}
					
					SEMOSS.menuVisible = true;
					
					SEMOSS.toggleNavMenu = function (){
						if (SEMOSS.menuVisible){
							$('#dt_navigation_menu').hide(200);
							$('#expand_border').removeClass('glyphicon-menu-left');
							$('#expand_border').addClass('glyphicon-menu-right');
							SEMOSS.menuVisible = false;
						}
						else {
							$('#dt_navigation_menu').show(200);
							$('#expand_border').removeClass('glyphicon-menu-right');
							$('#expand_border').addClass('glyphicon-menu-left');
							SEMOSS.menuVisible = true;
						}
					}
					
					SEMOSS.showPanel = function(id) {
						$('.main_div').each(function() {
							$(this).hide(200);
						});
						$('#' + id).show(200);
						
						$('.admin_nav_item').each(function() {
							$(this).removeClass('active');
						});
						$('#' + id + "_nav").addClass('active');
					}
					
					SEMOSS.showNavMenu = function(navmenuID, buttonID){
						$('.semoss_nav_menu_btn').each(function() {
							$(this).removeClass('btn-info');
						});
						$('.semoss_nav_menu').each(function() {
							$(this).hide();
						});
						$('#' + buttonID).addClass('btn-info');
						$('#' + navmenuID).show();
					}
					
					$(document).ready(function (){
						$('#success_message_panel').hide();
						$('#warning_message_panel').hide();
						$('#failure_message_panel').hide();
						$('.main_div').each(function() {
							$(this).hide();
						});

						
						// Set the set of nav items 
						$('.semoss_nav_menu_btn').each(function() {
							$(this).removeClass('btn-info');
						});
						
						
						// Set the active nave item (Organizations)
						$('.admin_nav_item').each(function() {
							$(this).removeClass('active');
						});
						$('#dtorganizations_nav').addClass('active');
						$('#dt_organizations').hide();
						
					});
					
					
					
					var Base64 = {

							// private property
							_keyStr : "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",

							// public method for encoding
							encode : function (input) {
							    var output = "";
							    var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
							    var i = 0;

							    input = Base64._utf8_encode(input);

							    while (i < input.length) {

							        chr1 = input.charCodeAt(i++);
							        chr2 = input.charCodeAt(i++);
							        chr3 = input.charCodeAt(i++);

							        enc1 = chr1 >> 2;
							        enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
							        enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
							        enc4 = chr3 & 63;

							        if (isNaN(chr2)) {
							            enc3 = enc4 = 64;
							        } else if (isNaN(chr3)) {
							            enc4 = 64;
							        }

							        output = output +
							        this._keyStr.charAt(enc1) + this._keyStr.charAt(enc2) +
							        this._keyStr.charAt(enc3) + this._keyStr.charAt(enc4);

							    }

							    return output;
							},

							// public method for decoding
							decode : function (input) {
							    var output = "";
							    var chr1, chr2, chr3;
							    var enc1, enc2, enc3, enc4;
							    var i = 0;

							    input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");

							    while (i < input.length) {

							        enc1 = this._keyStr.indexOf(input.charAt(i++));
							        enc2 = this._keyStr.indexOf(input.charAt(i++));
							        enc3 = this._keyStr.indexOf(input.charAt(i++));
							        enc4 = this._keyStr.indexOf(input.charAt(i++));

							        chr1 = (enc1 << 2) | (enc2 >> 4);
							        chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
							        chr3 = ((enc3 & 3) << 6) | enc4;

							        output = output + String.fromCharCode(chr1);

							        if (enc3 != 64) {
							            output = output + String.fromCharCode(chr2);
							        }
							        if (enc4 != 64) {
							            output = output + String.fromCharCode(chr3);
							        }

							    }

							    output = Base64._utf8_decode(output);

							    return output;

							},

							// private method for UTF-8 encoding
							_utf8_encode : function (string) {
							    string = string.replace(/\r\n/g,"\n");
							    var utftext = "";

							    for (var n = 0; n < string.length; n++) {

							        var c = string.charCodeAt(n);

							        if (c < 128) {
							            utftext += String.fromCharCode(c);
							        }
							        else if((c > 127) && (c < 2048)) {
							            utftext += String.fromCharCode((c >> 6) | 192);
							            utftext += String.fromCharCode((c & 63) | 128);
							        }
							        else {
							            utftext += String.fromCharCode((c >> 12) | 224);
							            utftext += String.fromCharCode(((c >> 6) & 63) | 128);
							            utftext += String.fromCharCode((c & 63) | 128);
							        }

							    }

							    return utftext;
							},

							// private method for UTF-8 decoding
							_utf8_decode : function (utftext) {
							    var string = "";
							    var i = 0;
							    var c = c1 = c2 = 0;

							    while ( i < utftext.length ) {

							        c = utftext.charCodeAt(i);

							        if (c < 128) {
							            string += String.fromCharCode(c);
							            i++;
							        }
							        else if((c > 191) && (c < 224)) {
							            c2 = utftext.charCodeAt(i+1);
							            string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
							            i += 2;
							        }
							        else {
							            c2 = utftext.charCodeAt(i+1);
							            c3 = utftext.charCodeAt(i+2);
							            string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
							            i += 3;
							        }

							    }

							    return string;
							}

							}