package com.github.rahulsom.docsource

import com.github.rahulsom.genealogy.NameDbUsa
import com.github.rahulsom.genealogy.Person
import com.github.rahulsom.geocoder.coder.GeonamesCoder
import com.github.rahulsom.geocoder.coder.GoogleCoder
import com.github.rahulsom.geocoder.domain.Address
import com.github.rahulsom.geocoder.domain.LatLng
import ratpack.handling.Context
import ratpack.handling.Handler

import java.security.SecureRandom

import static groovy.json.JsonOutput.toJson

/**
 * Created by rahul on 11/4/14.
 */
class RandomPatientHandler implements Handler {
	double lat, lng
	RandomPatientHandler(String baseAddress) {
		(lat, lng) = new GoogleCoder().encode(baseAddress).with {
			[it.lat, it.lng]
		}
	}
	def nameDb = NameDbUsa.instance
	def rand = new Random(new SecureRandom().nextLong())

	@Override
	void handle(Context context) throws Exception {
		def person = nameDb.person
		Address add = null
		def dob = new Date() - (Math.abs(rand.nextInt()) % (365 * 80))
		context.background {
			int i = 0
			while (!add) {
				add = new GeonamesCoder('rahulsom').decode(
						new LatLng(lat + rand.nextGaussian() / 10, lng + rand.nextGaussian() / 10))
				if (++i > 3) {
					break
				}
			}
		}.then {
			context.render toJson(patientToMap(person, add, dob))
		}
	}

	private LinkedHashMap<String, Serializable> patientToMap(Person person, Address add, Date dob) {

		[
				firstName: person.firstName,
				lastName : person.lastName,
				gender   : person.gender,
				race     : person.race,
				dob      : dob.format('yyyy-MM-dd'),
				address  : add ? [
						street : add.street,
						city   : add.city,
						state  : add.state,
						country: add.country,
						zipCode: add.zip
				] : null
		]
	}

}
