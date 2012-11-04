package nl.sense_os.commonsense.main.client.sensormanagement.publishing;

import nl.sense_os.commonsense.main.client.shared.ui.ErrorDialog;

public interface PublicationFailureView extends ErrorDialog {

    void setPresenter(ConfirmPublicationView.Presenter presenter);
}
