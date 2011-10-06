package nl.sense_os.commonsense.client.groups.list;

import java.util.ArrayList;
import java.util.List;

import nl.sense_os.commonsense.client.common.models.GroupJso;
import nl.sense_os.commonsense.client.common.models.NewGroupModel;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class GetGroupsResponseJso extends JavaScriptObject {

    protected GetGroupsResponseJso() {
        // empty protected constructor
    }

    public final native JsArray<GroupJso> getRawGroups() /*-{
        if (undefined != this.groups) {
            return this.groups;
        } else {
            return [];
        }
    }-*/;

    public final List<NewGroupModel> getGroups() {
        List<NewGroupModel> list = new ArrayList<NewGroupModel>();

        JsArray<GroupJso> groups = getRawGroups();

        if (null != groups) {
            for (int i = 0; i < groups.length(); i++) {
                list.add(new NewGroupModel(groups.get(i)));
            }
        }

        return list;
    }
}
