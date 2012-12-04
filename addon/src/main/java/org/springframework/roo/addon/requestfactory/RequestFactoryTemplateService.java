package org.springframework.roo.addon.requestfactory;

import hapax.TemplateDataDictionary;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.roo.addon.requestfactory.entity.TextType;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.layers.MemberTypeAdditions;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Interface for {@link RequestFactoryTemplateServiceImpl}.
 *
 * @author James Tyrrell
 * @since 1.1.2
 */
public interface RequestFactoryTemplateService {

    RequestFactoryTemplateDataHolder getMirrorTemplateTypeDetails(
            ClassOrInterfaceTypeDetails governorTypeDetails,
            Map<JavaSymbolName, RequestFactoryProxyProperty> clientSideTypeMap,
            String moduleName);

    List<ClassOrInterfaceTypeDetails> getStaticTemplateTypeDetails(
            RequestFactoryType type, String moduleName);

    void addImport(TemplateDataDictionary dataDictionary, JavaType type);

    void addImport(TemplateDataDictionary dataDictionary,
            String importDeclaration);

    void addImport(TemplateDataDictionary dataDictionary, String simpleName,
            RequestFactoryType requestFactoryType, String moduleName);

    void addReference(TemplateDataDictionary dataDictionary,
            RequestFactoryType type, Map<RequestFactoryType,
            JavaType> mirrorTypeMap);

    void addReference(TemplateDataDictionary dataDictionary,
            RequestFactoryType type, String moduleName);

    TemplateDataDictionary buildDictionary(RequestFactoryType type,
            String moduleName);

    TemplateDataDictionary buildMirrorDataDictionary(RequestFactoryType type,
            ClassOrInterfaceTypeDetails mirroredType,
            ClassOrInterfaceTypeDetails proxy, Map<RequestFactoryType,
            JavaType> mirrorTypeMap, Map<JavaSymbolName,
            RequestFactoryProxyProperty> clientSideTypeMap, String moduleName);

    TemplateDataDictionary buildStandardDataDictionary(RequestFactoryType type,
            String moduleName, String proxyModuleName);

    JavaType getCollectionImplementation(JavaType javaType);

    JavaType getDestinationJavaType(RequestFactoryType destType,
            String moduleName);

    String getFullyQualifiedTypeName(RequestFactoryType requestFactoryType,
            String moduleName);

    String getRequestMethodCall(ClassOrInterfaceTypeDetails request,
            MemberTypeAdditions memberTypeAdditions);

    String getTemplateContents(String templateName,
            TemplateDataDictionary dataDictionary, String templateDirectory);

    ClassOrInterfaceTypeDetails getTemplateDetails(
            TemplateDataDictionary dataDictionary, String templateFile,
            JavaType templateType, String moduleName,
            String templateDirectory);

    boolean isInvisible(RequestFactoryProxyProperty property,
            ClassOrInterfaceTypeDetails governorTypeDetails);

    boolean isUneditable(RequestFactoryProxyProperty property,
            ClassOrInterfaceTypeDetails governorTypeDetails);

    boolean isNotNull(RequestFactoryProxyProperty property,
            ClassOrInterfaceTypeDetails governorTypeDetails);

    TextType getTextType(JavaSymbolName fieldName,
            ClassOrInterfaceTypeDetails governorTypeDetails);

    String getHelpText(JavaSymbolName fieldName,
            ClassOrInterfaceTypeDetails governorTypeDetails);

    String getUnits(JavaSymbolName fieldName,
            ClassOrInterfaceTypeDetails governorTypeDetails);

    boolean isSameBaseType(JavaType type1, JavaType type2);

    void maybeAddImport(TemplateDataDictionary dataDictionary,
            Set<String> importSet, JavaType type);

    boolean isReadOnly(String name,
            ClassOrInterfaceTypeDetails governorTypeDetails);

    boolean isPrimaryProp(RequestFactoryProxyProperty prop,
            ClassOrInterfaceTypeDetails entity);

    boolean isSecondaryProp(RequestFactoryProxyProperty prop,
            ClassOrInterfaceTypeDetails entity);

    boolean isRenderProp(RequestFactoryProxyProperty prop,
            ClassOrInterfaceTypeDetails entity, String propertyAttribute);
}
