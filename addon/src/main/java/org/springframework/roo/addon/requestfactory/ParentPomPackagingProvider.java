package org.springframework.roo.addon.requestfactory;

import java.util.Collection;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.packaging.AbstractPackagingProvider;
import org.springframework.roo.project.packaging.PackagingProvider;
import org.w3c.dom.Document;

/**
 * A simple Maven "pom" {@link PackagingProvider}
 *
 */
@Component
@Service
public class ParentPomPackagingProvider extends AbstractPackagingProvider {

    /**
     * Constructor invoked by the OSGi container
     */
    public ParentPomPackagingProvider() {
        super("parent_pom", "pom", "parent-pom-template.xml");
    }

    @Override
    protected void createOtherArtifacts(final JavaPackage topLevelPackage,
            final String module, final ProjectOperations projectOperations) {
        // No artifacts are applicable for parent POM modules
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    @Override
    protected final void setPackagingProviderId(final Document pom) {
        // Not needed, as the provider uses the Maven packaging name as
        // the ID.
    }

    @Override
    public Collection<Path> getPaths() {
        return null;
    }
}
