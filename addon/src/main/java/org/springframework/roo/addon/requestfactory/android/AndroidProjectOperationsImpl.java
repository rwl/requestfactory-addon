package org.springframework.roo.addon.requestfactory.android;

import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_ACTIVITY;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;

@Component
@Service
public class AndroidProjectOperationsImpl implements AndroidProjectOperations {

    @Reference TypeLocationService typeLocationService;
    @Reference ProjectOperations projectOperations;
    
    @Override
    public boolean isActivityAvailable() {
        return projectOperations.isFocusedProjectAvailable();
    }

    @Override
    public boolean isViewAvailable() {
        return projectOperations.isFocusedProjectAvailable()
                && typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_ACTIVITY)
                .size() > 0;
    }

    @Override
    public void activity(final JavaType type, final String layout) {

    }

    @Override
    public void view(final JavaType type, final JavaType view,
            final String identifier, final JavaSymbolName fieldName,
            final Dimension height, final Dimension width) {

    }

    @Override
    public void resourceString(final JavaType type, final String name,
            final JavaSymbolName fieldName, final String value,
            final Dimension height, final Dimension width) {

    }
}
