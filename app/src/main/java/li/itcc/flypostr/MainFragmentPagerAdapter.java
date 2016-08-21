package li.itcc.flypostr;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import li.itcc.flypostr.postingList.PostingListFragment;
import li.itcc.flypostr.postingMap.PostingMapFragment;

/**
 * Created by Arthur on 21.08.2016.
 */

public class MainFragmentPagerAdapter extends FragmentPagerAdapter {
    private Context context;

    public MainFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new PostingMapFragment();
        }
        else if (position == 1) {
            return new PostingListFragment();
        }
        throw new RuntimeException();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return this.context.getString(R.string.txt_map);
        }
        else if (position == 1) {
            return this.context.getString(R.string.txt_list);
        }
        throw new RuntimeException();
    }
}