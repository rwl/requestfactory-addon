package __TOP_LEVEL_PACKAGE__.place;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import __TOP_LEVEL_PACKAGE__.managed.SimplePagerConstants;
import __TOP_LEVEL_PACKAGE__.messages.ApplicationMessages;

import com.github.gwtbootstrap.client.ui.CellTable;
import com.github.gwtbootstrap.client.ui.Divider;
import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.github.gwtbootstrap.client.ui.SimplePager.Resources;
import com.github.gwtbootstrap.client.ui.SimplePager.TextLocation;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Cookies;
import com.google.web.bindery.requestfactory.shared.EntityProxy;

public abstract class AbstractDesktopListView<P extends EntityProxy> extends AbstractProxyListView<P> {

    protected static final String TABLE_STYLE_COOKIE_NAME = "TABLE_STYLE";
    protected static final String PAGE_SIZE_COOKIE_NAME = "PAGE_SIZE";

    protected static final String COMFORTABLE_STYLE = "comfortable";
    protected static final String CONDENSED_STYLE = "condensed";

    protected static final int[] PAGE_SIZES = new int[] { 10, 25, 50, 100 };

    protected ApplicationMessages applicationMessages = GWT.create(ApplicationMessages.class);

    @UiField(provided = true)
    public SimplePager pager = new SimplePager(TextLocation.RIGHT, (Resources) GWT.create(Resources.class), true, 50, false, (SimplePagerConstants) GWT.create(SimplePagerConstants.class));

    @UiField(provided = true)
    public CellTable<P> table = new CellTable<P>(PAGE_SIZES[0], GWT.<CellTable.SelectableResources> create(CellTable.SelectableResources.class));

    @UiField
    public DropdownButton tableOptions;

    private final Set<NavLink> tableStyles = new HashSet<NavLink>();
    private final Map<Integer, NavLink> pageSizes = new HashMap<Integer, NavLink>();
    private final Map<Integer, ClickHandler> pageSizeHandlers = new HashMap<Integer, ClickHandler>();

    public void init() {
        initTableStyles();
        tableOptions.add(new Divider());
        initPageSizes();
            tableOptions.setText(applicationMessages.options());
    }

    private void initTableStyles() {
        final NavLink comfortable = new NavLink(applicationMessages.comfortable());
        comfortable.setIcon(IconType.CHECK_EMPTY);
        tableStyles.add(comfortable);
        final ClickHandler comfortableHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                for (NavLink link : tableStyles) {
                    link.setIcon(IconType.CHECK_EMPTY);
                }
                comfortable.setIcon(IconType.CHECK);
                Cookies.setCookie(TABLE_STYLE_COOKIE_NAME, COMFORTABLE_STYLE);
                table.setCondensed(false);
            }
        };
        comfortable.addClickHandler(comfortableHandler);
        tableOptions.add(comfortable);

        final NavLink condensed = new NavLink(applicationMessages.condensed());
        condensed.setIcon(IconType.CHECK_EMPTY);
        tableStyles.add(condensed);
        final ClickHandler condensedHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                for (NavLink link : tableStyles) {
                    link.setIcon(IconType.CHECK_EMPTY);
                }
                condensed.setIcon(IconType.CHECK);
                Cookies.setCookie(TABLE_STYLE_COOKIE_NAME, CONDENSED_STYLE);
                table.setCondensed(true);
            }
        };
        condensed.addClickHandler(condensedHandler);
        tableOptions.add(condensed);

        final String tableStyle = Cookies.getCookie(TABLE_STYLE_COOKIE_NAME);
        if (COMFORTABLE_STYLE.equals(tableStyle)) {
            comfortableHandler.onClick(null);
        } else if (CONDENSED_STYLE.equals(tableStyle)) {
            condensedHandler.onClick(null);
        } else {
            comfortableHandler.onClick(null);
        }
    }

    private void initPageSizes() {
        for (final int pageSize : PAGE_SIZES) {
            final NavLink navLink = new NavLink(String.valueOf(pageSize));
            navLink.setIcon(IconType.CHECK_EMPTY);
            pageSizes.put(pageSize, navLink);
            final ClickHandler handler = new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    for (NavLink link : pageSizes.values()) {
                        link.setIcon(IconType.CHECK_EMPTY);
                    }
                    navLink.setIcon(IconType.CHECK);
                    Cookies.setCookie(PAGE_SIZE_COOKIE_NAME, String.valueOf(pageSize));
                    table.setPageSize(pageSize);
                    pager.setFastForwardRows(5 * pageSize);
                }
            };
            pageSizeHandlers.put(pageSize, handler);
            navLink.addClickHandler(handler);
            tableOptions.add(navLink);
        }

        final String pageSizeCookie = Cookies.getCookie(PAGE_SIZE_COOKIE_NAME);
        final int pageSize;
        if (pageSizeCookie == null) {
            pageSize = Integer.valueOf(PAGE_SIZES[0]);
        } else {
            pageSize = Integer.valueOf(pageSizeCookie);
        }
        final ClickHandler handler = pageSizeHandlers.get(pageSize);
        if (handler != null) {
            handler.onClick(null);
        }
    }
}
