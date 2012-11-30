package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Window;

import com.googlecode.mgwt.ui.client.MGWT;

import __TOP_LEVEL_PACKAGE__.ioc.DesktopInjectorWrapper;
import __TOP_LEVEL_PACKAGE__.ioc.MobileInjectorWrapper;
import __TOP_LEVEL_PACKAGE__.ioc.InjectorWrapper;


public class ApplicationEntryPoint implements EntryPoint {

    private static final Logger LOGGER = Logger.getLogger(ApplicationEntryPoint.class.getName());

    public void onModuleLoad() {
        GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable e) {
                Window.alert("Error: " + e.getMessage());
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        });
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    /* Get and run platform specific app */
                    final InjectorWrapper injectorWrapper;
                    if (MGWT.getOsDetection().isDesktop()) {
                        injectorWrapper = GWT.create(DesktopInjectorWrapper.class);
                    } else {
                        injectorWrapper = GWT.create(MobileInjectorWrapper.class);
                    }
                    injectorWrapper.getInjector().getApplication().run();
                }
        });
    }
}
