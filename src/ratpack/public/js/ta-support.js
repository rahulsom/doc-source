var globalTypeAheadOptions = {
	highlight: true,
	minLength: 3
};

function createLoincDataSource(name, url) {
	var LoincFromHal = function (resp) {
		if (!resp._embedded) {
			return [];
		}
		_.each(resp._embedded.loinc, function (loinc) {
			var parts = loinc._links.self.href.split('/');
			loinc.id = parts[parts.length - 1];

			if (loinc.exampleUcumUnits) {
				loinc.units = loinc.exampleUcumUnits.split(';');
				loinc.unit = loinc.units[0];
			} else {
				loinc.units = [];
			}

		});
		return resp._embedded.loinc;
	};

	var bloodHound = new Bloodhound({
		datumTokenizer: Bloodhound.tokenizers.obj.whitespace('value'),
		queryTokenizer: Bloodhound.tokenizers.whitespace,
		remote: {
			url: url + '?q=%QUERY*',
			filter: LoincFromHal
		}
	});

	bloodHound.initialize();

	return {
		name: name,
		displayKey: 'shortName',
		source: bloodHound.ttAdapter(),
		templates: {
			empty: [
				'<div class="empty-message">',
				'Unable to find any term matching current query',
				'</div>'
			].join('\n'),
			suggestion: Handlebars.compile('<p><strong>{{longCommonName}}</strong> – {{id}}</p>')
		}
	};
}
function createNdcDataSource(name, url) {
	var NdcFromHal = function (resp) {
		if (!resp._embedded) {
			return [];
		}
		_.each(resp._embedded.ndcProduct, function (ndcProduct) {
			var parts = ndcProduct._links.self.href.split('/');
			ndcProduct.id = parts[parts.length - 1];
		});
		return resp._embedded.ndcProduct;
	};

	var bloodHound = new Bloodhound({
		datumTokenizer: Bloodhound.tokenizers.obj.whitespace('value'),
		queryTokenizer: Bloodhound.tokenizers.whitespace,
		remote: {
			url: url + '?q=%QUERY*',
			filter: NdcFromHal
		}
	});

	bloodHound.initialize();

	return {
		name: name,
		displayKey: 'proprietaryName',
		source: bloodHound.ttAdapter(),
		templates: {
			empty: [
				'<div class="empty-message">',
				'Unable to find any term matching current query',
				'</div>'
			].join('\n'),
			suggestion: Handlebars.compile('<p><strong>{{proprietaryName}}</strong> {{nonProprietaryName}} {{dosageFormName}} {{activeNumeratorStrength}}</p>')
		}
	};
}
