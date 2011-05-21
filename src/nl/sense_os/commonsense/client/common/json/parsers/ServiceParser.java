package nl.sense_os.commonsense.client.common.json.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.shared.models.ServiceModel;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

public class ServiceParser {

    private static final Logger LOGGER = Logger.getLogger(ServiceParser.class.getName());

    public static ServiceModel parse(JSONObject object) {
        HashMap<String, Object> props = new HashMap<String, Object>();
        if (null != object.get(ServiceModel.ID)) {
            props.put(ServiceModel.ID, object.get(ServiceModel.ID).isString().stringValue());
        }
        props.put(ServiceModel.NAME, object.get(ServiceModel.NAME).isString().stringValue());

        JSONArray rawDataFields = object.get(ServiceModel.DATA_FIELDS).isArray();
        List<String> dataFields = new ArrayList<String>();
        if (null != rawDataFields) {
            for (int i = 0; i < rawDataFields.size(); i++) {
                dataFields.add(rawDataFields.get(i).isString().stringValue());
            }
        }
        props.put(ServiceModel.DATA_FIELDS, dataFields);

        return new ServiceModel(props);
    }

    public static List<ServiceModel> parseList(String json) {
        List<ServiceModel> result = new ArrayList<ServiceModel>();
        try {
            JSONObject parsed = JSONParser.parseLenient(json).isObject();
            JSONValue services = parsed.get("available_services");
            if (null == services) {
                services = parsed.get("services");
            }

            JSONArray serviceArray = services.isArray();
            for (int i = 0; i < serviceArray.size(); i++) {
                result.add(parse(serviceArray.get(i).isObject()));
            }
        } catch (Exception e) {
            LOGGER.severe("Exception parsing services: " + e.getMessage());
            LOGGER.severe("Raw JSON: " + json);
        }
        return result;
    }
}
