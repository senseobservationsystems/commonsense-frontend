package nl.sense_os.commonsense.main.client.sensormanagement.deleter;

import com.google.gwt.user.client.ui.IsWidget;

public interface ConfirmRemovalView extends IsWidget {

    public interface Presenter {

        void onCancelClick();

        void onDeleteClick();
    }

    void hide();

    void setBusy(boolean busy);

    void setConfirmationText(String text);

    void setPresenter(Presenter presenter);

    void show();
}
