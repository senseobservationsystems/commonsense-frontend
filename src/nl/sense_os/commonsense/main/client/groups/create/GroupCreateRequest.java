package nl.sense_os.commonsense.main.client.groups.create;

import com.extjs.gxt.ui.client.mvc.AppEvent;

public class GroupCreateRequest extends AppEvent {

    private String name;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isUseJoinPassword() {
        return useJoinPassword;
    }

    public void setUseJoinPassword(boolean useJoinPassword) {
        this.useJoinPassword = useJoinPassword;
    }

    public String getJoinPassword() {
        return joinPassword;
    }

    public void setJoinPassword(String joinPassword) {
        this.joinPassword = joinPassword;
    }

    public String getRequiredSharing() {
        return requiredSharing;
    }

    public void setRequiredSharing(String requiredSharing) {
        this.requiredSharing = requiredSharing;
    }

    public String getOptionalSharing() {
        return optionalSharing;
    }

    public void setOptionalSharing(String optionalSharing) {
        this.optionalSharing = optionalSharing;
    }

    public boolean isAnonymousSharing() {
        return anonymousSharing;
    }

    public void setAnonymousSharing(boolean anonymousSharing) {
        this.anonymousSharing = anonymousSharing;
    }

    public boolean isShowUserId() {
        return showUserId;
    }

    public void setShowUserId(boolean showUserId) {
        this.showUserId = showUserId;
    }

    public boolean isShowUsername() {
        return showUsername;
    }

    public void setShowUsername(boolean showUsername) {
        this.showUsername = showUsername;
    }

    public boolean isShowFirstName() {
        return showFirstName;
    }

    public void setShowFirstName(boolean showFirstName) {
        this.showFirstName = showFirstName;
    }

    public boolean isShowSurname() {
        return showSurname;
    }

    public void setShowSurname(boolean showSurname) {
        this.showSurname = showSurname;
    }

    public boolean isShowEmail() {
        return showEmail;
    }

    public void setShowEmail(boolean showEmail) {
        this.showEmail = showEmail;
    }

    public boolean isShowPhone() {
        return showPhone;
    }

    public void setShowPhone(boolean showPhone) {
        this.showPhone = showPhone;
    }

    public boolean isAllowMemberCreate() {
        return allowMemberCreate;
    }

    public void setAllowMemberCreate(boolean allowMemberCreate) {
        this.allowMemberCreate = allowMemberCreate;
    }

    public boolean isAllowMemberRead() {
        return allowMemberRead;
    }

    public void setAllowMemberRead(boolean allowMemberRead) {
        this.allowMemberRead = allowMemberRead;
    }

    public boolean isAllowMemberDelete() {
        return allowMemberDelete;
    }

    public void setAllowMemberDelete(boolean allowMemberDelete) {
        this.allowMemberDelete = allowMemberDelete;
    }

    public boolean isAllowSensorCreate() {
        return allowSensorCreate;
    }

    public void setAllowSensorCreate(boolean allowSensorCreate) {
        this.allowSensorCreate = allowSensorCreate;
    }

    public boolean isAllowSensorRead() {
        return allowSensorRead;
    }

    public void setAllowSensorRead(boolean allowSensorRead) {
        this.allowSensorRead = allowSensorRead;
    }

    public boolean isAllowSensorDelete() {
        return allowSensorDelete;
    }

    public void setAllowSensorDelete(boolean allowSensorDelete) {
        this.allowSensorDelete = allowSensorDelete;
    }

    public boolean isUseGroupLogin() {
        return useGroupLogin;
    }

    public void setUseGroupLogin(boolean useGroupLogin) {
        this.useGroupLogin = useGroupLogin;
    }

    public String getGroupLogin() {
        return groupLogin;
    }

    public void setGroupLogin(String groupLogin) {
        this.groupLogin = groupLogin;
    }

    public String getGroupPassword() {
        return groupPassword;
    }

    public void setGroupPassword(String groupPassword) {
        this.groupPassword = groupPassword;
    }

    private boolean visible;
    private boolean useJoinPassword;
    private String joinPassword;

    private String requiredSharing;
    private String optionalSharing;
    private boolean anonymousSharing;
    private boolean showUserId;
    private boolean showUsername;
    private boolean showFirstName;
    private boolean showSurname;
    private boolean showEmail;
    private boolean showPhone;

    private boolean allowMemberCreate;
    private boolean allowMemberRead;
    private boolean allowMemberDelete;
    private boolean allowSensorCreate;
    private boolean allowSensorRead;
    private boolean allowSensorDelete;

    private boolean useGroupLogin;
    private String groupLogin;
    private String groupPassword;

    public GroupCreateRequest() {
        super(GroupCreateEvents.CreateRequested);
    }
}
