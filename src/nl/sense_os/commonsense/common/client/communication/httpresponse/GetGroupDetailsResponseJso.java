package nl.sense_os.commonsense.common.client.communication.httpresponse;

import nl.sense_os.commonsense.common.client.model.GroupJso;
import nl.sense_os.commonsense.common.client.model.GroupModel;

import com.google.gwt.core.client.JavaScriptObject;

public class GetGroupDetailsResponseJso extends JavaScriptObject {

    protected GetGroupDetailsResponseJso() {
        // empty protected constructor
    }

    public final native GroupJso getRawGroup() /*-{
        if (undefined != this.group) {
            return this.group;
        } else {
            return {};
        }
    }-*/;

    public final GroupModel getGroup() {
        GroupJso jso = getRawGroup();
        return new GroupModel(jso);
    }
}
