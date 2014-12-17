/*
 * Video support
 */
var enableVideoSupport = function ($scope) {
	var errorCallback = function (e) {
		console.log('Reeeejected!', e);
	};

	navigator.getUserMedia = navigator.getUserMedia ||
	navigator.webkitGetUserMedia ||
	navigator.mozGetUserMedia ||
	navigator.msGetUserMedia;

	var video = document.querySelector('video');
	var canvas = document.querySelector('canvas');
	var constraints = {
		audio: false,
		video: {
			mandatory: {
				minWidth: 1280,
				minHeight: 720
			}
		}
	};
	canvas.width = 1280;
	canvas.height = 720;
	var ctx = canvas.getContext('2d');

	var localMediaStream = null;
	if (navigator.getUserMedia) {
		navigator.getUserMedia(constraints, function (stream) {
			video.src = window.URL.createObjectURL(stream);
			localMediaStream = stream;
		}, errorCallback);
	} else {
		video.src = 'somevideo.webm'; // fallback.
	}

	function snapshot() {
		if (localMediaStream) {
			ctx.drawImage(video, 0, 0);
			document.querySelector('img').src = canvas.toDataURL('image/jpeg');
			localMediaStream.stop();
			$('video').hide();
			$('#photo').show();
			$scope.request.patient.photo = canvas.toDataURL('image/jpeg');
		}
	}

	video.addEventListener('click', snapshot, false);
};
