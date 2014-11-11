<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>Heat Maps</title>
<style>
       #map_canvas {
        width: 500px;
        height: 400px;
      }
</style>
<script src="https://maps.googleapis.com/maps/api/js?v=3.exp&libraries=visualization"></script>
<script type="text/javascript" src="http://code.jquery.com/jquery-2.1.0.min.js"></script>
<script>

	var map, heatmap, pointArray;
	var locData = [];
	function initialize() {
		var mapOptions = {
			center: new google.maps.LatLng(37.774546, -122.433523),
			zoom: 3,
    		mapTypeId: google.maps.MapTypeId.SATELLITE
  		};
		
  		$.getJSON('<%= request.getContextPath() %>' + "/TweetMapServer/", function(json){
	  		var latlng = json.latlon;
	  		for (var i = 0; i < latlng.length; i++) {
		    	var str = latlng[i];
		    	var res = str.split(" "); 
		    	locData.push(new google.maps.LatLng(parseFloat(res[0]), parseFloat(res[1])));
			}
	  		map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
			pointArray = new google.maps.MVCArray(locData); 
			heatmap = new google.maps.visualization.HeatmapLayer({data: pointArray});
			heatmap.setMap(map); 
		});
	}
	
	google.maps.event.addDomListener(window, 'load', initialize);
</script>
</head>
<body>
	<div id="map-canvas"></div>
</body>
</html>