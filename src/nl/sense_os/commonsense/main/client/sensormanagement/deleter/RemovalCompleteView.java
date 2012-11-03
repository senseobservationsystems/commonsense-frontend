package nl.sense_os.commonsense.main.client.sensormanagement.deleter;

public interface RemovalCompleteView {

    void hide();

    void setPresenter(ConfirmRemovalView.Presenter presenter);

    void show();
}
