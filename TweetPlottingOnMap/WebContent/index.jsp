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
	
	function loadMap(){
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
			alert(req.responseText);
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
	
	function getTweets(){
		var elem = document.getElementById('selectKeyword');
		var strUser = elem.options[elem.selectedIndex].value;
		if(strUser == "Choose a keyword") {
			rcvReq = retrieveTweets("input=NoKeyword");
		} else {
			rcvReq = retrieveTweets("input=" + strUser);
		}
	}
	
</script>
</head>
<body onload="loadMap()">
	<div id="panel">
		<button onclick="getTweets()">Get Tweets</button>
		<select id="selectKeyword">
   			<option>Choose a keyword</option>
		</select>
	</div>
	<div id="map-canvas"></div>
</body>
</html>