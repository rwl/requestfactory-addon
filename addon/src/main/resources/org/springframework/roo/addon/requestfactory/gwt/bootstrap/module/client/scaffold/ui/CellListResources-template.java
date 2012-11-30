package __TOP_LEVEL_PACKAGE__.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellList;

public interface CellListResources extends CellList.Resources {

	public static CellListResources INSTANCE = GWT.create(CellListResources.class);

	@Source("CellListStyle.css")
	CellList.Style cellListStyle();
}
