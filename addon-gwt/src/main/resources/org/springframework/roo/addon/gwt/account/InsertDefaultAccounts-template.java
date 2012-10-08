package __ACCOUNT_PACKAGE__;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import __FULL_ACCOUNT_NAME__;
import __FULL_ROLE_NAME__;

@Component
@Configurable
public class InsertDefaultAccounts implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        String identityUrl = "http://example.myopenid.com/";

        try {
            __ACCOUNT_NAME__.find__ACCOUNT_NAME__ByUsername(identityUrl);
            return;
        } catch (NoResultException e) {
        } catch (EmptyResultDataAccessException e) {
        }

        __ACCOUNT_NAME__ accountAdminActive = new __ACCOUNT_NAME__();
        accountAdminActive.setUsername(identityUrl);
        accountAdminActive.getUserRoles().add(Role.ROLE_USER);
        accountAdminActive.getUserRoles().add(Role.ROLE_ADMIN);
        accountAdminActive.setEmail("name@example.com");
        accountAdminActive.setName("John Smith");
        accountAdminActive.persist();
    }
}
