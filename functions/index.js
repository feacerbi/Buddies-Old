const functions = require('firebase-functions');
const admin = require("firebase-admin");
admin.initializeApp(functions.config().firebase);

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

exports.addNewFollowPosts =
	functions.database.ref('users/{userId}/follows/{followId}')
	.onWrite(event => {
		if (!event.data.val()) {
            console.log("not a valid event");
            return;
        }
		
		var userId = event.params.userId;
		var followId = event.params.followId;
		
		const getFollowPosts = admin.database().ref('pets/' + followId + "/posts").once('value');
		
		return Promise.all([getFollowPosts]).then(
			results => {
				const followPostsSnapshot = results[0];
				
				var promises = [];
				
				followPostsSnapshot.forEach(
					followPostSnapshot => {
						var postKey = followPostSnapshot.key;
						promises[promises.length] = addPostPromise(userId, postKey);
					})
					
				return Promise.all(promises);
			})
	});

exports.handleRequests =
	functions.database.ref('requests/{requestId}')
	.onWrite(event => {
		
		var requestId = event.params.requestId;
		var petId = event.data.child("petId").val();
		
		const getPetOwnersPromise = admin.database().ref('pets/' + petId + "/owns").once('value');
		
		return Promise.all([getPetOwnersPromise]).then(
			results => {
				const ownsSnapshot = results[0];
				
				var promises = [];
				
				ownsSnapshot.forEach(
					ownerSnapshot => {
						var ownerKey = ownerSnapshot.key;
						if(event.data.val()) {
							promises[promises.length] = addRequestPromise(requestId, ownerKey);
						} else {
							promises[promises.length] = removeRequestPromise(requestId, ownerKey);
						}
					})
					
				return Promise.all(promises);
			})
	});
	
function addRequestPromise(requestId, userKey) {
	var userRequestsRef = admin.database().ref('users/' + userKey + "/requests/" + requestId);
	var addRequest = userRequestsRef.set(true);
	
	return Promise.all([addRequest]);
}
	
function removeRequestPromise(requestId, userKey) {
	var userRequestsRef = admin.database().ref('users/' + userKey + "/requests/" + requestId);
	var removeRequest = userRequestsRef.set(null);
	
	return Promise.all([removeRequest]);
}

exports.syncPostToFollows =
	functions.database.ref('pets/{petId}/posts/{postId}')
	.onWrite(event => {
		
		var petId = event.params.petId;
		var postId = event.params.postId;
		
		const getFollowsPromise = admin.database().ref('pets/' + petId + "/follows").once('value');
		
		return Promise.all([getFollowsPromise]).then(
			results => {
				const followsSnapshot = results[0];
				
				var promises = [];
				
				followsSnapshot.forEach(
					followSnapshot => {
						var followKey = followSnapshot.key;
						if(event.data.val()) {
							promises[promises.length] = addPostPromise(followKey, postId);
						} else {
							promises[promises.length] = removePostPromise(followKey, postId);
						}
					})
					
				return Promise.all(promises);
			})
	});
	
function addPostPromise(userKey, postId) {
	var userPostsRef = admin.database().ref('users/' + userKey + "/posts/" + postId);
	var addPost = userPostsRef.set(true);
	
	return Promise.all([addPost]);
}

function removePostPromise(userKey, postId) {
	var userPostsRef = admin.database().ref('users/' + userKey + "/posts/" + postId);
	var addPost = userPostsRef.set(null);
	
	return Promise.all([addPost]);
}

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
