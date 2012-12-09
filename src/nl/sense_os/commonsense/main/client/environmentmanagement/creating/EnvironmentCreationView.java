package nl.sense_os.commonsense.main.client.environmentmanagement.creating;

import java.util.List;
import java.util.Map;

import nl.sense_os.commonsense.lib.client.model.apiclass.Device;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.shared.ui.HasBusyState;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Polygon;

public interface EnvironmentCreationView extends HasBusyState {

    public interface Presenter {

        void onCancelClick();

        void onSubmitClick();
    }

    Map<Device, LatLng> getDevicePositions();

    int getFloors();

    String getName();

    Polygon getOutline();

    List<GxtSensor> getSensors();

    void hide();

    void setPresenter(Presenter presenter);

    void show();
}
