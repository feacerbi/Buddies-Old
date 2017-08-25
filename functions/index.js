const functions = require('firebase-functions');
const admin = require("firebase-admin");
admin.initializeApp(functions.config().firebase);

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

function sendMessagePromise(idToken, payload) {
	console.log("Sending message to " + idToken + "...");
	sendPromise = admin.messaging().sendToDevice(idToken, payload)
		.then(function(response) {
			console.log("Successfully sent message:", response);
        })
		.catch(function(error) {
			console.log("Error sending message:", error);
        });
	
	return Promise.all([sendPromise]);
}

exports.addNewFollowPosts =
	functions.database.ref('users/{userId}/follows/{followId}')
	.onWrite(event => {
		if (!event.data.val()) {
            console.log("not a valid event");
            return;
        }
		
		var userId = event.params.userId;
		var followId = event.params.followId;
		
		const getUsers = admin.database().ref('users').once('value');
		const getFollowedPet = admin.database().ref('pets/' + followId).once('value');
		
		return Promise.all([getUsers, getFollowedPet]).then(
			results => {
				const usersSnapshot = results[0];
				const petSnapshot = results[1];
				
				var promises = [];
				
				petSnapshot.child("posts").forEach(
					followPostSnapshot => {
						var postKey = followPostSnapshot.key;
						promises[promises.length] = addPostPromise(userId, postKey);
					})
					
				var followerName = usersSnapshot.child(userId).child("name").val();
				var followerPhoto = usersSnapshot.child(userId).child("photo").val();
				var petName = petSnapshot.child("name").val();
		
				var payload = {
					notification: {
						title: "New follower!",
						body: followerName + " started following " + petName + "."
					}
				};
				
				petSnapshot.child("owns").forEach(
					ownerSnapshot => {
						var ownerKey = ownerSnapshot.key;
						var idToken = usersSnapshot.child(ownerKey).child("idToken").val();
						promises[promises.length] = sendMessagePromise(idToken, payload);
					})
					
				return Promise.all(promises);
			})
	});

exports.handleRequests =
	functions.database.ref('requests/{requestId}')
	.onWrite(event => {
		
		var requestId = event.params.requestId;
		var requesterId = event.data.child("username").val();
		var petId = event.data.child("petId").val();
		
		const getUsers = admin.database().ref('users').once('value');
		const getPet = admin.database().ref('pets/' + petId).once('value');
		
		return Promise.all([getUsers, getPet]).then(
			results => {
				const usersSnapshot = results[0];
				const petSnapshot = results[1];
				
				var petName = petSnapshot.child("name").val();
				var requesterName = usersSnapshot.child(requesterId).child("name").val();
				var requesterPhoto = usersSnapshot.child(requesterId).child("photo").val();
				
				var payload = {
					notification: {
						title: "New owner request!",
						body: requesterName + " wants to be owner of " + petName
					}
				};
				
				var promises = [];
				
				petSnapshot.child("owns").forEach(
					ownerSnapshot => {
						var ownerKey = ownerSnapshot.key;
						var ownerIdToken = usersSnapshot.child(ownerKey).child("idToken").val();
						if(event.data.val()) {
							promises[promises.length] = addRequestPromise(requestId, ownerKey);
							promises[promises.length] = sendMessagePromise(ownerIdToken, payload);
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
