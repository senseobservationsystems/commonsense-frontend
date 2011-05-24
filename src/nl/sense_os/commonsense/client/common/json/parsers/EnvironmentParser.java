package nl.sense_os.commonsense.client.common.json.parsers;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.models.EnvironmentModel;
import nl.sense_os.commonsense.client.common.models.TagModel;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Polygon;

public class EnvironmentParser {

    private static final Logger logger = Logger.getLogger("EnvironmentParser");

    public static EnvironmentModel parse(JSONObject json) {

        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put(EnvironmentModel.ID, json.get(EnvironmentModel.ID).isString().stringValue());
        props.put(EnvironmentModel.NAME, json.get(EnvironmentModel.NAME).isString().stringValue());

        // optional properties
        if (null != json.get(EnvironmentModel.FLOORS)) {
            props.put(EnvironmentModel.FLOORS, json.get(EnvironmentModel.FLOORS).isString()
                    .stringValue());
        }
        if (null != json.get(EnvironmentModel.OUTLINE)) {
            String outlineValue = json.get(EnvironmentModel.OUTLINE).isString().stringValue();
            String[] values = outlineValue.split(";");
            LatLng[] points = new LatLng[values.length];
            for (int i = 0; i < values.length; i++) {
                points[i] = LatLng.fromUrlValue(values[i]);
            }
            props.put(EnvironmentModel.OUTLINE, new Polygon(points));
        }
        if (null != json.get(EnvironmentModel.POSITION)) {
            String positionValue = json.get(EnvironmentModel.POSITION).isString().stringValue();
            LatLng position = LatLng.fromUrlValue(positionValue);
            props.put(EnvironmentModel.POSITION, position);
        }
        if (null != json.get(EnvironmentModel.DATE)) {
            props.put(EnvironmentModel.DATE,
                    Math.round(json.get(EnvironmentModel.DATE).isNumber().doubleValue()));
        }

        // front end-only properties
        props.put("tagType", TagModel.TYPE_CATEGORY);
        props.put("text", props.get(EnvironmentModel.NAME));

        return new EnvironmentModel(props);
    }

    public static void parseList(String jsonString, List<EnvironmentModel> list) {

        try {
            JSONObject json = JSONParser.parseStrict(jsonString).isObject();

            // get array of raw sensors
            JSONArray environments = json.get("environments").isArray();

            JSONObject environment;
            for (int i = 0; i < environments.size(); i++) {
                environment = environments.get(i).isObject();
                list.add(parse(environment));
            }

        } catch (Exception e) {
            logger.severe("Exception parsing sensors list: " + e.getMessage());
            logger.severe("Raw response: " + jsonString);
        }
    }
}
