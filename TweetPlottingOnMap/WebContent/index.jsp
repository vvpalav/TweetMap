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
</style>
<script src="https://maps.googleapis.com/maps/api/js?libraries=visualization"></script>
<script src="https://maps.googleapis.com/maps/api/js"></script>
<script type="text/javascript" src="http://code.jquery.com/jquery-2.1.0.min.js"></script>
<script>

	function initialize() {
		var mapOptions = {
			center: new google.maps.LatLng(37.774546, -122.433523),
			zoom: 3,
			mapTypeId:google.maps.MapTypeId.TERRAIN
  		};
		var map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
		var locData = [];
		var marker, point;
		/* point = new google.maps.LatLng(37.774546, -122.433523);
    	marker = new google.maps.Marker({
            position: point,
            map: map
          }); */
  		$.getJSON( '<%= request.getContextPath() %>' + "/TweetMapServer", function(json){
  			point = new google.maps.LatLng(parseDouble(res[0]), parseDouble(res[1]));
	    	marker = new google.maps.Marker({
	            position: point,
	            map: map
	          }); 
	  		var latlng = json.latlon;
	  		for (var i = 0; i < latlng.length; i++) {
		    	var str = latlng[i];
		    	var res = str.split(" "); 
		    	locData.push(new google.maps.LatLng(parseDouble(res[0]), parseDouble(res[1])));
		    	
			}
	  		
	  		point = new google.maps.LatLng(37.774546, -122.433523);
	    	marker = new google.maps.Marker({
	            position: point,
	            map: map
	       	});
	  		
			var pointArray = new google.maps.MVCArray(locData); 
			var heatmap = new google.maps.visualization.HeatmapLayer({data: pointArray});
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