package nl.sense_os.commonsense.main.client.shared.ui;

public interface ErrorDialog {

    void hide();

    void show(int code, Throwable error);
}
