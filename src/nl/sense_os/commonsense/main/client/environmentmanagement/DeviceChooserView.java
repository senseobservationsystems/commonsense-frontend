package nl.sense_os.commonsense.main.client.environmentmanagement;

import java.util.List;

import nl.sense_os.commonsense.lib.client.model.apiclass.Device;

public interface DeviceChooserView {

    public interface Presenter {

        void onSubmitClick();

        void onCancelClick();
    }

    void hide();

    List<Device> getDevices();

    void setDevices(List<Device> devices);

    void setPresenter(Presenter presenter);

    void show();
}
