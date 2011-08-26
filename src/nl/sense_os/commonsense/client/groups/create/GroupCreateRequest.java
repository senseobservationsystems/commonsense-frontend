package nl.sense_os.commonsense.client.groups.create;

import com.extjs.gxt.ui.client.mvc.AppEvent;

public class GroupCreateRequest extends AppEvent {

    public GroupCreateRequest() {
        super(GroupCreateEvents.CreateRequested);
    }
}
