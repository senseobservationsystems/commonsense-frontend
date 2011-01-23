package nl.sense_os.commonsense.client.mvc.controllers;

import java.util.List;

import nl.sense_os.commonsense.client.mvc.events.LoginEvents;
import nl.sense_os.commonsense.client.mvc.events.TagsEvents;
import nl.sense_os.commonsense.client.mvc.views.TagsView;
import nl.sense_os.commonsense.client.services.TagServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.shared.Constants;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class TagsController extends Controller {
    
    private static final String TAG = "TagSController";
    private TagsView tagsView;
    
    public TagsController() {
        registerEventTypes(TagsEvents.ShowTags, TagsEvents.TagsNotUpdated, TagsEvents.TagsRequested, TagsEvents.TagsUpdated);
        registerEventTypes(LoginEvents.LoggedOut);
    }

    @Override
    public void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(TagsEvents.TagsRequested)) {
            Log.d(TAG, "TagsRequested");
            onTagsRequested(event);
        }
        forwardToView(this.tagsView, event);
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.tagsView = new TagsView(this);
    }

    private void onTagsRequested(AppEvent event) {
        TagServiceAsync service = Registry.<TagServiceAsync> get(Constants.REG_TAG_SVC);
        String sessionId = Registry.get(Constants.REG_SESSION_ID);
        AsyncCallback<List<TreeModel>> callback = new AsyncCallback<List<TreeModel>>() {

            @Override
            public void onFailure(Throwable caught) {
                Dispatcher.forwardEvent(TagsEvents.TagsNotUpdated, caught);
            }

            @Override
            public void onSuccess(List<TreeModel> result) {
                Dispatcher.forwardEvent(TagsEvents.TagsUpdated, result);
            }
        };
        service.getTags(sessionId, callback);
    }
}
