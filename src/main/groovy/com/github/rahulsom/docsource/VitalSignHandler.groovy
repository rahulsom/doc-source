package com.github.rahulsom.docsource

import com.github.rahulsom.docsource.maas.MaasClient
import com.google.inject.Inject
import ratpack.handling.Context
import ratpack.handling.Handler

import static ratpack.jackson.Jackson.json

/**
 * Created by rahul on 11/19/14.
 */
class VitalSignHandler implements Handler {

	MaasClient maasClient

	@Inject
	VitalSignHandler(MaasClient maasClient) {
		this.maasClient = maasClient
	}

	@Override
	void handle(Context context) throws Exception {
		context.with {
			def query = request.queryParams['q']
			maasClient.loinc("(${query}) AND class_:*.ATOM AND timeAspct:Pt") {
				render json(it)
			}
		}
	}

}
