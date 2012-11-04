package nl.sense_os.commonsense.main.client.groupmanagement.leaving;

import nl.sense_os.commonsense.main.client.shared.ui.HasBusyState;

public interface ConfirmGroupLeaveView extends HasBusyState {

    public interface Presenter {

        void onCancelClick();

        void onLeaveClick();
    }

    void hide();

    void setPresenter(Presenter presenter);

    void show(String name);
}
