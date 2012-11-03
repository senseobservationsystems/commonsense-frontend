package nl.sense_os.commonsense.main.client.sensormanagement.publisher;


public interface PublicationFailureView {

    void hide();

    void setPresenter(ConfirmPublicationView.Presenter presenter);

    void show(int code, Throwable error);
}
