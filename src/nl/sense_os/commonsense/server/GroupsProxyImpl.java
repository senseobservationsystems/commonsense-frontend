package nl.sense_os.commonsense.server;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.services.GroupsProxy;
import nl.sense_os.commonsense.server.utility.GroupConverter;
import nl.sense_os.commonsense.server.utility.UserConverter;
import nl.sense_os.commonsense.shared.Constants;
import nl.sense_os.commonsense.shared.UserModel;
import nl.sense_os.commonsense.shared.exceptions.DbConnectionException;
import nl.sense_os.commonsense.shared.exceptions.WrongResponseException;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class GroupsProxyImpl extends RemoteServiceServlet implements GroupsProxy {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger("GroupsProxyImpl");
    private static final long serialVersionUID = 1L;

    @Override
    public List<TreeModel> getGroups(String sessionId) throws DbConnectionException,
            WrongResponseException {

        // get list of groups
        String url = Constants.URL_GROUPS;
        String response = Requester.request(url, sessionId, "GET", null);
        List<ModelData> groupsIds = GroupConverter.parseGroupIds(response);

        List<TreeModel> groups = new ArrayList<TreeModel>();
        for (ModelData model : groupsIds) {
            String groupId = model.get("group_id");

            url = Constants.URL_GROUPS + "/" + groupId;
            response = Requester.request(url, sessionId, "GET", null);
            ModelData details = GroupConverter.parseGroup(response);

            url = Constants.URL_GROUPS + "/" + groupId + "/users";
            response = Requester.request(url, sessionId, "GET", null);
            List<UserModel> users = UserConverter.parseGroupUsers(response);

            TreeModel group = new BaseTreeModel(details.getProperties());
            for (ModelData userModel : users) {
                group.add(new BaseTreeModel(userModel.getProperties()));
            }
            groups.add(group);
        }
        return groups;
    }
}
