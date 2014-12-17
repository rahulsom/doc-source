package com.github.rahulsom.docsource.maas

import com.fasterxml.jackson.databind.ObjectReader
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import ratpack.launch.LaunchConfig

/**
 * Created by rahul on 11/24/14.
 */
class MaasModule extends AbstractModule {

	@Provides
	@Singleton
	MaasClient maasClient(LaunchConfig launchConfig, ObjectReader reader) {
		println "Creating maas client..."
		new MaasClient(reader, launchConfig.getOther('maas.location', 'http://localhost:8090/maas'), launchConfig)
	}

	@Override
	protected void configure() {

	}

}
