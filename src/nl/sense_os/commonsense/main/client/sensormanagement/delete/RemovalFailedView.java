package nl.sense_os.commonsense.main.client.sensormanagement.delete;


public interface RemovalFailedView {

    void setPresenter(ConfirmRemovalView.Presenter presenter);

    void show();

    void hide();
}
