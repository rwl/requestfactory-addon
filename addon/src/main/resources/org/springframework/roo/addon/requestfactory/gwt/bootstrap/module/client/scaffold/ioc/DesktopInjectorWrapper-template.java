package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import com.google.gwt.core.client.GWT;

public class DesktopInjectorWrapper implements InjectorWrapper {

    public Injector getInjector() {
        return GWT.create(DesktopInjector.class);
    }
}
