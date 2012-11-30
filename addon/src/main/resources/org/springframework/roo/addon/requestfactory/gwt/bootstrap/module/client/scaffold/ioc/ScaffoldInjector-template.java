package __TOP_LEVEL_PACKAGE__.ioc;

import __TOP_LEVEL_PACKAGE__.app.ScaffoldApp;
import com.google.gwt.inject.client.Ginjector;

public interface ScaffoldInjector extends Ginjector {

	ScaffoldApp getScaffoldApp();
}
