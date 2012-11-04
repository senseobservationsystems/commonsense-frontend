package nl.sense_os.commonsense.main.client.sensormanagement.deleting;

import nl.sense_os.commonsense.main.client.shared.ui.HasBusyState;

public interface ConfirmRemovalView extends HasBusyState {

    public interface Presenter {

        void onCancelClick();

        void onDeleteClick();
    }

    void hide();

    void setConfirmationText(String text);

    void setPresenter(Presenter presenter);

    void show();
}
