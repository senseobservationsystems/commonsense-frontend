package nl.sense_os.commonsense.client.visualization.components;

import java.util.List;

import nl.sense_os.commonsense.shared.sensorvalues.TaggedDataModel;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

public abstract class VisualizationTab extends LayoutContainer {

    private final Text requestText;

    public VisualizationTab() {
        // set up layout
        setLayout(new RowLayout(Orientation.VERTICAL));
        setScrollMode(Scroll.AUTOY);

        // show "waiting..." bar
        Text waiting = new Text("Waiting for data...");
        waiting.setStyleName("notification-bar");
        waiting.setWidth(150);
        waiting.setVisible(false);

        this.requestText = waiting;
        this.add(this.requestText, new RowData(1, -1, new Margins(0)));
    }

    public abstract void addData(TaggedDataModel taggedData);
    public abstract void addData(List<TaggedDataModel> taggedDatas);
    public void setWaitingText(boolean visible) {
        this.requestText.setVisible(visible);
    }
}
