package __TOP_LEVEL_PACKAGE__.ioc;

import __TOP_LEVEL_PACKAGE__.application.DesktopApplication;
import com.google.gwt.inject.client.GinModules;

@GinModules(value = {Module.class})
public interface DesktopInjector extends Injector {

	DesktopApplication getApplication();
}