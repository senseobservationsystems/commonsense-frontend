package nl.sense_os.commonsense.dto.sensorvalues;

import com.extjs.gxt.ui.client.data.BaseModel;

import nl.sense_os.commonsense.dto.TagModel;

public class TaggedDataModel extends BaseModel {

    private static final long serialVersionUID = 1L;

    public TaggedDataModel() {
        
    }
    
    public TaggedDataModel(TagModel tag, SensorValueModel[] data) {
        setTag(tag);
        setData(data);
    }
    
    public TaggedDataModel setTag(TagModel tag) {
        set("tag", tag);
        return this;
    }
    
    public TaggedDataModel setData(SensorValueModel[] data) {
        set("data", data);
        return this;
    }
    
    public TagModel getTag() {
        return get("tag");
    }
    
    public SensorValueModel[] getData() {
        return get("data");
    }
}
