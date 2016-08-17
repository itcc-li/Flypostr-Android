package li.itcc.flypostr.services;

import android.content.Context;

import java.util.ArrayList;

import li.itcc.flypostr.backend.ImageUploadUrlBean;
import li.itcc.flypostr.backend.PoiCreateBean;
import li.itcc.flypostr.backend.PoiDetailBean;
import li.itcc.flypostr.backend.PoiOverviewBean;
import li.itcc.flypostr.backend.PoiOverviewListBean;


/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiServices {
    private final String Url;
    private final Context Context;

    public PoiServices(Context context, String url) {
        this.Context = context;
        this.Url = url;
    }

    // dummy implementation

    public PoiOverviewListBean getPoiList(PoiOverviewQuery query) throws Exception {
        PoiOverviewListBean result = new PoiOverviewListBean();
        result.setList(new ArrayList<PoiOverviewBean>());
        return result;
    }

    public PoiOverviewBean insertPoi(PoiCreateBean newPoi) throws Exception {
        PoiOverviewBean bean = new PoiOverviewBean();
        return bean;
    }

    public ImageUploadUrlBean getImageUploadUrl() throws Exception {
        ImageUploadUrlBean result = new ImageUploadUrlBean();
        return result;
    }

    public PoiDetailBean getPoiDetails(String poiUuid) throws Exception {
        PoiDetailBean result = new PoiDetailBean();
        return result;
    }

}
