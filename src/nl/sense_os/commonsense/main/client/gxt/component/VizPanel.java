package nl.sense_os.commonsense.main.client.gxt.component;

import nl.sense_os.commonsense.common.client.model.Timeseries;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.google.gwt.core.client.JsArray;

public abstract class VizPanel extends ContentPanel {

    private ToolButton refresh;
    private ToolButton autoRefresh;

    /**
     * Adds tool buttons to the panel's heading.
     */
    protected void showToolButtons() {

        // regular refresh button
        refresh = new ToolButton("x-tool-refresh");
        refresh.setToolTip("refresh");

        // auto-refresh button
        autoRefresh = new ToolButton("x-tool-right");
        autoRefresh.setToolTip("start auto-refresh");

        // add buttons to the panel's header
        Header header = getHeader();
        header.addTool(autoRefresh);
        header.addTool(refresh);
    }

    public ToolButton getAutoRefresh() {
        return autoRefresh;
    }

    public ToolButton getRefresh() {
        return refresh;
    }

    /**
     * Called when the data was changed and the panel should update accordingly.
     * 
     * @param Array
     *            with the new data to visualize. The data is also stored in the panel's
     *            <code>data</code> field.
     */
    protected abstract void onNewData(JsArray<Timeseries> data);
}
