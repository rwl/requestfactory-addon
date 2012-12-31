package org.springframework.roo.addon.requestfactory.android;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.maven.Pom;
import org.springframework.roo.support.util.DomUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Component
@Service
public class AndroidTypeServiceImpl implements AndroidTypeService {

    private static final String NAME = "android:name";
    private static final String MAIN_ACTION = "android.intent.action.MAIN";
    private static final String LAUNCHER_CATEGORY = "android.intent.category.LAUNCHER";

    @Reference FileManager fileManager;
    @Reference ProjectOperations projectOperations;

    @Override
    public void addPermission(final String moduleName,
            final String permissionName) {
        final String manifestXml = getAndroidManifestXml(moduleName);
        Validate.notBlank(manifestXml,
                "AndroidManifest.xml not found for module '"
                + moduleName + "'");
        final Document manifestXmlDoc = getAndroidManifestXmlDocument(manifestXml);
        final Element manifestXmlRoot = manifestXmlDoc.getDocumentElement();

        final List<Element> permissionElements = XmlUtils.findElements(
                "/manifest/uses-permission", manifestXmlRoot);

        if (!existingWithName(permissionName, permissionElements)) {
            final Element permissionElement = manifestXmlDoc.createElement(
                    "uses-permission");
            permissionElement.setAttribute(NAME, permissionName);
            manifestXmlRoot.appendChild(permissionElement);

            final String xmlString = XmlUtils.nodeToString(manifestXmlDoc);
            fileManager.createOrUpdateTextFileIfRequired(manifestXml,
                    xmlString, "Added '" + permissionName
                    + "' permission to Android manifest", true);
        }
    }
    
    @Override
    public void setApplicationName(final String moduleName,
            final String applicationName) {
        final String manifestXml = getAndroidManifestXml(moduleName);
        Validate.notBlank(manifestXml,
                "AndroidManifest.xml not found for module '"
                + moduleName + "'");
        final Document manifestXmlDoc = getAndroidManifestXmlDocument(manifestXml);
        final Element manifestXmlRoot = manifestXmlDoc.getDocumentElement();
        final Element applicationElement = XmlUtils.findFirstElement(
                "/manifest/application", manifestXmlRoot);
        
        applicationElement.setAttribute(NAME, applicationName);

        final String xmlString = XmlUtils.nodeToString(manifestXmlDoc);
        fileManager.createOrUpdateTextFileIfRequired(manifestXml,
                xmlString, "Set application name to: " + applicationName,
                true);
    }

    @Override
    public void addActvity(final String moduleName, final String activityName,
            final boolean mainActivity) {
        final String manifestXml = getAndroidManifestXml(moduleName);
        Validate.notBlank(manifestXml,
                "AndroidManifest.xml not found for module '"
                + moduleName + "'");
        final Document manifestXmlDoc = getAndroidManifestXmlDocument(manifestXml);
        final Element manifestXmlRoot = manifestXmlDoc.getDocumentElement();
        final Element applicationElement = XmlUtils.findFirstElement(
                "/manifest/application", manifestXmlRoot);
        final List<Element> activityElements = XmlUtils.findElements(
                "/manifest/application/activity", manifestXmlRoot);
        if (!existingWithName(activityName, activityElements)) {
            final Element activityElement = manifestXmlDoc.createElement(
                    "activity");
            activityElement.setAttribute(NAME, activityName);

            if (mainActivity) {
                /*for (Element element : activityElements) {
                    
                }*/
                final Element intentFilter = manifestXmlDoc.createElement(
                        "intent-filter");
                final Element action = manifestXmlDoc.createElement("action");
                action.setAttribute(NAME, MAIN_ACTION);
                intentFilter.appendChild(action);
                final Element category = manifestXmlDoc.createElement("category");
                category.setAttribute(NAME, LAUNCHER_CATEGORY);
                intentFilter.appendChild(category);
                activityElement.appendChild(intentFilter);
            }

            applicationElement.appendChild(activityElement);

            final String xmlString = XmlUtils.nodeToString(manifestXmlDoc);
            fileManager.createOrUpdateTextFileIfRequired(manifestXml,
                    xmlString,
                    "Added '" + activityName + "' to Android manifest",
                    true);
        }
    }

