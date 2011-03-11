package nl.sense_os.commonsense.shared;

import java.util.Map;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;

public class ServiceModel extends BaseTreeModel {

    private static final long serialVersionUID = 1L;
    public static final String DATA_FIELDS = "data_fields";
    public static final String NAME = "name";
    public static final String ID = "id";

    public ServiceModel() {
        super();
    }

    public ServiceModel(Map<String, Object> properties) {
        super(properties);
    }

    public ServiceModel(TreeModel parent) {
        super(parent);
    }

}
