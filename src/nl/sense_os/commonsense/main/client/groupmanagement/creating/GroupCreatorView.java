package nl.sense_os.commonsense.main.client.groupmanagement.creating;

import java.util.List;

import nl.sense_os.commonsense.main.client.shared.ui.HasBusyState;

public interface GroupCreatorView extends HasBusyState {

    public interface Presenter {

        void onCancelClick();

        void onCreateClick();
    }

    String getAccessPassword();

    String getGroupDescription();

    String getGroupName();

    String getGroupPassword();

    String getGroupUsername();

    List<String> getOptSensors();

    String getPresetChoice();

    List<String> getReqSensors();

    void hide();

    boolean isCreateMembers();

    boolean isCreateSensors();

    boolean isDeleteMembers();

    boolean isDeleteSensors();

    boolean isEmailRequired();

    boolean isFirstNameRequired();

    boolean isGroupAnonymous();

    boolean isGroupHidden();

    boolean isGroupLogin();

    boolean isGroupPublic();

    boolean isPhoneRequired();

    boolean isReadMembers();

    boolean isReadSensors();

    boolean isSurnameRequired();

    boolean isUserIdRequired();

    boolean isUsernameRequired();

    void setPresenter(Presenter presenter);

    void show();
}
