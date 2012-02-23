package nl.sense_os.commonsense.client.alerts.create.components;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.client.alerts.create.triggers.NumericTrigger;
import nl.sense_os.commonsense.client.common.components.WizardFormPanel;
import nl.sense_os.commonsense.client.viz.data.timeseries.DataPoint;
import nl.sense_os.commonsense.client.viz.data.timeseries.Timeseries;

import com.chap.links.client.Graph;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.KeyCodes;

public class CopyOfNumTriggerForm extends WizardFormPanel {

    /**
     * A class to create and distinguish between min, max etc. graph lines *
     */
    private class GraphLine {
        private String name;
        private boolean active;
        private double value;

        public GraphLine(String name) {
            this.name = name;
            this.active = false;
        }

        public String getName() {
            return this.name;
        }

        public double getValue() {
            return this.value;
        }

        public boolean isActive() {
            return this.active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

    private static final Logger LOG = Logger.getLogger(CopyOfNumTriggerForm.class.getName());

    private Radio radioMax;
    private Radio radioMin;
    private Radio radioInRange;
    private Radio radioOutRange;

    private TextField<String> txtfldMax;
    private TextField<String> txtfldMin;
    private TextField<String> txtfldInRangeMin;
    private TextField<String> txtfldInRangeMax;
    private TextField<String> txtfldOutRangeMin;
    private TextField<String> txtfldOutRangeMax;

    // private JsArray<Timeseries> datan3;
    private JsArray<Timeseries> data;
    private long start;
    private long end;
    private double vstart;
    private double vend;
    private double orig_vstart;
    private double orig_vend;
    private Date startDate;

    private Date endDate;
    private Graph.Options graphOpts = Graph.Options.create();
    private GraphLine maxLine;
    private GraphLine minLine;
    private GraphLine insideRangeLine1;
    private GraphLine insideRangeLine2;
    private GraphLine outsideRangeLine1;
    private GraphLine outsideRangeLine2;

    private com.extjs.gxt.ui.client.widget.HorizontalPanel threshMaxPanel;
    private com.extjs.gxt.ui.client.widget.HorizontalPanel threshMinPanel;
    private com.extjs.gxt.ui.client.widget.HorizontalPanel rangeInPanel;
    private com.extjs.gxt.ui.client.widget.HorizontalPanel rangeOutPanel;

    private ArrayList<GraphLine> graphLinesList;

    public CopyOfNumTriggerForm() {
        super();
        setSize("450px", "350px");
        LOG.setLevel(Level.ALL);

        createGraph();
        createControls();
    }

    /**
     * Adds radio button listeners
     */
    private void addRadioListeners() {

        radioMax.addListener(Events.OnClick, new Listener<BaseEvent>() {

            @SuppressWarnings("unchecked")
            @Override
            public void handleEvent(BaseEvent be) {

                if (radioMax.getValue() == true) {
                    setEnabled(txtfldMax);
                    removeLines();
                }
            }
        });

        radioMin.addListener(Events.OnClick, new Listener<BaseEvent>() {

            @SuppressWarnings("unchecked")
            @Override
            public void handleEvent(BaseEvent be) {

                if (radioMin.getValue() == true) {
                    setEnabled(txtfldMin);
                    removeLines();
                }
            }
        });

        radioInRange.addListener(Events.OnClick, new Listener<BaseEvent>() {

            @SuppressWarnings("unchecked")
            @Override
            public void handleEvent(BaseEvent be) {

                if (radioInRange.getValue() == true) {
                    setEnabled(txtfldInRangeMin, txtfldInRangeMax);
                    removeLines();
                }
            }
        });

        radioOutRange.addListener(Events.OnClick, new Listener<BaseEvent>() {

            @SuppressWarnings("unchecked")
            @Override
            public void handleEvent(BaseEvent be) {

                if (radioOutRange.getValue() == true) {
                    setEnabled(txtfldOutRangeMin, txtfldOutRangeMax);
                    removeLines();
                }
            }
        });
    }

    private void addTextListeners() {
        txtfldMax.addKeyListener(new KeyListener() {
            public void componentKeyDown(ComponentEvent event) {
                Double value = getFieldValue(event);
                if (value != null) {
                    drawThresholdLine(value, maxLine);
                }
            }
        });

        txtfldMin.addKeyListener(new KeyListener() {
            public void componentKeyDown(ComponentEvent event) {
                Double value = getFieldValue(event);
                if (value != null) {
                    drawThresholdLine(value, minLine);
                }
            }
        });

        txtfldInRangeMin.addKeyListener(new KeyListener() {
            public void componentKeyDown(ComponentEvent event) {
                Double value = getFieldValue(event);
                String text2 = "";
                text2 = txtfldInRangeMax.getValue();

                if (isNumber(text2)) {
                    double value2 = getValue(text2);
                    if (value != null) {
                        drawThresholdLines(value, value2, insideRangeLine1, insideRangeLine2);
                    }
                } else if (value != null)
                    drawThresholdLine(value, insideRangeLine1);
            }
        });

        txtfldInRangeMax.addKeyListener(new KeyListener() {
            public void componentKeyDown(ComponentEvent event) {
                Double value = getFieldValue(event);
                String text2 = txtfldInRangeMin.getRawValue();
                if (isNumber(text2)) {
                    double value2 = getValue(text2);
                    if (value != null) {
                        drawThresholdLines(value, value2, insideRangeLine2, insideRangeLine1);
                    }
                } else if (value != null)
                    drawThresholdLine(value, insideRangeLine2);
            }
        });

        txtfldOutRangeMin.addKeyListener(new KeyListener() {
            public void componentKeyDown(ComponentEvent event) {
                Double value = getFieldValue(event);
                String text2 = txtfldOutRangeMax.getRawValue();

                if (isNumber(text2)) {
                    double value2 = getValue(text2);
                    if (value != null) {
                        drawThresholdLines(value, value2, outsideRangeLine1, outsideRangeLine2);
                    }
                } else if (value != null)
                    drawThresholdLine(value, outsideRangeLine1);
            }
        });

        txtfldOutRangeMax.addKeyListener(new KeyListener() {
            public void componentKeyDown(ComponentEvent event) {
                Double value = getFieldValue(event);
                String text2 = txtfldOutRangeMin.getRawValue();
                if (isNumber(text2)) {
                    double value2 = getValue(text2);
                    if (value != null) {
                        drawThresholdLines(value, value2, outsideRangeLine2, outsideRangeLine1);
                    }
                } else if (value != null)
                    drawThresholdLine(value, outsideRangeLine2);
            }
        });
    }

    /**
     * Creates threshold graph lines
     */
    private void createLines() {
        maxLine = new GraphLine("maxLine");
        minLine = new GraphLine("minLine");
        insideRangeLine1 = new GraphLine("insideRangeLine1");
        insideRangeLine2 = new GraphLine("insideRangeLine2");
        outsideRangeLine1 = new GraphLine("outsideRangeLine1");
        outsideRangeLine2 = new GraphLine("outsideRangeLine2");
        graphLinesList = new ArrayList<GraphLine>();
    }

    /**
     * Creates radio buttons to select trigger type.
     */
    private void createRadios() {
    }

    /**
     * Creates text fields for input of the limits for the trigger.
     */
    private void createTextFields() {
    }

    /**
     * Draws a threshold line at a specified value
     * 
     * @param value
     * @param line
     */
    private void drawThresholdLine(double value, GraphLine line) {

        removeLine(value, line);

        double newStart = getVerticalStart(value);
        double newEnd = getVerticalEnd(value);
        graphOpts.setVerticalStart(newStart);
        graphOpts.setVerticalEnd(newEnd);

        insertLine(value, line);
    }

    /**
     * Draws two threshold line at specified value range
     * 
     * @param value
     * @param line
     */
    private void drawThresholdLines(double value1, double value2, GraphLine line1, GraphLine line2) {

        removeLines();

        double newStart = getVerticalStart(value1);
        double newEnd = getVerticalEnd(value1);
        double newStart1 = getVerticalStart(value2);
        double newEnd1 = getVerticalEnd(value2);

        if (newStart1 < newStart)
            graphOpts.setVerticalStart(newStart1);
        else
            graphOpts.setVerticalStart(newStart);
        if (newEnd1 > newEnd)
            graphOpts.setVerticalEnd(newEnd1);
        else
            graphOpts.setVerticalEnd(newEnd);

        insertLine(value1, line1);
        insertLine(value2, line2);
    }

    private Double getFieldValue(ComponentEvent event) {
        String text = null;
        Double value = null;

        if (keyEnterOrTab(event)) {
            @SuppressWarnings("unchecked")
            TextField<String> sender = (TextField<String>) event.getSource();
            text = sender.getRawValue();

            if (isNumber(text)) {
                value = getValue(text);
                // LOG.fine ("Got text " + text + " got value " + value);
            }
        }
        // LOG.fine ("Got text " + text);
        return value;
    }

    public NumericTrigger getNumericTrigger() {
        NumericTrigger thresh = new NumericTrigger();
        ArrayList<Double> valueList = new ArrayList<Double>();
        String type = "";

        if (radioMax.getValue() == true) {

            if (!maxLine.isActive()) {
                LOG.fine("Oops no max line drawn");
                return null;
            } else {
                type = "max";
                double val = maxLine.getValue();
                valueList.add(val);
            }
        } else if (radioMin.getValue() == true) {
            if (!minLine.isActive()) {
                LOG.fine("Oops no min line drawn");
                return null;
            } else {
                type = "min";
                double val = minLine.getValue();
                valueList.add(val);
            }
        } else if (radioInRange.getValue() == true) {
            if (!insideRangeLine1.isActive() || !insideRangeLine2.isActive()) {
                LOG.fine("Oops one of the inside range lines is missing");
                return null;
            } else {
                type = "inside";
                double val1 = insideRangeLine1.getValue();
                double val2 = insideRangeLine2.getValue();
                valueList.add(val1);
                valueList.add(val2);
            }
        } else if (radioOutRange.getValue() == true) {
            if (!outsideRangeLine1.isActive() || !outsideRangeLine2.isActive()) {
                LOG.fine("Oops one of the outside range lines is missing");
                return null;
            } else {
                type = "outside";
                double val1 = outsideRangeLine1.getValue();
                double val2 = outsideRangeLine2.getValue();
                valueList.add(val1);
                valueList.add(val2);
            }
        }

        thresh.setType(type);
        thresh.setValues(valueList);

        return thresh;
    }

    /**
     * Gets the numeric value from a string
     * 
     * @param text
     * @return
     */

    public double getValue(String text) {
        double value = Double.parseDouble(text);
        // LOG.fine ("The value is " + value);
        return value;
    }

    private double getVerticalEnd(double value) {

        double newEnd = orig_vend;

        if (value > orig_vend) {
            double vertRange = value - orig_vstart;
            newEnd = value + 0.05 * vertRange;
        }

        // LOG.fine ("Setting end at " + newEnd);
        return newEnd;
    }

    private double getVerticalStart(double value) {

        double newStart = orig_vstart;
        if (value < orig_vstart) {
            // LOG.fine ("Vstart is " + vstart + " and the value is " + value);
            double vertRange = orig_vend - value;
            newStart = value - 0.05 * vertRange;
        }
        // LOG.fine ("Setting start at " + newStart);
        return newStart;
    }

    /**
     * Create a set of controls on the bottom
     */
    private void createControls() {

        createRadios();
        createTextFields();
        addTextListeners();
        addRadioListeners();
        createLines();

        threshMaxPanel = new com.extjs.gxt.ui.client.widget.HorizontalPanel();
        add(threshMaxPanel);
        threshMaxPanel.setVerticalAlign(VerticalAlignment.MIDDLE);
        radioMax = new Radio();
        threshMaxPanel.add(radioMax);
        radioMax.setName("limit");
        radioMax.setBoxLabel("");
        radioMax.setValue(true);
        txtfldMax = new TextField<String>();
        txtfldMax.setFieldLabel("Max threshold");
        threshMaxPanel.add(txtfldMax);
        txtfldMax.setAllowBlank(false);

        threshMinPanel = new com.extjs.gxt.ui.client.widget.HorizontalPanel();
        add(threshMinPanel);
        threshMinPanel.setVerticalAlign(VerticalAlignment.MIDDLE);

        radioMin = new Radio();
        threshMinPanel.add(radioMin);
        radioMin.setName("limit");
        radioMin.setBoxLabel("Min threshold");

        txtfldMin = new TextField<String>();
        threshMinPanel.add(txtfldMin);
        txtfldMin.setEnabled(false);

        rangeInPanel = new com.extjs.gxt.ui.client.widget.HorizontalPanel();
        add(rangeInPanel);
        rangeInPanel.setVerticalAlign(VerticalAlignment.MIDDLE);

        radioInRange = new Radio();
        rangeInPanel.add(radioInRange);
        radioInRange.setName("limit");
        radioInRange.setBoxLabel("Inside safe range");

        txtfldInRangeMin = new TextField<String>();
        rangeInPanel.add(txtfldInRangeMin);
        txtfldInRangeMin.setEnabled(false);

        txtfldInRangeMax = new TextField<String>();
        rangeInPanel.add(txtfldInRangeMax);
        txtfldInRangeMax.setEnabled(false);

        rangeOutPanel = new com.extjs.gxt.ui.client.widget.HorizontalPanel();
        add(rangeOutPanel);
        rangeOutPanel.setVerticalAlign(VerticalAlignment.MIDDLE);

        radioOutRange = new Radio();
        rangeOutPanel.add(radioOutRange);
        radioOutRange.setName("limit");
        radioOutRange.setBoxLabel("Outside safe range");

        txtfldOutRangeMin = new TextField<String>();
        rangeOutPanel.add(txtfldOutRangeMin);
        txtfldOutRangeMin.setEnabled(false);

        txtfldOutRangeMax = new TextField<String>();
        rangeOutPanel.add(txtfldOutRangeMax);
        txtfldOutRangeMax.setEnabled(false);
        layout();
    }

    /**
     * Initializes a graph panel.
     */
    private void createGraph() {

        graphOpts.setLegendVisibility(false);
        graphOpts.setHeight(250);
        // graphOpts.setHeight("100%");
        graphOpts.setWidth("100%");
        setLayout(new ColumnLayout());
    }

    /**
     * Draws a horizontal line on the graph at a specified value
     * 
     * @param value
     * @param line
     */
    private void insertLine(double value, GraphLine line) {

        String newString = " {" + " \"label\" : \"Activity\"," + " \"data\" : [" + " {\"date\": "
                + start + ", \"value\" : " + value + "}," + " {\"date\": " + end + ", \"value\" : "
                + value + "}" + " ]" + " }";

        Timeseries datan0 = JsonUtils.safeEval(newString);
        data.push(datan0);

        graphOpts.setLineWidth(3, data.length() - 1);
        graphOpts.setLineColor("Blue", data.length() - 1);
        // TODO alertGraph.draw(data, graphOpts);

        line.setActive(true);
        line.setValue(value);
    }

    /**
     * Determines if a string contains a numeric value
     * 
     * @param s
     * @return
     */
    private boolean isNumber(String s) {
        try {
            Double.parseDouble(s);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * Determines if the key down is an enter or tab
     * 
     * @param event
     * @return
     */
    private boolean keyEnterOrTab(ComponentEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyCodes.KEY_ENTER || keyCode == KeyCodes.KEY_TAB) {
            return true;
        }

        return false;
    }

    public void passData(JsArray<Timeseries> data) {
        int length = data.length();
        this.data = data;
        LOG.fine("Data length passData is " + length);
        start = Long.MAX_VALUE;
        end = 0;
        vstart = Double.MAX_VALUE;
        vend = -Double.MAX_VALUE;

        for (int i = 0; i < length; i++) {

            Timeseries ts = data.get(i);

            JsArray<DataPoint> data1 = ts.getData();

            startDate = data1.get(0).getTimestamp();
            endDate = data1.get(data1.length() - 1).getTimestamp();

            for (int j = 0; j < data1.length(); j++) {
                long timestamp = (long) (data1.get(j).getTime());
                if (timestamp < start)
                    start = timestamp;
                if (timestamp > end)
                    end = timestamp;
                // LOG.fine ("Raw date is " + timestamp);

            }
        }

        graphOpts.setVerticalStart(vstart);
        graphOpts.setVerticalEnd(vend);
        graphOpts.setStart(startDate);
        graphOpts.setEnd(endDate);

        // LOG.fine ("Ts vstart is " + vstart + " Ts vend is " + vend);
        // LOG.fine ("Start is " + start + " end is " + end);

        // TODO alertGraph.draw(data, graphOpts);
        // LOG.fine ("Tried to draw graph");
    }

    /**
     * Removes a specified line from the graph
     * 
     * @param line
     */
    private void removeLine(double value, GraphLine line) {
        if (line.isActive()) {
            // data.pop();
            data.setLength(data.length() - 1);

            // LOG.fine ("Line " + line.getName() + " removed. Vstart is " + vstart + " vend is " +
            // vend);

            // TODO alertGraph.draw(data, graphOpts);
            line.setActive(false);

        }
    }

    /**
     * Removes all threshold graph lines
     */
    private void removeLines() {
        if (maxLine.isActive())
            removeLine(0, maxLine);
        if (minLine.isActive())
            removeLine(0, minLine);
        if (insideRangeLine1.isActive())
            removeLine(0, insideRangeLine1);
        if (insideRangeLine2.isActive())
            removeLine(0, insideRangeLine2);
        if (outsideRangeLine1.isActive())
            removeLine(0, outsideRangeLine1);
        if (outsideRangeLine2.isActive())
            removeLine(0, outsideRangeLine2);
        vend = orig_vend;
        vstart = orig_vstart;
        graphOpts.setVerticalStart(orig_vstart);
        graphOpts.setVerticalEnd(orig_vend);
        // TODO alertGraph.draw(data, graphOpts);

        graphLinesList.clear();
    }

    /**
     * Sets all TextBoxes to ReadOnly, EXCEPT the ones in the parameters
     * 
     * @param fields
     */
    private void setEnabled(TextField<String>... fields) {

        for (TextField<String> field : fields) {
            field.setEnabled(true);
            field.setAllowBlank(false);
        }

        // isValid();
    }
}
