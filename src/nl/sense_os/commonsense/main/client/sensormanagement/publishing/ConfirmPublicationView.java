package nl.sense_os.commonsense.main.client.sensormanagement.publishing;

import nl.sense_os.commonsense.main.client.shared.ui.HasBusyState;

public interface ConfirmPublicationView extends HasBusyState {

    public interface Presenter {

        void onCancelClick();

        void onPublishClick();
    }

    void hide();

    boolean isAnonymous();

    void setNumberOfSensors(int nr);

    void setPresenter(Presenter presenter);

    void show();
}
