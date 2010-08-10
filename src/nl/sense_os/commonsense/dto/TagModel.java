package nl.sense_os.commonsense.dto;

import com.extjs.gxt.ui.client.data.BaseTreeModel;


public class TagModel extends BaseTreeModel {

    private static final long serialVersionUID = 1L;
    public static final int TYPE_GROUP = 1;
    public static final int TYPE_DEVICE = 2;
    public static final int TYPE_SENSOR = 3;
    public static final int TYPE_USER = 4;    
    
    public TagModel() {
        // empty constructor
    }

    public TagModel(String path, int type) {
        setPath(path);
        setType(type);
    }

    public TagModel setPath(String path) {
        set("path", path);

        // get tag label
        int start = path.lastIndexOf("/", path.length() - 2) + 1;
        String label = path.substring(start, path.length() - 1);
        set("text", label);
        return this;
    }

    public TagModel setType(int type) {
        set("type", type);
        return this;
    }

    public String getPath() {
        return get("path");
    }
    
    public int getType() {
        return get("type", -1);
    }
}
