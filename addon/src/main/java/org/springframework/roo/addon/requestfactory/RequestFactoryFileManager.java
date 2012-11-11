package org.springframework.roo.addon.requestfactory;

import java.util.List;

import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;

/**
 * Provides a basic implementation of {@link RequestFactoryFileManager} which encapsulates
 * the file management functionality required by
 * {@link org.springframework.roo.addon.requestfactory.scaffold.RequestFactoryScaffoldMetadataProviderImpl}
 * .
 * 
 * @author James Tyrrell
 * @since 1.1.1
 */
public interface RequestFactoryFileManager {

    String write(ClassOrInterfaceTypeDetails typeDetails, boolean includeWarning);

    /**
     * Writes the given Java type to disk in the user project
     * 
     * @param typeDetails the type to write (required)
     * @param warning any warning to appear at the top of the source file
     *            (cannot be <code>null</code>; include a trailing newline if
     *            not empty)
     * @return the contents of the type (minus the warning)
     */
    String write(ClassOrInterfaceTypeDetails typeDetails, String warning);

    void write(List<ClassOrInterfaceTypeDetails> typeDetails,
            boolean includeWarning);

    void write(String destFile, String newContents);
}
