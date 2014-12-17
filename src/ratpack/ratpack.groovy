import com.github.rahulsom.docsource.MedicationHandler
import com.github.rahulsom.docsource.RandomPatientHandler
import com.github.rahulsom.docsource.SubmissionHandler
import com.github.rahulsom.docsource.VitalSignHandler
import com.github.rahulsom.docsource.maas.MaasModule
import ratpack.groovy.templating.TemplatingModule
import ratpack.jackson.JacksonModule

import static ratpack.groovy.Groovy.groovyTemplate
import static ratpack.groovy.Groovy.ratpack

if (!new File('data').exists()) {
	new File('data').mkdirs()
}
ratpack {

	bindings {
		add new JacksonModule()
		add(TemplatingModule) { TemplatingModule.Config config ->
			config.staticallyCompile = true
		}
		add new MaasModule()
	}

	handlers {

		get('') {
			render groovyTemplate([:], "index.html")
		}

		get('random-patient', new RandomPatientHandler('910 Hamilton Ave, Campbell, CA 95008'))
		get('medication', MedicationHandler)
		get('vital', VitalSignHandler)

		post('send', SubmissionHandler)

		assets "public"

	}
}
