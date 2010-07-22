package nl.sense_os.commonsense.dto;

import com.extjs.gxt.ui.client.data.BaseTreeModel;

public class SenseTreeModel extends BaseTreeModel {
    
    private static final long serialVersionUID = 1L;

    public void setId(int id) {
        set("id", id);
    }
    
    /**
     * @return the ID, or -1 if it was not set.
     */
    public int getId() {
        return get("id", -1);
    }
}
