$(function () {
	$('.navsection').css('min-height', $(window).height());

	$('#random-demographics').click(function () {
		$.getJSON('/random-patient', function (data, status, xhr) {
			$('#firstName').val(data.firstName);
			$('#lastName').val(data.lastName);
			$('#gender').val(data.gender);
			$('#dob').val(data.dob);
			$('#race').val(data.race);
			if (data.address) {
				$('#street').val(data.address.street);
				$('#city').val(data.address.city);
				$('#state').val(data.address.state);
				$('#zip').val(data.address.zipCode);
				$('#country').val(data.address.country);
			}
		});
		console.log("Clicked random");
		return false;
	});

});

/*
 * Video support
 */
$(function(){
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
			// "image/webp" works in Chrome.
			// Other browsers will fall back to image/png.
			document.querySelector('img').src = canvas.toDataURL('image/jpg');
			localMediaStream.stop();
			$('video').hide();
			$('#photo').show();
		}
	}

	video.addEventListener('click', snapshot, false);
});
