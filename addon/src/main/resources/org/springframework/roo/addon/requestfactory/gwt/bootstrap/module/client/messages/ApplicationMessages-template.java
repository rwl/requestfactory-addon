package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;

@DefaultLocale("en_US")
public interface ApplicationMessages extends Messages {

    @DefaultMessage("Back")
    String back();

    @DefaultMessage("Delete")
    String delete();

    @DefaultMessage("Edit")
    String edit();

    @DefaultMessage("View")
    String visualize();

    @DefaultMessage("New")
    String create();

    @DefaultMessage("Save")
    String save();

    @DefaultMessage("Cancel")
    String cancel();

    @DefaultMessage("Model Editor")
    String applicationName();

    @DefaultMessage("Not Signed In")
    String notSignedIn();

    @DefaultMessage("{0}")
    String signedInAs(String nickName);

    @DefaultMessage("loading...")
    String loading();
}
