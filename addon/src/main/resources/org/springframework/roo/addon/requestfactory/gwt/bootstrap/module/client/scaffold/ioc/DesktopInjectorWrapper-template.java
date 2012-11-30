package __TOP_LEVEL_PACKAGE__.ioc;

import com.google.gwt.core.client.GWT;

public class DesktopInjectorWrapper implements InjectorWrapper {

	public Injector getInjector() {
		return GWT.create(DesktopInjector.class);
	}
}
