package nl.sense_os.commonsense.main.client.groupmanagement.inviting;

import nl.sense_os.commonsense.main.client.shared.ui.HasBusyState;

public interface InviteUserView extends HasBusyState {

    public interface Presenter {
        void onCancelClick();

        void onSubmitClick();
    }

    String getUsername();

    void setPresenter(Presenter presenter);

    void hide();

    void show();
}
