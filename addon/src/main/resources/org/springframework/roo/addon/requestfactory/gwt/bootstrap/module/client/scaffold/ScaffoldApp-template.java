package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import __SHARED_TOP_LEVEL_PACKAGE__.managed.request.ApplicationEntityTypesProcessor;
import __TOP_LEVEL_PACKAGE__.place.ProxyListPlace;
import com.google.web.bindery.requestfactory.shared.EntityProxy;

import java.util.ArrayList;
import java.util.List;

public class ScaffoldApp {

	static boolean isMobile = false;

	public static boolean isMobile() {
		return isMobile;
	}

	public void run() {
	}

	public static ArrayList<ProxyListPlace> getRootPlaces() {
		List<Class<? extends EntityProxy>> types = ApplicationEntityTypesProcessor.getAll();
		ArrayList<ProxyListPlace> rtn = new ArrayList<ProxyListPlace>(types.size());

		for (Class<? extends EntityProxy> type : types) {
			rtn.add(new ProxyListPlace(type, null));
		}

		return rtn;
	}
}