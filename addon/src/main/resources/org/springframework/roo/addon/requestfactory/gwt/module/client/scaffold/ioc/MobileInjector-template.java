package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import __TOP_LEVEL_PACKAGE__.application.MobileApplication;
import com.google.gwt.inject.client.GinModules;

@GinModules(value = {Module.class})
public interface MobileInjector extends Injector {

	MobileApplication getApplication();
}
