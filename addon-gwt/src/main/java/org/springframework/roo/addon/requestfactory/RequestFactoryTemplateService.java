package org.springframework.roo.addon.requestfactory;

import java.util.List;
import java.util.Map;

import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.model.JavaSymbolName;

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
}
