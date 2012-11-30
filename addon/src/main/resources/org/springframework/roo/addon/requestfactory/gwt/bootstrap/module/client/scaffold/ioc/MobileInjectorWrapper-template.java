package __TOP_LEVEL_PACKAGE__.ioc;

import com.google.gwt.core.client.GWT;

public class MobileInjectorWrapper implements InjectorWrapper {

	@Override
	public Injector getInjector() {
		return GWT.create(MobileInjector.class);
	}
}
