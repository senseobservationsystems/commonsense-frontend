package nl.sense_os.commonsense.client.groups.create.forms;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

public abstract class AbstractGroupForm extends FormPanel {

    protected final FormData layoutData = new FormData("-10");

    public AbstractGroupForm() {
        super();
        setHeaderVisible(false);
        setBodyBorder(false);
        setLayout(new FormLayout(LabelAlign.TOP));
        setScrollMode(Scroll.AUTOY);
    }
}
