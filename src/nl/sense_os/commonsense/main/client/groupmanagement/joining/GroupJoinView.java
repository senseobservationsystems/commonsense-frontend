package nl.sense_os.commonsense.main.client.groupmanagement.joining;

import java.util.List;

import nl.sense_os.commonsense.main.client.gxt.model.GxtGroup;
import nl.sense_os.commonsense.main.client.gxt.model.GxtSensor;
import nl.sense_os.commonsense.main.client.shared.ui.HasBusyState;

public interface GroupJoinView extends HasBusyState {

    public interface Presenter {
        void onCancelClick();

        void onGroupDetailsRequest(GxtGroup group);

        void onSubmitClick();
    }

    GxtGroup getGroup();

    List<GxtSensor> getSharedSensors();

    void hide();

    void setGroupDetails(GxtGroup group);

    void setPresenter(Presenter presenter);

    void show();
}
