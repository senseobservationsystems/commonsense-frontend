package nl.sense_os.commonsense.main.client.environmentmanagement;

import java.util.List;
import java.util.Map;

import nl.sense_os.commonsense.lib.client.model.apiclass.Device;
import nl.sense_os.commonsense.lib.client.model.apiclass.Environment;
import nl.sense_os.commonsense.lib.client.model.apiclass.Sensor;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Polygon;
import com.google.gwt.user.client.ui.IsWidget;

public interface EnvironmentDisplayView extends IsWidget {

    public interface Presenter {

        void onOutlineComplete();

        void onDeviceAddClick(LatLng latLng);
    }

    void setSensors(List<Sensor> sensors, List<String> positions);

    void setEnvironment(Environment environment);

    void setPresenter(Presenter presenter);

    Polygon getOutline();

    Map<Device, LatLng> getDevices();

    void addDevice(LatLng latLng, List<Device> devices);
}
