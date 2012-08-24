package nl.sense_os.commonsense.main.client.viz.tabs;

import nl.sense_os.commonsense.main.client.gxt.util.SenseIconProvider;

import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.LayoutData;
import com.google.gwt.user.client.ui.Frame;

/**
 * Composite for visualization tab panel. Shows a welcome tab by default.
 */
public class VizTabPanel extends TabPanel {

    private final TabItem tabItemWelcome;

    public VizTabPanel() {
        setId("viz-tab-panel");
        setSize("100%", "100%");
        setPlain(true);
        setMinTabWidth(120);
        setResizeTabs(true);
        addStyleName("transparent");

        // Welcome tab item
        final Frame frameWelcome = new Frame("http://welcome.sense-os.nl/node/9");
        frameWelcome.setStylePrimaryName("senseFrame");

        tabItemWelcome = new TabItem("Welcome");
        tabItemWelcome.setIcon(IconHelper.create(SenseIconProvider.SENSE_ICONS_PATH + "help.png"));
        tabItemWelcome.setLayout(new FitLayout());
        LayoutData data = new FitData(new Margins(0));
        tabItemWelcome.add(frameWelcome, data);

        add(tabItemWelcome);
    }

    public TabItem getTabItemWelcome() {
        return tabItemWelcome;
    }
}
