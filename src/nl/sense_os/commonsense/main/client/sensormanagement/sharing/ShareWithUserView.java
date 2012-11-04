package nl.sense_os.commonsense.main.client.sensormanagement.sharing;

import nl.sense_os.commonsense.main.client.shared.ui.HasBusyState;

public interface ShareWithUserView extends HasBusyState {

    public interface Presenter {

        void onSubmitClick();

        void onCancelClick();

    }

    String getUsername();

    void setPresenter(Presenter presenter);

    void hide();

    void show();
}
