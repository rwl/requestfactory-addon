package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;


public class AsyncActivityProxy<T extends Activity> implements Activity {

    @Inject private AsyncProvider<T> provider;
    private boolean canceled = false;
    private Activity activity;

    @Override
    public String mayStop() {
        return activity != null ? activity.mayStop() : null;
    }

    @Override
    public void onCancel() {
        if (activity != null) {
            activity.onCancel();
        } else {
            canceled = true;
        }
    }

    @Override
    public void onStop() {
        if (activity != null) {
            activity.onStop();
        } else {
            canceled = true;
        }
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        if (activity != null) {
            activity.start(panel, eventBus);
            return;
        }
        provider.get(new AsyncCallback<T>() {

            @Override
            public void onSuccess(T result) {
                if (!canceled) {
                    activity = result;
                    activity.start(panel, eventBus);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Error downloading code: " + caught.getMessage());
            }
        });
    }
}
