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
	
	function sendRequestToServer(keyword){
		if (window.XMLHttpRequest) {
			rcvReq = new XMLHttpRequest();
		} else if(window.ActiveXObject) {
			rcvReq = new ActiveXObject("Microsoft.XMLHTTP"); 
		}
		rcvReq.open('POST', '/TweetMapServer', true);
		rcvReq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
		rcvReq.onreadystatechange = handleResponse;
		rcvReq.send(keyword);
		return rcvReq;
	}
	
	rcvReq = sendRequestToServer("input=NoKeyword");
	
	function handleResponse(){
		if (rcvReq.readyState == 4) {
			//alert(rcvReq.responseText);
			initialize(JSON.parse(rcvReq.responseText));
			google.maps.event.addDomListener(window, 'load', initialize);
		}
	}
	
	function initialize(myVar) {
		var mapOptions = {
			center: new google.maps.LatLng(37.774546, -122.433523),
			zoom: 3,
			mapTypeId:google.maps.MapTypeId.ROADMAP
  		};
		var map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
		
		kw = myVar.keywords;
		var dropdown = document.getElementById("selectKeyword");
	    for (var i = 0; i < kw.length; i++){    
	    	var optn = document.createElement("OPTION");
		    optn.text = kw[i];
		    optn.value = kw[i];
		    dropdown.options.add(optn);
	    }
		
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
	
	$(document).ready(function() {
		$("#selectKeyword").change(function() {
	    	var srch= $("#selectKeyword").val();
	    	rcvReq = sendRequestToServer("input=" + srch);
	    });
	});

</script>
</head>
<body>
	<div id="panel">
		<select id="selectKeyword">
   			<option>Choose a keyword</option>
		</select>
	</div>
	<div id="map-canvas"></div>
</body>
</html>