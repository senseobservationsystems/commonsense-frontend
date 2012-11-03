package nl.sense_os.commonsense.main.client.sensormanagement.publisher;

public interface ConfirmPublicationView {

    public interface Presenter {

        void onCancelClick();

        void onPublishClick();
    }

    void hide();

    boolean isAnonymous();

    void setBusy(boolean busy);

    void setNumberOfSensors(int nr);

    void setPresenter(Presenter presenter);

    void show();
}
