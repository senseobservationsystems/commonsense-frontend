package nl.sense_os.commonsense.dto;

import com.extjs.gxt.ui.client.data.BaseTreeModel;

public class SenseTreeModel extends BaseTreeModel {
    
    private static final long serialVersionUID = 1L;

    public void setId(String id) {
        set("id", id);
    }
    
    public String getId() {
        return get("id");
    }
}
