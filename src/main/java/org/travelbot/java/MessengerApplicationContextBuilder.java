package org.travelbot.java;

import org.joo.scorpius.ApplicationContext;
import org.joo.scorpius.support.builders.ApplicationContextBuilder;

public class MessengerApplicationContextBuilder extends ApplicationContextBuilder {

	@Override
	public ApplicationContext build() {
		return new MessengerApplicationContext(getInjector());
	}
}
