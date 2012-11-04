package nl.sense_os.commonsense.main.client.sensormanagement.publishing;

import nl.sense_os.commonsense.main.client.shared.ui.InfoDialog;

public interface PublicationCompleteView extends InfoDialog {

    void setInfo(String url, String title, String name, int[] sensorIds);

    void setPresenter(ConfirmPublicationView.Presenter presenter);
}
