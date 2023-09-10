    var map;
	const myLatLng = { lat: 18.5449, lng: 73.7648 };
	async function initMap() {
	  const { Map } = await google.maps.importLibrary("maps");
	  map = new Map(document.getElementById("map"), {
	    center: myLatLng,
	    zoom: 15,
	  });
	  new google.maps.Marker({
	    position: myLatLng,
	    map,
	    title: "Demo Location",
	  });

	  map.addListener("click", (e) => {
      	if(e.placeId){
      	    const placeId = e.placeId
      	    getPlaceDetails(placeId)
      	    e.stop()
      	  }
      	});

	}

    function recenterMap() {
        map.setCenter(myLatLng);
        Android.recenterMap();
    }
    function enableDarkMode(darkModeStyles) {
        map.setOptions({styles: darkModeStyles});
    }
    function resetStyle() {
        map.setOptions({styles: null});
    }
    	async function getPlaceDetails(placeId){
    	  const request = {
    	    placeId: placeId,
    	    fields: ["name", "formatted_address", "geometry"],
    	  };
    	    const infoWindow = new google.maps.InfoWindow();
    	    const service = new google.maps.places.PlacesService(map);
    	    service.getDetails(request, (place, status) => {
    		    if (
    		      status === google.maps.places.PlacesServiceStatus.OK &&
    		      place &&
    		      place.geometry &&
    		      place.geometry.location
    		    ) {
    			const content = document.createElement("div");

    			const nameElement = document.createElement("h3");
    			nameElement.textContent = place.name;
    			content.appendChild(nameElement);

    			const placeAddressElement = document.createElement("p");
    			placeAddressElement.textContent = place.formatted_address;
    			content.appendChild(placeAddressElement);

    			const openInMapElement = document.createElement("p");
    			openInMapElement.textContent = "Open in Map view";
      			openInMapElement.style.color = "blue";
      		    openInMapElement.addEventListener("click", () => {
                    Android.openMap(place.geometry.location.lat(), place.geometry.location.lng())
      			});
      			content.appendChild(openInMapElement);

    			infoWindow.setContent(content);
    			infoWindow.setPosition({lat: place.geometry.location.lat(), lng: place.geometry.location.lng()});
    			infoWindow.open(map);
    		    }
    		  });
    	}