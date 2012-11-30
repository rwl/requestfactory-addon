package __TOP_LEVEL_PACKAGE__.ioc;

import __TOP_LEVEL_PACKAGE__.app.ScaffoldMobileApp;
import com.google.gwt.inject.client.GinModules;

@GinModules(value = {ScaffoldModule.class})
public interface MobileInjector extends ScaffoldInjector {

	ScaffoldMobileApp getScaffoldApp();
}
