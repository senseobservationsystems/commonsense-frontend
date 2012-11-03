package nl.sense_os.commonsense.main.client.sensormanagement.publisher;


public interface PublicationCompleteView {

    void hide();

    void setPresenter(ConfirmPublicationView.Presenter presenter);

    void show(String url, String title, String name, int[] sensorIds);
}
