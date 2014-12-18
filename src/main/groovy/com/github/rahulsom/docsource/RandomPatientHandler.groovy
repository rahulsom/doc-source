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

import static ratpack.jackson.Jackson.json

/**
 * Created by rahul on 11/4/14.
 */
class RandomPatientHandler implements Handler {
	public static final String GEOLOCATION = /-?\d+.\d+,-?\d+.\d+/

	String filename
	double lat, lng

	RandomPatientHandler(String baseAddress) {
		filename = baseAddress.replaceAll('[^a-zA-Z0-9 ]','').replace(' ', '-')
		def file = new File("data/${filename}.csv")
		if (file.exists() && file.text =~ GEOLOCATION) {
			(lat, lng) = file.text.split(',').collect {Double.parseDouble(it)}
			println "Loaded (lat,lng) from file as ($lat,$lng)"
		} else {
			(lat, lng) = new GoogleCoder().encode(baseAddress).with { [it.lat, it.lng] }
			println "Initialized (lat,lng) from google as ($lat,$lng)"
			file.text = "${lat},${lng}"
		}
	}
	def nameDb = NameDbUsa.instance
	def rand   = new Random(new SecureRandom().nextLong())

	@Override
	void handle(Context context) throws Exception {
		def person = nameDb.person
		def dob = new Date() - (Math.abs(rand.nextInt()) % (365 * 80))
		context.blocking { promise ->
			Address address = null
			for (int i = 0; i < 3; i++) {
				def latLng = new LatLng(lat + rand.nextGaussian() / 10, lng + rand.nextGaussian() / 10)
				address = new GeonamesCoder('rahulsom').decode(latLng)
				if (address) {
					break
				}
			}
			address
		}.then { add ->
			context.render json(patientToMap(person, add, dob))
		}
	}

	private LinkedHashMap<String, Serializable> patientToMap(Person person, Address add, Date dob) {

		[
				firstName: person.firstName,
				lastName : person.lastName,
				gender   : person.gender,
				race     : person.race,
				dob      : dob.format('yyyy-MM-dd'),
				address  : add ? addressToMap(add) : null
		]
	}

	private LinkedHashMap<String, String> addressToMap(Address add) {
		[
				street : add.street,
				city   : add.city,
				state  : add.state,
				country: add.country,
				zipCode: add.zip
		]
	}

}
