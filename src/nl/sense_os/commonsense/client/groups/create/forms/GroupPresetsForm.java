package nl.sense_os.commonsense.client.groups.create.forms;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

public class GroupPresetsForm extends FormPanel {

    public class AnonymousRadio extends Radio {

    };
    public class PrivateRadio extends Radio {

    };
    public class CommunityRadio extends Radio {

    }
    public class CustomRadio extends Radio {

    }

    private final AnonymousRadio anonymous = new AnonymousRadio();
    private final PrivateRadio hidden = new PrivateRadio();
    private final CommunityRadio community = new CommunityRadio();
    private final CustomRadio custom = new CustomRadio();
    private final RadioGroup presets = new RadioGroup();
    private final TextField<String> privatePass = new TextField<String>();

    public GroupPresetsForm() {

        // init radio fields
        anonymous.setBoxLabel("Anonymous");
        anonymous.setHideLabel(true);
        hidden.setBoxLabel("Private");
        hidden.setHideLabel(true);
        community.setBoxLabel("Community");
        community.setHideLabel(true);
        custom.setBoxLabel("Custom");
        custom.setHideLabel(true);

        // select anonymous
        anonymous.setValue(true);
        anonymous.setOriginalValue(true);

        // radio presets manages selection
        presets.add(anonymous);
        presets.add(hidden);
        presets.add(community);
        presets.add(custom);
        presets.setSelectionRequired(true);
        presets.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                onSelectionChange((Radio) be.getField().getValue());
            }
        });

        privatePass.setEmptyText("Enter a group password");
        privatePass.setAllowBlank(false);
        privatePass.setHideLabel(true);
        privatePass.setEnabled(false);

        LabelField mainLabel = new LabelField();
        mainLabel.setFieldLabel("Pick a group preset:");

        LabelField anonyLabel = new LabelField(
                "An anonymous group is visible for everyone and anyone can join the group."
                        + "<br>" + "Members share their sensor data anonymously.");
        anonyLabel.setHideLabel(true);
        LabelField hiddenLabel = new LabelField(
                "A private group is hidden and the group has a password for new members." + "<br>"
                        + "Members can see eachother's shared sensors and all user details.");
        hiddenLabel.setHideLabel(true);
        LabelField communityLabel = new LabelField(
                "A community group is visible for everyone and anyone can join the group." + "<br>"
                        + "Members can see eachother's shared sensors and some user details.");
        communityLabel.setHideLabel(true);
        LabelField customLabel = new LabelField("Only for true sensei with special needs.");
        customLabel.setHideLabel(true);

        // init layout
        setLayout(new FormLayout(LabelAlign.TOP));
        setHeaderVisible(false);
        setBodyBorder(false);
        FormData data = new FormData("0");
        add(mainLabel, data);
        add(anonymous, data);
        add(anonyLabel, data);
        add(hidden, data);
        add(hiddenLabel, data);
        add(privatePass, new FormData("-10"));
        add(community, data);
        add(communityLabel, data);
        add(custom, data);
        add(customLabel, data);
    }

    private void onSelectionChange(Radio selection) {
        boolean isPrivate = selection.equals(hidden);
        privatePass.setEnabled(isPrivate);
        privatePass.setAllowBlank(!isPrivate);
        privatePass.validate();
    }

    public RadioGroup getPresets() {
        return presets;
    }

    public String getPrivatePass() {
        return privatePass.getValue();
    }
}
