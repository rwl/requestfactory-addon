package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import com.google.gwt.core.client.GWT;

public class MobileInjectorWrapper implements InjectorWrapper {

    @Override
    public Injector getInjector() {
        return GWT.create(MobileInjector.class);
    }
}
