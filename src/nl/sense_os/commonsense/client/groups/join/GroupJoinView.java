package nl.sense_os.commonsense.client.groups.join;

import java.util.List;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.common.constants.Constants;
import nl.sense_os.commonsense.client.common.models.GroupModel;
import nl.sense_os.commonsense.client.common.models.SensorModel;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.widget.MessageBox;

public class GroupJoinView extends View {

    private static final Logger LOG = Logger.getLogger(GroupJoinView.class.getName());
    private GroupJoinDialog window;
    private PagingLoader<PagingLoadResult<GroupModel>> loader;
    private MessageBox progress;

    public GroupJoinView(Controller c) {
        super(c);
    }

    private void getAllGroups() {
        progress = MessageBox.wait("Join group", "Please wait...", "Getting list of groups");
        AppEvent loadRequest = new AppEvent(GroupJoinEvents.AllGroupsRequest);
        loadRequest.setSource(this);
        fireEvent(loadRequest);
    }

    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type.equals(GroupJoinEvents.Show)) {
            LOG.finest("Show");
            getAllGroups();

        } else if (type.equals(GroupJoinEvents.AllGroupsSuccess)) {
            LOG.finest("AllGroupsSuccess");
            List<GroupModel> groups = event.getData("groups");
            onAllGroupsSuccess(groups);

        } else if (type.equals(GroupJoinEvents.AllGroupsFailure)) {
            LOG.finest("AllGroupsSuccess");
            onAllGroupsFailure();

        } else if (type.equals(GroupJoinEvents.JoinSuccess)) {
            LOG.finest("JoinSuccess");
            onSuccess();

        } else if (type.equals(GroupJoinEvents.JoinFailure)) {
            LOG.warning("JoinFailure");
            onFailure();

        } else {
            LOG.warning("Unexpected event: " + event);
        }
    }

    private void hideWindow() {
        window.hide();
    }

    private void onAllGroupsFailure() {
        progress.close();
    }

    private void onAllGroupsSuccess(List<GroupModel> groups) {
        progress.close();

        // data proxy
        PagingModelMemoryProxy proxy = new PagingModelMemoryProxy(groups);

        // group list loader
        loader = new BasePagingLoader<PagingLoadResult<GroupModel>>(proxy);

        // list of sensors
        List<SensorModel> sensors = Registry.<List<SensorModel>> get(Constants.REG_SENSOR_LIST);

        // create dialog
        window = new GroupJoinDialog(loader, sensors);

        window.getBtnSubmit().addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                if (ce.getButton().getText().equalsIgnoreCase("next")) {
                    window.goToNext();
                } else {
                    submitForm();
                }
            }
        });
        window.getBtnBack().addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                window.goToPrev();
            }
        });
        window.getBtnCancel().addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                hideWindow();
            }
        });

        window.show();

        loader.load();
    }

    private void onFailure() {
        window.setBusy(false);

        GroupModel group = window.getGroup();
        MessageBox.confirm("CommonSense", "Failed to join the group " + group.getName()
                + "! Do you want to retry?", new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                if (be.getButtonClicked().getText().equalsIgnoreCase("yes")) {
                    submitForm();
                } else {
                    hideWindow();
                }
            }
        });
    }

    private void onSuccess() {
        window.setBusy(false);

        GroupModel group = window.getGroup();
        MessageBox.info("CommonSense", "You have joined the group " + group.getName() + ".", null);
    }

    private void submitForm() {

        GroupModel group = window.getGroup();

        AppEvent event = new AppEvent(GroupJoinEvents.JoinRequest);
        event.setData("group", group);
        event.setSource(this);
        Dispatcher.forwardEvent(event);

        window.setBusy(true);
    }
}
