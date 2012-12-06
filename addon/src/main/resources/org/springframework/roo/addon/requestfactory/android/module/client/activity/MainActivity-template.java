package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import __TOP_LEVEL_PACKAGE__.R;

import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import roboguice.activity.RoboListActivity;

import __SHARED_TOP_LEVEL_PACKAGE__.managed.request.ApplicationEntityTypesProcessor;
import com.google.web.bindery.requestfactory.shared.EntityProxy;


public class MainActivity extends RoboListActivity {

    private static final List<String> values = getListValues();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_view);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
            android.R.layout.simple_list_item_1, values);

        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }
    
    private static List<String> getListValues() {
		List<Class<? extends EntityProxy>> types = ApplicationEntityTypesProcessor.getAll();
		List<String> values = new ArrayList<String>(types.size());

		for (Class<? extends EntityProxy> type : types) {
			//values.add(ProxyListRenderer.instance().render(type));
		}

		return values;
    }
}
