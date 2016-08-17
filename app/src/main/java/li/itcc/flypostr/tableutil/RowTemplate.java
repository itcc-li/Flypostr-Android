package li.itcc.flypostr.tableutil;

import android.view.View;

public abstract class RowTemplate<ENTRY, HOLDER> {
    private int fItemLayoutId;
    private Class<ENTRY> fItemClass;
    private boolean fAllItemsEnabled;

    public RowTemplate(Class<ENTRY> itemClass, int itemLayoutId, boolean allItemsEnabled) {
        fItemClass = itemClass;
        fItemLayoutId = itemLayoutId;
        fAllItemsEnabled = allItemsEnabled;
    }

    public Class<ENTRY> getItemClass() {
        return fItemClass;
    }

    public int getItemLayoutId() {
        return fItemLayoutId;
    }

    public boolean areAllItemsEnabled() {
        return fAllItemsEnabled;
    }

    @SuppressWarnings("unchecked")
    public final boolean internalIsEnabled(Object item) {
        return onIsEnabled((ENTRY)item);
    }

    public HOLDER createViewHolder(View view) {
        return onCreateViewHolder(view);
    }

    @SuppressWarnings("unchecked")
    public final void internalOnLoad(Object viewHolder, Object item) {
        onLoad((HOLDER)viewHolder, (ENTRY)item);
    }

    protected boolean onIsEnabled(ENTRY item) {
        return fAllItemsEnabled;
    }

    protected abstract HOLDER onCreateViewHolder(View view);

    protected abstract void onLoad(HOLDER holder, ENTRY item);

}
