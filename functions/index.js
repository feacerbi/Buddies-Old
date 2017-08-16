const functions = require('firebase-functions');
const admin = require("firebase-admin");
admin.initializeApp(functions.config().firebase);

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

exports.listUserPlaces =
	functions.database.ref('users/{userId}/latlong')
	.onWrite(event => {
		if (!event.data.val()) {
            console.log("not a valid event");
            return;
        }
		
		var userId = event.params.userId;
		var userLat = event.data.child("latitude").val();
		console.log("user lat: " + userLat);
		var userLong = event.data.child("longitude").val();
		console.log("user lat: " + userLong);
		
		var removeUserPlacesPromise = admin.database().ref('users/' + userId + "/places").set(null);
		const getPlacesPromise = admin.database().ref('places').once('value');
		
		return Promise.all([removeUserPlacesPromise, getPlacesPromise]).then(
			results => {
				const placesSnapshot = results[1];
				
				var promises = [];
				
				placesSnapshot.forEach(
					placeSnapshot => {
						var placeLat = placeSnapshot.child("latlong").child("latitude").val();
						console.log("place lat: " + placeLat);
						var placeLong = placeSnapshot.child("latlong").child("longitude").val();
						console.log("place long: " + placeLong);
						
						var distance = getDistance(placeLat, userLat, placeLong, userLong);
						
						var placeKey = placeSnapshot.key;
						console.log("place key: " + placeKey);
						console.log("place distance: " + distance);
						
						promises[promises.length] = createUserPlacePromise(userId, placeKey, distance);
					})
					
				return Promise.all(promises);
			})
	});
	
function createUserPlacePromise(userKey, placeKey, distance) {
	var userPlacesRef = admin.database().ref('users/' + userKey + "/places/" + placeKey);
	var addPlace = userPlacesRef.set(distance);
	
	return Promise.all([addPlace]);
}

function getDistance(lat1, lat2, long1, long2) {
    var R = 6371e3; // metres
    var f1 = lat1 * Math.PI / 180;
    var f2 = lat2 * Math.PI / 180;
    var df = (lat2 - lat1) * Math.PI / 180;
    var dl = (long2 - long1) * Math.PI / 180;

    var a = Math.sin(df/2) * Math.sin(df/2) + Math.cos(f1) * Math.cos(f2) * Math.sin(dl/2) * Math.sin(dl/2);
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

    return Math.round(R * c);
}
