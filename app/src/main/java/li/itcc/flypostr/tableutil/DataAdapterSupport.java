package li.itcc.flypostr.tableutil;

import android.database.DataSetObserver;

import java.util.ArrayList;

public class DataAdapterSupport {
    private ArrayList<DataSetObserver> fObservers = new ArrayList<DataSetObserver>();

    public void registerDataSetObserver(DataSetObserver observer) {
        fObservers.add(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        fObservers.remove(observer);
    }

    public void notifyOnChanged() {
        int size = fObservers.size();
        for (int i = 0; i < size; i++) {
            DataSetObserver observer = fObservers.get(i);
            observer.onChanged();
        }
    }

    public void notifyOnInvalidated() {
        int size = fObservers.size();
        for (int i = 0; i < size; i++) {
            DataSetObserver observer = fObservers.get(i);
            observer.onInvalidated();
        }
    }

}
