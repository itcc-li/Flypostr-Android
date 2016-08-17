package li.itcc.flypostr.tableutil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;

public abstract class AbstractDataAdapter<ENTRY> extends DataAdapterSupport implements ListAdapter, SpinnerAdapter {
    protected LayoutInflater fInflater;
    private ArrayList<ENTRY> fItems;

    public AbstractDataAdapter(LayoutInflater inflater) {
        fInflater = inflater;
        fItems = new ArrayList<ENTRY>();
    }

    public LayoutInflater getInflater() {
        return fInflater;
    }

    public void setEntries(ENTRY[] entries) {
        if (fItems.size() > 0) {
            fItems.clear();
            onClearDone();
        }
        appendEntries(entries);
    }

    public void appendEntries(ENTRY[] entries) {
        if (entries != null && entries.length > 0) {
            fItems.ensureCapacity(fItems.size() + entries.length);
            for (int i = 0; i < entries.length; i++) {
                fItems.add(entries[i]);
            }
            onAppendDone(entries);
        }
        notifyOnChanged();
    }

    public ENTRY getTypesafeItem(int position) {
        return fItems.get(position);
    }

    public ENTRY[] getEntries(ENTRY[] allocated) {
        return fItems.toArray(allocated);
    }

    // // interface Adapter

    public boolean hasStableIds() {
        return true;
    }

    public long getItemId(int position) {
        return position;
    }

    public int getCount() {
        return fItems.size();
    }

    public Object getItem(int position) {
         return fItems.get(position);
    }

    public boolean isEmpty() {
        boolean isEmpty = getCount() == 0;
        return isEmpty;
    }

    // // SpinnerAdapter

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    // // callbacks

    protected void onAppendDone(ENTRY[] entries) {
    }

    protected void onClearDone() {
    }

}
