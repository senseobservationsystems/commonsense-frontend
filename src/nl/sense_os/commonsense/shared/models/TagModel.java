package nl.sense_os.commonsense.shared.models;

import com.extjs.gxt.ui.client.data.BaseTreeModel;

public class TagModel extends BaseTreeModel {

    private static final long serialVersionUID = 1L;
    public static final int TYPE_DEVICE = 2;
    public static final int TYPE_GROUP = 1;
    public static final int TYPE_SENSOR = 3;
    public static final int TYPE_USER = 4;
    public static final int TYPE_SERVICE = 5;
    public static final int TYPE_CATEGORY = 6;

    public TagModel() {
        // empty constructor
    }

    public TagModel(String path, int taggedId, int parentId, int type) {
        setPath(path);
        setTaggedId(taggedId);
        setParentId(parentId);
        setType(type);
    }

    public int getParentId() {
        return get("parent_id", -1);
    }

    public String getPath() {
        return get("path");
    }

    public int getTaggedId() {
        return get("tagged_id", -1);
    }

    public int getType() {
        return get("type", -1);
    }

    public TagModel setParentId(int id) {
        set("parent_id", id);
        return this;
    }

    public TagModel setPath(String path) {
        set("path", path);

        // get tag label
        int start = path.lastIndexOf("/", path.length() - 2) + 1;
        String label = path.substring(start, path.length() - 1);
        set("text", label);
        return this;
    }

    public TagModel setTaggedId(int id) {
        set("tagged_id", id);
        return this;
    }

    public TagModel setType(int type) {
        set("type", type);
        return this;
    }
}