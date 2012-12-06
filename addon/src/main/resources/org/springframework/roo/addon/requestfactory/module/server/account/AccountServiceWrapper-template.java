package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

__IMPORT_ACCOUNT__

/**
 * Service object that reduces the visible api of {@link AccountService}.
 */
public interface AccountServiceWrapper {

    Boolean isAdminAccount();

    String getIdentityUrl();

    String getName();

    String getEmail();

    Account getAccount();

    String getAccountId();
}
