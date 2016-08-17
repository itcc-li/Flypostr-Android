package li.itcc.flypostr.backend;

import java.util.List;

/**
 * The object model for the data we are sending through endpoints
 */
public class PoiOverviewListBean {
    private List<PoiOverviewBean> list;


    public List<PoiOverviewBean> getList() {
        return list;
    }

    public void setList(List<PoiOverviewBean> list) {
        this.list = list;
    }


}