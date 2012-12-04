package org.springframework.roo.addon.requestfactory.android;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Component
@Service
public class AndroidTypeServiceImpl implements AndroidTypeService {

    private static final String NAME = "activity:name";
    private static final String MAIN_ACTION = "android.intent.action.MAIN";
    private static final String LAUNCHER_CATEGORY = "android.intent.category.LAUNCHER";

    @Reference FileManager fileManager;
    @Reference ProjectOperations projectOperations;

    @Override
    public void addActvity(final String moduleName, final String activityName,
            final boolean mainActivity) {
        final String manifestXml = getAndroidManifestXml(moduleName);
        Validate.notBlank(manifestXml, "AndroidManifest.xml not found for module '"
                + moduleName + "'");
        final Document manifestXmlDoc = getAndroidManifestXmlDocument(manifestXml);
        final Element manifestXmlRoot = manifestXmlDoc.getDocumentElement();
        final Element applicationElement = XmlUtils.findFirstElement("/manifest/application",
                manifestXmlRoot);
        final List<Element> activityElements = XmlUtils.findElements(
                "/manifest/application/activity", manifestXmlRoot);
        if (!existingActivity(activityName, activityElements)) {
            final Element activityElement = manifestXmlDoc.createElement(
                    "activity");
            activityElement.setAttribute(NAME, activityName);

            if (mainActivity) {
                for (Element element : activityElements) {
                    
                }
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

            fileManager.createOrUpdateTextFileIfRequired(manifestXml,
                    XmlUtils.nodeToString(manifestXmlDoc),
                    "Added '" + activityName + "' to Android manifest",
                    true);
        }
    }

    private boolean existingActivity(final String activityName,
            final Iterable<Element> activityElements) {
        for (final Element activityElement : activityElements) {
            if (activityName.equals(activityElement.getAttribute(NAME))) {
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
}
