package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ButtonGroup;
import com.github.gwtbootstrap.client.ui.Form.SubmitHandler;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.WellForm;
import com.github.gwtbootstrap.client.ui.Form.SubmitEvent;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.resources.ButtonSize;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.RootLayoutPanel;

public class Account implements EntryPoint {

	private static final String INPUT_ID = "openid_identifier";
	private static final String USERNAME_TAG = "{username}";
	private static final String COOKIE_NAME = "openid_providor";

	private static AccountUiBinder uiBinder = GWT.create(AccountUiBinder.class);

	interface AccountUiBinder extends UiBinder<Widget, Account> {
	}

	private static class Provider {

		private final String name;
		private final String label;
		private final String url;

		public Provider(String name, String label, String url) {
			this.name = name;
			this.label = label;
			this.url = url;
		}

		public String getName() {
			return name;
		}

		public String getLabel() {
			return label;
		}

		public String getUrl() {
			return url;
		}
	}

	private static final Map<String, Provider> primaryProviders = new LinkedHashMap<String, Provider>();
	private static final Map<String, Provider> secondaryProviders = new LinkedHashMap<String, Provider>();
	private static final Map<String, Provider> allProviders = new LinkedHashMap<String, Provider>();

	static {
		primaryProviders.put("google", new Provider("Google", null,
				"https://www.google.com/accounts/o8/id"));
		primaryProviders.put("yahoo", new Provider("Yahoo", null,
				"http://me.yahoo.com/"));
		primaryProviders.put("aol", new Provider("AOL", "AOL Screen Name",
				"http://openid.aol.com/" + USERNAME_TAG));
		primaryProviders.put("myopenid", new Provider("MyOpenId", "MyOpenID Username",
				"http://" + USERNAME_TAG + ".myopenid.com/"));
		primaryProviders.put("openid", new Provider("OpenID", "OpenID", ""));

		secondaryProviders.put("livejournal", new Provider("LiveJournal", "LiveJournal Username",
				"http://" + USERNAME_TAG + ".livejournal.com/"));
		secondaryProviders.put("wordpress", new Provider("WordPress", "WordPress Username",
				"http://" + USERNAME_TAG + ".wordpress.com/"));
		secondaryProviders.put("blogger", new Provider("Blogger", "Blogger Account",
				"http://" + USERNAME_TAG + ".blogspot.com/"));
		secondaryProviders.put("verisign", new Provider("VeriSign", "VeriSign Username",
				"http://" + USERNAME_TAG + ".pip.verisignlabs.com/"));
		secondaryProviders.put("claimid", new Provider("ClaimID", "ClaimID Username",
				"http://claimid.com/" + USERNAME_TAG));
		secondaryProviders.put("clickpass", new Provider("ClickPass", "ClickPass Username",
				"http://clickpass.com/public/" + USERNAME_TAG));

		allProviders.putAll(primaryProviders);
		allProviders.putAll(secondaryProviders);
	}

	@UiField HorizontalPanel primaryGroup;
	@UiField FlowPanel secondaryGroup;
	@UiField(provided = true)
	DisclosurePanel more = new DisclosurePanel("More");
	//@UiField(provided = true) DisclosurePanel evenMore = new DisclosurePanel("more");
	@UiField WellForm wellForm;
	@UiField TextBox username;

	private HandlerRegistration handlerRegistration;
	private final Map<Provider, Button> providerButtonMap = new HashMap<Provider, Button>();

    @Override
    public void onModuleLoad() {
        /* Hide the loading message */
        Element loading = Document.get().getElementById("loading");
        loading.getParentElement().removeChild(loading);

        RootLayoutPanel.get().add(uiBinder.createAndBindUi(this));

		for (Entry<String, Provider> entry : primaryProviders.entrySet()) {
			final String providerId = entry.getKey();
			final Provider provider = entry.getValue();
			Button btn = new Button(provider.getName(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					signIn(providerId, provider, false);
				}
			});
			btn.setSize(ButtonSize.LARGE);
			primaryGroup.add(btn);
			providerButtonMap.put(provider, btn);
		}

		for (Entry<String, Provider> entry : secondaryProviders.entrySet()) {
			final String providerId = entry.getKey();
			final Provider provider = entry.getValue();
			Button btn = new Button(provider.getName(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					signIn(providerId, provider, false);
				}
			});
			btn.setSize(ButtonSize.LARGE);
			secondaryGroup.add(btn);
			providerButtonMap.put(provider, btn);
		}

		wellForm.setVisible(false);

		String lastProviderId = Cookies.getCookie(COOKIE_NAME);
		Provider lastProvider = allProviders.get(lastProviderId);
		if (lastProvider != null) {
			signIn(lastProviderId, lastProvider, true);
		}
	}

	private void signIn(String providerId, Provider provider, boolean onLoad) {
		highlight(provider);
		Cookies.setCookie(COOKIE_NAME, providerId);

		if (handlerRegistration != null) {
			handlerRegistration.removeHandler();
		}
		final String url = provider.getUrl();
		handlerRegistration = wellForm.addSubmitHandler(new SubmitHandler() {
			@Override
			public void onSubmit(SubmitEvent event) {
				setOpenIdUrl(url.replaceAll("{username}", username.getValue()));
			}
		});

		String label = provider.getLabel();
		if (label != null) {
			wellForm.setVisible(true);
			username.setPlaceholder(provider.getLabel());
		} else {
			wellForm.setVisible(false);
			username.setValue("");
			if (!onLoad) {
				wellForm.submit();
			}
		}
	}

	private void highlight(Provider provider) {
		for (Button btn : providerButtonMap.values()) {
			btn.setType(ButtonType.DEFAULT);
		}
		Button btn = providerButtonMap.get(provider);
		if (btn != null) {
			btn.setType(ButtonType.INFO);
		}
		if (secondaryProviders.containsValue(provider)) {
			more.setOpen(true);
		}
	}

	private void setOpenIdUrl(String url) {
		Element hidden = DOM.getElementById(INPUT_ID);
		if (hidden != null) {
			hidden.setAttribute("value", url);
		} else {
			Hidden h = new Hidden(INPUT_ID, url);
			h.setID(INPUT_ID);
			wellForm.add(h);
		}
	}
}
