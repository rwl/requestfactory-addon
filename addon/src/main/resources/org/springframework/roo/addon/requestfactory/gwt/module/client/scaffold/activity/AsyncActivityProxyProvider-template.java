package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import com.google.gwt.activity.shared.Activity;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class AsyncActivityProxyProvider<T extends Activity> implements Provider<AsyncActivityProxy<T>> {

    @Inject
    Provider<AsyncActivityProxy<T>> provider;

    @Override
    public AsyncActivityProxy<T> get() {
        return provider.get();
    }
}
