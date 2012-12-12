package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import java.util.List;

import __TOP_LEVEL_PACKAGE__.R;
import __TOP_LEVEL_PACKAGE__.adapter.ProxyTypeArrayAdapter;
import __TOP_LEVEL_PACKAGE__.processor.ListActivityProcessor;
import __SHARED_TOP_LEVEL_PACKAGE__.managed.request.ApplicationEntityTypesProcessor;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.web.bindery.requestfactory.shared.EntityProxy;


public class MainActivity extends ListActivity {

    private static final List<Class<? extends EntityProxy>> values =
            ApplicationEntityTypesProcessor.getAll();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ProxyTypeArrayAdapter adapter = new ProxyTypeArrayAdapter(this,
            R.layout.proxytype_listview_item_row, values);

        final TextView headerText = (TextView) findViewById(R.id.headerText);
        headerText.setText("Select Type");

        final View header = (View) getLayoutInflater().inflate(R.layout
                .listview_header_row, null);
        getListView().addHeaderView(header);

        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(final ListView l, final View v,
            final int position, final long id) {
        super.onListItemClick(l, v, position, id);

        final Class<? extends EntityProxy> clazz = values.get(position);

        final Intent intent = new Intent(this, ListActivityProcessor
                .instance().process(clazz));

        startActivity(intent);
    }
}
