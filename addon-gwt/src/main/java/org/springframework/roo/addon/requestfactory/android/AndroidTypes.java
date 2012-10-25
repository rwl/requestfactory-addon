package org.springframework.roo.addon.requestfactory.android;

import org.springframework.roo.addon.requestfactory.RequestFactoryType;

public class AndroidTypes {

    public static final RequestFactoryType DETAIL_ACTIVITY = new RequestFactoryType(AndroidPaths.MANAGED_ACTIVITY, true, "DetailsActivity", "detailsActivity", "DetailsActivity", true, false);

    public static final RequestFactoryType[] ALL_TYPES = new RequestFactoryType[] {
        DETAIL_ACTIVITY
    };

    private AndroidTypes() {
    }
}
