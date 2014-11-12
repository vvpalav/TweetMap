<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
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

	var req, rcvReq;
	
	function loadDefaultMap(){
		req = retrieveKeywords();
		rcvReq = retrieveTweets("input=NoKeyword");
	}
	
	function retrieveKeywords(){
		if (window.XMLHttpRequest) {
			req = new XMLHttpRequest();
		} else if(window.ActiveXObject) {
			req = new ActiveXObject("Microsoft.XMLHTTP"); 
		}
		req.open('POST', '/TweetMapServerForKeyword', true);
		req.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
		req.onreadystatechange = handleKeywordsResponse;
		req.send();
		return req;
	}
	
	function handleKeywordsResponse(){
		if (req.readyState == 4) {
			var values = JSON.parse(req.responseText);
			var dropdown = document.getElementById("selectKeyword");
			kw = values.keywords;
		    for (var i = 0; i < kw.length; i++){    
		    	var optn = document.createElement("OPTION");
			    optn.text = kw[i];
			    optn.value = kw[i];
			    dropdown.options.add(optn);
		    }
		}
	}
	
	function retrieveTweets(keyword){
		if (window.XMLHttpRequest) {
			rcvReq = new XMLHttpRequest();
		} else if(window.ActiveXObject) {
			rcvReq = new ActiveXObject("Microsoft.XMLHTTP"); 
		}
		rcvReq.open('POST', '/TweetMapServerForTweets', true);
		rcvReq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
		rcvReq.onreadystatechange = handleTweetsResponse;
		rcvReq.send(keyword);
		return rcvReq;
	}
	
	function handleTweetsResponse(){
		if (rcvReq.readyState == 4) {
			initialize(JSON.parse(rcvReq.responseText));
			google.maps.event.addDomListener(window, 'load', initialize);
		}
	}
	
	function initialize(myVar) {
		var mapOptions = {
			center: new google.maps.LatLng(20,-10),
			zoom: 2,
			mapTypeId:google.maps.MapTypeId.ROADMAP
  		};
		var map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
		var latlng = myVar.latlon;
  		for (var j = 0; j < latlng.length; j++) {
	    	var str = latlng[j];
	    	var res = str.split(" "); 
	    	point = new google.maps.LatLng(parseFloat(res[0]), parseFloat(res[1]));
	    	marker = new google.maps.Marker({
	            position: point,
	            map: map
	       	}); 
		}
	}
	
	function getTweets() {
		var txt = document.getElementById('username').value;
		if (txt.length > 0) {
			if (window.XMLHttpRequest) {
				newReq = new XMLHttpRequest();
			} else if(window.ActiveXObject) {
				newReq = new ActiveXObject("Microsoft.XMLHTTP"); 
			}
			newReq.open('POST', '/TwitterReaderForParticularUser', true);
			newReq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
			newReq.onreadystatechange = handleTweetsResponseForGivenUser;
			newReq.send("username=" + txt);
		} else {
			var elem = document.getElementById('selectKeyword');
			var strUser = elem.options[elem.selectedIndex].value;
			if (strUser == "All Tweets") {
				rcvReq = retrieveTweets("input=NoKeyword");
			} else {
				rcvReq = retrieveTweets("input=" + strUser);
			}
		}
	}
	
	function handleTweetsResponseForGivenUser(){
		if (newReq.readyState == 4) {
			var json = JSON.parse(newReq.responseText);
			if(json.error == "success"){
				initialize(json);
				google.maps.event.addDomListener(window, 'load', initialize);
			} else if ( json.error == "failed"){
				alert("Failed to pull tweets for " + document.getElementById('username').value);
				loadDefaultMap();
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
</script>
</head>
<body onload="loadDefaultMap()">
	<div id="panel">
		<button onclick="getTweets()">Get Tweets</button>
		<select id="selectKeyword">
   			<option>All Tweets</option>
		</select>
		<input type="text" id="username" oninput="processTextInput()"/>
	</div>
	<div id="map-canvas"></div>
</body>
</html>