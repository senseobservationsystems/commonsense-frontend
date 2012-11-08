package nl.sense_os.commonsense.main.client.groupmanagement.joining;

import nl.sense_os.commonsense.main.client.groupmanagement.joining.GroupJoinView.Presenter;

public interface GroupListWaitView {

    void hide();

    void setPresenter(Presenter presenter);

    void show();

}
