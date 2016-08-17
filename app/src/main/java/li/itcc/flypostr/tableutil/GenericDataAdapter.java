package li.itcc.flypostr.tableutil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class GenericDataAdapter<ENTRY> extends AbstractDataAdapter<ENTRY> {
    private ArrayList<RowTemplate<? extends ENTRY, ?>> fRowTemplates = new ArrayList<RowTemplate<? extends ENTRY, ?>>();
    private boolean fAllItemsEnabled = true;
    private byte[] fItemViewTypes = new byte[0];

    public GenericDataAdapter(LayoutInflater inflater) {
        super(inflater);
    }

    public void addRowTemplate(RowTemplate<? extends ENTRY, ?> template) {
        fRowTemplates.add(template);
        fAllItemsEnabled &= template.areAllItemsEnabled();
    }

    // // interface Adapter

    public int getViewTypeCount() {
        return fRowTemplates.size();
    }

    public int getItemViewType(int position) {
        return fItemViewTypes[position];
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Object holder;
        ENTRY item = getTypesafeItem(position);
        int type = fItemViewTypes[position];
        RowTemplate<? extends ENTRY, ?> template = fRowTemplates.get(type);
        if (convertView == null) {
            int itemLayoutId = template.getItemLayoutId();
            convertView = fInflater.inflate(itemLayoutId, parent, false);
            holder = template.createViewHolder(convertView);
            convertView.setTag(holder);
        }
        else {
            holder = convertView.getTag();
        }
        template.internalOnLoad(holder, item);
        return convertView;
    }

    // // interface ListAdapter

    public boolean areAllItemsEnabled() {
        return fAllItemsEnabled;
    }

    public boolean isEnabled(int position) {
        if (fAllItemsEnabled) {
            return true;
        }
        int type = fItemViewTypes[position];
        RowTemplate<? extends ENTRY, ?> template = fRowTemplates.get(type);
        if (template.areAllItemsEnabled()) {
            return true;
        }
        ENTRY item = getTypesafeItem(position);
        return template.internalIsEnabled(item);
    }

    // // overridden methods

    protected void onAppendDone(ENTRY[] entries) {
        super.onAppendDone(entries);
        byte[] newTypes = new byte[fItemViewTypes.length + entries.length];
        System.arraycopy(fItemViewTypes, 0, newTypes, 0, fItemViewTypes.length);
        for (int i = 0, j = fItemViewTypes.length; i < entries.length; i++, j++) {
            newTypes[j] = calculateItemViewType(entries[i]);
        }
        fItemViewTypes = newTypes;
    }

    @Override
    protected void onClearDone() {
        super.onClearDone();
        fItemViewTypes = new byte[0];
    }

    private byte calculateItemViewType(ENTRY entry) {
        Class<?> c = entry.getClass();
        for (int i = 0; i < fRowTemplates.size(); i++) {
            if (fRowTemplates.get(i).getItemClass() == c) {
                return (byte)i;
            }
        }
        throw new IllegalStateException();
    }

}
