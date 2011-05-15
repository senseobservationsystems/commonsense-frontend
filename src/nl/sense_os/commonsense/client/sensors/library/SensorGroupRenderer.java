package nl.sense_os.commonsense.client.sensors.library;

import nl.sense_os.commonsense.shared.SensorModel;

import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;

/**
 * Renderer for groups of sensors.
 */
public class SensorGroupRenderer implements GridGroupRenderer {

    @Override
    public String render(GroupColumnData data) {

        String field = data.group;
        if (data.field.equals(SensorModel.TYPE)) {
            int group = Integer.parseInt(data.group);
            switch (group) {
            case 0:
                field = "Feeds";
                break;
            case 1:
                field = "Physical";
                break;
            case 2:
                field = "States";
                break;
            case 3:
                field = "Environment sensors";
                break;
            case 4:
                field = "Public sensors";
                break;
            default:
                field = "Unsorted";
            }
        } else {
            if (data.group.equals("")) {
                return "Ungrouped";
            } else {
                return data.group;
            }
        }

        String count = data.models.size() == 1 ? "Sensor" : "Sensors";
        return field + " (" + data.models.size() + " " + count + ")";
    }
}
