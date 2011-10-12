package nl.sense_os.commonsense.client.alerts.create.utils;

import com.extjs.gxt.ui.client.data.BaseModel;

public class StringSensorValue extends BaseModel {

    private static final long serialVersionUID = 1L;

    private String name;

    public StringSensorValue() {
        super();
    }

    public StringSensorValue(String name) {
        super();
        set("name", name);
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