    private boolean existingWithName(final String name,
            final Iterable<Element> elements) {
        for (final Element activityElement : elements) {
            if (name.equals(activityElement.getAttribute(NAME))) {
                return true;
            }
        }
        return false;
    }

    public String getAndroidManifestXml(final String moduleName) {
        final LogicalPath logicalPath = LogicalPath.getInstance(
                Path.ROOT, moduleName);
        final String manifestXmlPath = projectOperations.getPathResolver()
                .getRoot(logicalPath) + File.separatorChar
                + "AndroidManifest.xml";
        final FileDetails manifestXml = fileManager.readFile(manifestXmlPath);
        if (manifestXml == null) {
            throw new IllegalStateException(
                    "Module must have an AndroidManifest.xml file");
        }
        return manifestXml.getCanonicalPath();
    }

    public Document getAndroidManifestXmlDocument(final String canonicalPath) {
        final DocumentBuilder builder = XmlUtils.getDocumentBuilder();
        InputStream inputStream = null;
        try {
            inputStream = fileManager.getInputStream(canonicalPath);
            return builder.parse(inputStream);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Override
    public void addDependencies(final String moduleName,
            final Collection<? extends AndroidDependency> newDependencies) {
        Validate.isTrue(projectOperations.isProjectAvailable(moduleName),
                "Dependency modification prohibited; no such module '"
                        + moduleName + "'");
        final Pom pom = projectOperations.getPomFromModuleName(moduleName);
        Validate.notNull(pom,
                "The pom is not available, so dependencies cannot be added");

        final Document document = XmlUtils.readXml(fileManager
                .getInputStream(pom.getPath()));
        final Element dependenciesElement = DomUtils.createChildIfNotExists(
                "dependencies", document.getDocumentElement(), document);
        final List<Element> existingDependencyElements = XmlUtils.findElements(
                "dependency", dependenciesElement);

        final List<String> addedDependencies = new ArrayList<String>();
        final List<String> removedDependencies = new ArrayList<String>();
        final List<String> skippedDependencies = new ArrayList<String>();
        for (final AndroidDependency newDependency : newDependencies) {
            if (pom.canAddDependency(newDependency)) {
                // Look for any existing instances of this dependency
                boolean inserted = false;
                for (final Element existingDependencyElement : existingDependencyElements) {
                    final Dependency existingDependency = new Dependency(
                            existingDependencyElement);
                    if (existingDependency.hasSameCoordinates(newDependency)) {
                        // It's the same artifact, but might have a different
                        // version, exclusions, etc.
                        if (!inserted) {
                            // We haven't added the new one yet; do so now
                            dependenciesElement.insertBefore(
                                    newDependency.getElement(document),
                                    existingDependencyElement);
                            inserted = true;
                            if (!newDependency.getVersion().equals(
                                    existingDependency.getVersion())) {
                                // It's a genuine version change => mention the
                                // old and new versions in the message
                                addedDependencies.add(newDependency
                                        .getSimpleDescription());
                                removedDependencies.add(existingDependency
                                        .getSimpleDescription());
                            }
                        }
                        // Either way, we remove the previous one in case it was
                        // different in any way
                        dependenciesElement
                                .removeChild(existingDependencyElement);
                    }
                    // Keep looping in case it's present more than once
                }
                if (!inserted) {
                    // We didn't encounter any existing dependencies with the
                    // same coordinates; add it now
                    dependenciesElement.appendChild(newDependency
                            .getElement(document));
                    addedDependencies.add(newDependency.getSimpleDescription());
                }
            }
            else {
                skippedDependencies.add(newDependency.getSimpleDescription());
            }
        }
        if (!newDependencies.isEmpty() || !skippedDependencies.isEmpty()) {
            /*final String message = getPomDependenciesUpdateMessage(addedDependencies,
                    removedDependencies, skippedDependencies);*/
            fileManager.createOrUpdateTextFileIfRequired(pom.getPath(),
                    XmlUtils.nodeToString(document), false);
        }
    }
}
