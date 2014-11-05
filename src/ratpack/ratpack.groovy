import com.github.rahulsom.docsource.RandomPatientHandler
import com.github.rahulsom.genealogy.NameDbUsa
import com.github.rahulsom.genealogy.Person
import com.github.rahulsom.geocoder.coder.GeonamesCoder
import com.github.rahulsom.geocoder.coder.GoogleCoder
import com.github.rahulsom.geocoder.domain.Address
import com.github.rahulsom.geocoder.domain.LatLng

import java.security.SecureRandom

import static groovy.json.JsonOutput.toJson
import static ratpack.groovy.Groovy.*

ratpack {
	handlers {
		get('') {
			render groovyTemplate([:], "index.html", 'html')
		}

		get('random-patient', new RandomPatientHandler('910 Hamilton Ave, Campbell, CA 95008'))

		post('send') {

		}

		assets "public"
	}
}
