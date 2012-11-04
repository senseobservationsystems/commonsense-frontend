package nl.sense_os.commonsense.main.client.sensormanagement.unsharing;

import nl.sense_os.commonsense.main.client.shared.ui.HasBusyState;

public interface ConfirmUnshareView extends HasBusyState {

    public interface Presenter {
        void onCancelClick();

        void onSubmitClick();
    }

    void hide();

    void setPresenter(Presenter presenter);

    void show();
}
