package org.springframework.roo.addon.requestfactory.account;

import static org.springframework.roo.addon.requestfactory.account.AccountJavaType.ROO_ACCOUNT;
import static org.springframework.roo.model.RooJavaType.ROO_JPA_ENTITY;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.jpa.activerecord.JpaCrudAnnotationValues;
import org.springframework.roo.addon.jpa.entity.JpaEntityAnnotationValues;
import org.springframework.roo.addon.requestfactory.entity.EntityAnnotationValues;
import org.springframework.roo.addon.requestfactory.entity.EntityMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.MemberHoldingTypeDetailsMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;


/**
 * Provides {@link ExampleMetadata}. This type is called by Roo to retrieve the metadata for this add-on.
 * Use this type to reference external types and services needed by the metadata type. Register metadata triggers and
 * dependencies here. Also define the unique add-on ITD identifier.
 *
 * @since 1.1
 */
@Component
@Service
public final class AccountMetadataProviderImpl extends AbstractItdMetadataProvider implements AccountMetadataProvider {

    @Reference TypeManagementService typeManagementService;
    @Reference private ProjectOperations projectOperations;

    /**
     * The activate method for this OSGi component, this will be called by the OSGi container upon bundle activation
     * (result of the 'addon install' command)
     *
     * @param context the component context can be used to get access to the OSGi container (ie find out if certain bundles are active)
     */
    protected void activate(ComponentContext context) {
        metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
        addMetadataTrigger(ROO_ACCOUNT);
    }

    /**
     * The deactivate method for this OSGi component, this will be called by the OSGi container upon bundle deactivation
     * (result of the 'addon uninstall' command)
     *
     * @param context the component context can be used to get access to the OSGi container (ie find out if certain bundles are active)
     */
    protected void deactivate(ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
        removeMetadataTrigger(new JavaType(RooAccount.class.getName()));
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(String metadataIdentificationString, JavaType aspectName, PhysicalTypeMetadata governorPhysicalTypeMetadata, String itdFilename) {

        final JpaCrudAnnotationValues crudAnnotationValues = new JpaCrudAnnotationValues(governorPhysicalTypeMetadata);
        final JpaEntityAnnotationValues jpaEntityAnnotationValues = new JpaEntityAnnotationValues(governorPhysicalTypeMetadata, ROO_JPA_ENTITY);
        final AccountAnnotationValues accountAnnotationValues = new AccountAnnotationValues(governorPhysicalTypeMetadata);

        final String sharedPackageName = accountAnnotationValues.getSharedPackage();

        JavaType entity = AccountMetadata.getJavaType(metadataIdentificationString);
        LogicalPath path = AccountMetadata.getPath(metadataIdentificationString);

        final String entityName = StringUtils.defaultIfEmpty(
                jpaEntityAnnotationValues.getEntityName(),
                entity.getSimpleTypeName());

        boolean isGaeEnabled = false;
        final String moduleName = path.getModule();
        if (projectOperations.isProjectAvailable(moduleName)) {
            // If the project itself changes, we want a chance to refresh this
            // item
            metadataDependencyRegistry.registerDependency(
                    ProjectMetadata.getProjectIdentifier(moduleName),
                    metadataIdentificationString);
            isGaeEnabled = projectOperations
                    .isFeatureInstalledInFocusedModule(FeatureNames.GAE);
        }

        return new AccountMetadata(metadataIdentificationString, aspectName, governorPhysicalTypeMetadata, crudAnnotationValues, typeManagementService, typeLocationService, entityName, sharedPackageName, isGaeEnabled);
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name will be **_ROO_Account.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "Account";
    }

    protected String getGovernorPhysicalTypeIdentifier(String metadataIdentificationString) {
        JavaType javaType = AccountMetadata.getJavaType(metadataIdentificationString);
        LogicalPath path = AccountMetadata.getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return AccountMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return AccountMetadata.getMetadataIdentiferType();
    }

    @Override
    public AccountAnnotationValues getAnnotationValues(JavaType javaType) {
        Validate.notNull(javaType, "JavaType required");
        final String physicalTypeId = typeLocationService
                .getPhysicalTypeIdentifier(javaType);
        if (StringUtils.isBlank(physicalTypeId)) {
            return null;
        }
        final MemberHoldingTypeDetailsMetadataItem<?> governor = (MemberHoldingTypeDetailsMetadataItem<?>) metadataService
                .get(physicalTypeId);
        if (MemberFindingUtils.getAnnotationOfType(governor, ROO_ACCOUNT) == null) {
            // The type is not annotated with @RooGwtBootstrap
            return null;
        }
        return new AccountAnnotationValues(governor);
    }
}