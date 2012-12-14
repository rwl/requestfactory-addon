package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import java.util.List;

import __TOP_LEVEL_PACKAGE__.R;
import __TOP_LEVEL_PACKAGE__.R.id;
import __TOP_LEVEL_PACKAGE__.processor.PluralProcessor;

import com.google.web.bindery.requestfactory.shared.EntityProxy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class ProxyTypeArrayAdapter extends ArrayAdapter<Class<? extends EntityProxy>> {

    final Context context;
    final int resource;
    final List<Class<? extends EntityProxy>> data;

    final LayoutInflater inflater;

    public ProxyTypeArrayAdapter(final Context context,
            final int resource,
            final List<Class<? extends EntityProxy>> data) {
        super(context, resource, data);
            this.resource = resource;
            this.context = context;
            this.data = data;

            inflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, final View convertView,
            final ViewGroup parent) {

        View row = convertView;
        final ProxyClassHolder holder;

        if (row == null) {
            row = inflater.inflate(resource, parent, false);

            holder = new ProxyClassHolder();
            holder.className = (TextView) row.findViewById(
                    R.id.className);

            row.setTag(holder);
        } else {
            holder = (ProxyClassHolder) row.getTag();
        }

        final Class<? extends EntityProxy> type = data.get(position);
        holder.className.setText(PluralProcessor.instance()
                .process(type));

        return row;
    }

    static class ProxyClassHolder {
            TextView className;
    }
}
