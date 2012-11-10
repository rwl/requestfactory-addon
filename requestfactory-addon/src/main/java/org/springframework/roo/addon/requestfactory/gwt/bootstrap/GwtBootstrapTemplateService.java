package org.springframework.roo.addon.requestfactory.gwt.bootstrap;

import java.util.List;
import java.util.Map;

import org.springframework.roo.addon.requestfactory.RequestFactoryProxyProperty;
import org.springframework.roo.addon.requestfactory.RequestFactoryTemplateDataHolder;
import org.springframework.roo.addon.requestfactory.RequestFactoryTemplateService;
import org.springframework.roo.addon.requestfactory.RequestFactoryType;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.model.JavaSymbolName;

/**
 * Interface for {@link GwtBootstrapTemplateServiceImpl}.
 *
 * @author James Tyrrell
 * @since 1.1.2
 */
public interface GwtBootstrapTemplateService {

    RequestFactoryTemplateDataHolder getMirrorTemplateTypeDetails(
            ClassOrInterfaceTypeDetails governorTypeDetails,
            Map<JavaSymbolName, RequestFactoryProxyProperty> clientSideTypeMap,
            String moduleName);

    List<ClassOrInterfaceTypeDetails> getStaticTemplateTypeDetails(
            RequestFactoryType type, String moduleName);



    String buildUiXml(String templateContents, String destFile,
            List<MethodMetadata> proxyMethods);
}
