package org.springframework.roo.addon.gwt.account;

import static org.springframework.roo.addon.gwt.account.RooAccount.SHARED_PACKAGE_DEFAULT;
import static org.springframework.roo.addon.gwt.account.AccountJavaType.ROO_ACCOUNT;

import org.apache.commons.lang3.StringUtils;
import org.springframework.roo.classpath.details.annotations.populator.AbstractAnnotationValues;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.classpath.itd.MemberHoldingTypeDetailsMetadataItem;

/**
 * The values of a parsed {@link RooGwtBootstrap} annotation.
 */
public class AccountAnnotationValues extends AbstractAnnotationValues {

    @AutoPopulate private String sharedPackage = SHARED_PACKAGE_DEFAULT;

    /**
     * Constructor
     *
     * @param annotatedType
     */
    public AccountAnnotationValues(final MemberHoldingTypeDetailsMetadataItem<?> annotatedType) {
        super(annotatedType, ROO_ACCOUNT);
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    public String getSharedPackage() {
        return StringUtils.defaultIfEmpty(sharedPackage, SHARED_PACKAGE_DEFAULT);
    }
}
