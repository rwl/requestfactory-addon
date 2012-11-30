package __TOP_LEVEL_PACKAGE__.ioc;

import __TOP_LEVEL_PACKAGE__.application.Application;
import com.google.gwt.inject.client.Ginjector;

public interface Injector extends Ginjector {

    Application getApplication();
}
