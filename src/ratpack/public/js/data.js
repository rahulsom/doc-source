var vitalSignsSource = createLoincDataSource('vital-signs', '/vital');
var labResultsSource = createLoincDataSource('lab-results', '/lab');
var medicationsSource = createNdcDataSource('medications', '/medication');

angular.module('docSourceApp', []).controller('DocSourceController', ['$scope', function ($scope) {
	$scope.request = {
		vitalSigns: [],
		medications: [],
		labResults: [],
		patient: {},
		repositoryUrl: 'http://localhost:8080/healthdock/alpine/repository',
		isProcessing: '',
		status: '',
		author: {
			name        : 'Sherry DoppleMeyer',
			institution : 'Good health clinic',
			specialty   : 'Orthopedic',
			role        : 'Attending'

		}
	};

	enableVideoSupport($scope);

	$scope.deleteVitalSign = function ($index) {
		if ($index > -1) {
			$scope.request.vitalSigns.splice($index, 1);
		}
	};

	$scope.deleteLabResult = function ($index) {
		if ($index > -1) {
			$scope.request.labResults.splice($index, 1);
		}
	};

	$scope.submitRequest = function () {
		$scope.request.isProcessing = 'disabled';
		$scope.request.status = 'Processing...';
		$.ajax({
			url: '/send',
			type: "POST",
			data: JSON.stringify($scope.request),
			contentType: "application/json; charset=utf-8",
			dataType: "json",
			success: function (data, status, xhr) {
				console.log(data);
				console.log(status);
				$scope.$apply(function(){
					$scope.request.isProcessing = null;
					if (data.status == 'Success') {
						$scope.request.status = data.status;
					} else {
						$scope.request.status = data.status + ': ' + data.message;
					}
				});

			},
			error: function (xhr, status, error) {
				console.log(xhr);
				console.log(status);
				console.log(error);
				$scope.$apply(function(){
					$scope.request.isProcessing = null;
					$scope.request.status = error;
				});
			}
		});
	};

	/*
	 * Typeahead for vital signs
	 */
	$('#newVitalSign')
			.typeahead(globalTypeAheadOptions, vitalSignsSource)
			.on('typeahead:selected', function (event, loinc, dataset) {
				$scope.$apply(function () {
					$scope.request.vitalSigns.push({
						text: loinc.longCommonName,
						code: loinc.id,
						units: loinc.units,
						unit: loinc.unit
					});
				});
			});

	/*
	 * Typeahead for lab results
	 */
	$('#newLabResult')
			.typeahead(globalTypeAheadOptions, labResultsSource)
			.on('typeahead:selected', function (event, loinc, dataset) {
				$scope.$apply(function () {
					$scope.request.labResults.push({
						text: loinc.longCommonName,
						code: loinc.id,
						units: loinc.units,
						unit: loinc.unit
					});
				});
			});

	/*
	 * Typeahead for medications
	 */
	$('#newMedication')
			.typeahead(globalTypeAheadOptions, medicationsSource)
			.on('typeahead:selected', function (event, ndcProduct, dataset) {
				$scope.$apply(function () {
					$scope.request.medications.push(ndcProduct);
				});
			});

	/*
	 * Random Demographics support
	 */
	$('#random-demographics').click(function () {
		$.getJSON('/random-patient', function (data, status, xhr) {
			$scope.$apply(function () {
				data.dob = new Date(data.dob);
				$.extend($scope.request.patient, data);
			});
			console.log("Updated patient data");
		});
		console.log("Clicked random");
	});

}]);

