package nl.sense_os.commonsense.client.visualization.map.components;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class MapPanel extends ContentPanel {

    public MapPanel() {
        setLayout(new FitLayout());
        setHeaderVisible(false);
        setBodyBorder(false);
        setScrollMode(Scroll.NONE);
        setId("viz-map");
    }
}
