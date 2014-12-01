<!DOCTYPE html>
<html>
<head>
<title>Heat Maps</title>
<style>
       html, body, #map-canvas {
        height: 100%;
        margin: 0px;
        padding: 0px
      }
      #panel {
        position: absolute;
        top: 5px;
        left: 50%;
        margin-left: -180px;
        z-index: 5;
        background-color: #fff;
        padding: 5px;
        border: 1px solid #999;
      }
</style>
<script src="https://maps.googleapis.com/maps/api/js?libraries=visualization"></script>
<script src="https://maps.googleapis.com/maps/api/js"></script>
<script>
	var httpKeywordReq, httpAllTweetReq, interval;
	var httpSpecificUserTweetReq, mapOptions, map;
	var markers = []; 
	var pinColorNegative = "FE7569";
	var pinColorPositive = "33FF00";
	var pinColorNeutral = "FFFF33";
	var pinColorDefault = "E3E3E3";
	
	function addMarker(location, text, pc) {
		var pinImage = new google.maps.MarkerImage(
				"http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|"
						+ pc, new google.maps.Size(21, 34),
				new google.maps.Point(0, 0), new google.maps.Point(10, 34));

		var pinShadow = new google.maps.MarkerImage(
				"http://chart.apis.google.com/chart?chst=d_map_pin_shadow",
				new google.maps.Size(40, 37), new google.maps.Point(0, 0),
				new google.maps.Point(12, 35));

		marker = new google.maps.Marker({
			position : location,
			map : map,
			title : text,
			icon : pinImage,
			shadow : pinShadow
		});

		markers.push(marker);
	}

	function setAllMap(map) {
		for (var i = 0; i < markers.length; i++) {
			markers[i].setMap(map);
		}
	}

	function clearMarkers() {
		setAllMap(null);
	}

	function showMarkers() {
		setAllMap(map);
	}

	function deleteMarkers() {
		clearMarkers();
		markers = [];
	}

	function loadDefaultMap() {
		retrieveKeywords();
		retrieveTweets();
	}

	function retrieveKeywords() {
		if (window.XMLHttpRequest) {
			httpKeywordReq = new XMLHttpRequest();
		} else if (window.ActiveXObject) {
			httpKeywordReq = new ActiveXObject("Microsoft.XMLHTTP");
		}
		httpKeywordReq.open('POST', '/TweetMapServerForKeyword', true);
		httpKeywordReq.setRequestHeader('Content-Type',
				'application/x-www-form-urlencoded');
		httpKeywordReq.onreadystatechange = handleKeywordsResponse;
		httpKeywordReq.send();
	}

	function handleKeywordsResponse() {
		if (httpKeywordReq.readyState == 4) {
			var values = JSON.parse(httpKeywordReq.responseText);
			var dropdown = document.getElementById("selectKeyword");
			kw = values.keywords;
			for (var i = 0; i < kw.length; i++) {
				var optn = document.createElement("OPTION");
				optn.text = kw[i];
				optn.value = kw[i];
				dropdown.options.add(optn);
			}
		}
	}

	function retrieveTweets(keyword) {
		if (window.XMLHttpRequest) {
			httpAllTweetReq = new XMLHttpRequest();
		} else if (window.ActiveXObject) {
			httpAllTweetReq = new ActiveXObject("Microsoft.XMLHTTP");
		}
		httpAllTweetReq.open('POST', '/TweetMapServerForTweets', true);
		httpAllTweetReq.setRequestHeader('Content-Type',
				'application/x-www-form-urlencoded');
		httpAllTweetReq.onreadystatechange = handleTweetsResponse;
		httpAllTweetReq.send(keyword);
	}

	function handleTweetsResponse() {
		if (httpAllTweetReq.readyState == 4) {
			
			var myVar = JSON.parse(httpAllTweetReq.responseText);
			if(myVar.type != "live"){
				deleteMarkers();
			}
			var data = myVar.data;
			if(myVar.count == 0 && myVar.type == "live" && myVar.msg == "done"){
				clearInterval(interval);
			}
			for (var j = 0; j < data.length; j++) {
				var json = JSON.parse(data[j]);
				var point = new google.maps.LatLng(json.latitude, json.longitude);
				var text = "Username: @" + json.username + "\n"
						+ "Sentiment: " + json.sentiment + "\n"
						+ "Timestamp: " + json.timestamp + "\n"
						+ "Tweet text: " + json.text;
				var pc = pinColorDefault;
				if(json.sentiment == "positive"){
					pc = pinColorPositive;
				} else if(json.sentiment == "negative"){
					pc = pinColorNegative;
				} else if(json.sentiment == "neutral"){
					pc = pinColorNeutral;
				}
				addMarker(point, text, pc);
			}
		}
	}

	function initialize(myVar) {
		mapOptions = {
			center : new google.maps.LatLng(20, -10),
			zoom : 2,
			mapTypeId : google.maps.MapTypeId.ROADMAP
		};
		map = new google.maps.Map(document.getElementById('map-canvas'),
				mapOptions);
	}

	function getTweets() {
		var txt = document.getElementById('username').value;
		if (txt.length > 0) {
			if (window.XMLHttpRequest) {
				httpSpecificUserTweetReq = new XMLHttpRequest();
			} else if (window.ActiveXObject) {
				httpSpecificUserTweetReq = new ActiveXObject(
						"Microsoft.XMLHTTP");
			}
			httpSpecificUserTweetReq.open('POST',
					'/TwitterReaderForParticularUser', true);
			httpSpecificUserTweetReq.setRequestHeader('Content-Type',
					'application/x-www-form-urlencoded');
			httpSpecificUserTweetReq.onreadystatechange = httpSpecificUserTweetReq;
			httpSpecificUserTweetReq.send("username=" + txt);
			
			deleteMarkers();
			interval = setInterval(function(){ retrieveTweets("type=live"); }, 3000);
		} else {
			var elem = document.getElementById('selectKeyword');
			var strUser = elem.options[elem.selectedIndex].value;
			if (strUser == "All Tweets") {
				retrieveTweets();
			} else {
				retrieveTweets("input=" + strUser);
			}
		}
	}
	
	function httpSpecificUserTweetReq(){
		if (httpAllTweetReq.readyState == 4) {
			var myVar = JSON.parse(httpSpecificUserTweetReq.responseText);
			if(myVar.msg == "exception"){
				clearInterval(interval);
				alert(myVar.text);
			}
		}
	}

	function processTextInput() {
		var txt = document.getElementById('username').value;
		if (txt.length > 0) {
			document.getElementById('selectKeyword').disabled = true;
		} else if (txt.length == 0) {
			document.getElementById('selectKeyword').disabled = false;
		}
	}
	
	google.maps.event.addDomListener(window, 'load', initialize);
</script>
</head>
<body onload="loadDefaultMap()">
	<div id="panel">
		<button onclick="deleteMarkers()">Clear All Markers</button>
		<button onclick="getTweets()">Get Tweets</button>
		<select id="selectKeyword">
   			<option>All Tweets</option>
		</select>
		<input type="text" id="username" oninput="processTextInput()"/>
	</div>
	<div id="map-canvas"></div>
</body>
</html>