package nl.sense_os.commonsense.main.client.ext.component;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

/**
 * Panel that can be used to create panels in a wizard, saving some boilerplate code for scenario's
 * where the user has to enter details in small forms. It is basically a FormPanel without heading
 * and body border and such.<br>
 * <br>
 * Note: subclasses have to call through to the constructor of the superclass!
 */
public class WizardFormPanel extends FormPanel {

    protected String anchorSpec = "-25";

    public WizardFormPanel() {
        setHeaderVisible(false);
        setBodyBorder(false);
        setLayout(new FormLayout(LabelAlign.TOP));
        setScrollMode(Scroll.AUTOY);
    }
}
