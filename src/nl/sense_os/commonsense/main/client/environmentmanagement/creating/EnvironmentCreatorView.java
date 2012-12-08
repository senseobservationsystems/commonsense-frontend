package nl.sense_os.commonsense.main.client.environmentmanagement.creating;

import java.util.List;

import nl.sense_os.commonsense.main.client.gxt.model.GxtDevice;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.shared.ui.HasBusyState;

import com.google.gwt.maps.client.overlay.Polygon;

public interface EnvironmentCreatorView extends HasBusyState {

    public interface Presenter {

        void onCancelClick();

        void onSubmitClick();
    }

    List<GxtDevice> getDevices();

    int getFloors();

    String getName();

    Polygon getOutline();

    List<GxtSensor> getSensors();

    void setPresenter(Presenter presenter);
}
