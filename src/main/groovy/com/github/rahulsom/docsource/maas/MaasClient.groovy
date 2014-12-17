package com.github.rahulsom.docsource.maas

import com.fasterxml.jackson.databind.ObjectReader
import groovyx.net.http.URIBuilder
import ratpack.func.Action
import ratpack.http.client.HttpClient
import ratpack.http.client.HttpClients
import ratpack.http.client.RequestSpec
import ratpack.launch.LaunchConfig

/**
 * Interacts with a MAAS server.
 */
class MaasClient {

	public static final Action<RequestSpec> MaasHeaders = new Action<RequestSpec>() {
		@Override
		void execute(RequestSpec requestSpec) throws Exception {
			requestSpec.headers.add('Authorization', "Basic ${'user:user'.bytes.encodeBase64()}")
			requestSpec.headers.add('Accept', "application/hal+json")
		}
	}

	private final HttpClient   httpClient
	private final String       address
	private final ObjectReader reader

	MaasClient(ObjectReader reader, String address, LaunchConfig launchConfig) {
		this.reader = reader
		this.address = address
		httpClient = HttpClients.httpClient(launchConfig)
	}

	def loinc(String query, Closure action) {
		URI uri = new URIBuilder(address + "/loinc").addQueryParam('q', query).toURI()
		httpClient.get(uri, MaasHeaders).then { it ->
			if (it.statusCode == 200) {
				def tree = reader.readTree(it.body.inputStream)
				action(tree)
			} else {
				action([error: 'Failed'])
			}
		}
	}

	def ndc(String query, Closure action) {
		URI uri = new URIBuilder(address + "/ndc").addQueryParam('q', query).toURI()
		httpClient.get(uri, MaasHeaders).then { it ->
			def tree = reader.readTree(it.body.inputStream)
			action(tree)
		}
	}

}
