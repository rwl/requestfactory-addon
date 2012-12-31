package org.springframework.roo.addon.requestfactory.android;

import org.apache.commons.lang3.StringUtils;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.DependencyScope;
import org.springframework.roo.support.util.DomUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AndroidDependency extends Dependency {
    
    private final AndroidDependencyType dependencyType;

    public AndroidDependency(Element dependency) {
        super(dependency);
        dependencyType = AndroidDependencyType.getType(dependency);
    }

    public AndroidDependencyType getDependencyType() {
        return dependencyType;
    }

    /**
     * Returns the XML element for this dependency
     * 
     * @param document the parent XML document
     * @return a non-<code>null</code> element
     */
    @Override
    public Element getElement(final Document document) {
        final Element dependencyElement = document.createElement("dependency");
        dependencyElement.appendChild(XmlUtils.createTextElement(document,
                "groupId", getGroupId()));
        dependencyElement.appendChild(XmlUtils.createTextElement(document,
                "artifactId", getArtifactId()));
        dependencyElement.appendChild(XmlUtils.createTextElement(document,
                "version", getVersion()));

        if (dependencyType != null && dependencyType != AndroidDependencyType.JAR) {
            // Keep the XML short, we don't need "JAR" given it's the default
            final Element typeElement = XmlUtils.createTextElement(document,
                    "type", dependencyType.toString().toLowerCase());
            dependencyElement.appendChild(typeElement);
        }

        // Keep the XML short, we don't need "compile" given it's the default
        if (getScope() != null && getScope() != DependencyScope.COMPILE) {
            dependencyElement.appendChild(XmlUtils.createTextElement(document,
                    "scope", getScope().toString().toLowerCase()));
            if (getScope() == DependencyScope.SYSTEM
                    && StringUtils.isNotBlank(getSystemPath())) {
                dependencyElement.appendChild(XmlUtils.createTextElement(
                        document, "systemPath", getSystemPath()));
            }
        }

        if (StringUtils.isNotBlank(getClassifier())) {
            dependencyElement.appendChild(XmlUtils.createTextElement(document,
                    "classifier", getClassifier()));
        }

        // Add exclusions if any
        if (!getExclusions().isEmpty()) {
            final Element exclusionsElement = DomUtils.createChildElement(
                    "exclusions", dependencyElement, document);
            for (final Dependency exclusion : getExclusions()) {
                final Element exclusionElement = DomUtils.createChildElement(
                        "exclusion", exclusionsElement, document);
                exclusionElement.appendChild(XmlUtils.createTextElement(
                        document, "groupId", exclusion.getGroupId()));
                exclusionElement.appendChild(XmlUtils.createTextElement(
                        document, "artifactId", exclusion.getArtifactId()));
            }
        }

        return dependencyElement;
    }
}
