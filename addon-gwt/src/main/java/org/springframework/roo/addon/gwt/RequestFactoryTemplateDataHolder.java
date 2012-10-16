package org.springframework.roo.addon.gwt;

import java.util.List;
import java.util.Map;

import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;

/**
 * Holder for types and xml files created via {@link RequestFactoryTemplateService}.
 * 
 * @author James Tyrrell
 * @since 1.1.2
 */
public class RequestFactoryTemplateDataHolder {

    private final Map<RequestFactoryType, ClassOrInterfaceTypeDetails> templateTypeDetailsMap;
    private final List<ClassOrInterfaceTypeDetails> typeList;
    private final Map<String, String> xmlMap;
    private final Map<RequestFactoryType, String> xmlTemplates;

    public RequestFactoryTemplateDataHolder(
            final Map<RequestFactoryType, ClassOrInterfaceTypeDetails> templateTypeDetailsMap,
            final Map<RequestFactoryType, String> xmlTemplates,
            final List<ClassOrInterfaceTypeDetails> typeList,
            final Map<String, String> xmlMap) {
        this.templateTypeDetailsMap = templateTypeDetailsMap;
        this.xmlTemplates = xmlTemplates;
        this.typeList = typeList;
        this.xmlMap = xmlMap;
    }

    public Map<RequestFactoryType, ClassOrInterfaceTypeDetails> getTemplateTypeDetailsMap() {
        return templateTypeDetailsMap;
    }

    public List<ClassOrInterfaceTypeDetails> getTypeList() {
        return typeList;
    }

    public Map<String, String> getXmlMap() {
        return xmlMap;
    }

    public Map<RequestFactoryType, String> getXmlTemplates() {
        return xmlTemplates;
    }
}
