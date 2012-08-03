package nl.sense_os.commonsense.client.alerts.create.components;

import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.component.WizardFormPanel;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.SpinnerField;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.i18n.client.NumberFormat;

public class NumTriggerForm extends WizardFormPanel {

    private static final Logger LOG = Logger.getLogger(NumTriggerForm.class.getName());

    private SpinnerField aboveThreshField;
    private SpinnerField belowThreshField;
    private SpinnerField inRangeMin;
    private SpinnerField inRangeMax;
    private SpinnerField outRangeMin;
    private SpinnerField outRangeMax;

    private Radio rdAboveThresh;
    private Radio rdBelowThresh;
    private Radio rdInsideRange;
    private Radio rdOutsideRange;

    private LayoutContainer inRangePanel;
    private LayoutContainer inRangeMinContainer;
    private LayoutContainer inRangeMaxContainer;
    private LayoutContainer outRangePanel;
    private LayoutContainer outRangeMinContainer;
    private LayoutContainer outRangeMaxContainer;
    private LayoutContainer belowThreshPanel;
    private LayoutContainer aboveThreshPanel;

    private RadioGroup radios;

    public NumTriggerForm() {
        super();

        // LOG.setLevel(Level.ALL);

        FormLayout formLayout = (FormLayout) getLayout();
        formLayout.setLabelAlign(LabelAlign.LEFT);

        createControls();
        addListeners();

        rdAboveThresh.setValue(true);
    }

    private void addListeners() {

        radios = new RadioGroup("numtriggertype");
        getRadios().add(rdAboveThresh);
        getRadios().add(rdBelowThresh);
        getRadios().add(rdInsideRange);
        getRadios().add(rdOutsideRange);
        getRadios().addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                LOG.fine("Radio selection changed");
                Radio selected = (Radio) be.getField().getValue();
                aboveThreshField.setEnabled(selected == rdAboveThresh);
                aboveThreshField.setAllowBlank(selected != rdAboveThresh);
                belowThreshField.setEnabled(selected == rdBelowThresh);
                belowThreshField.setAllowBlank(selected != rdBelowThresh);
                inRangeMin.setEnabled(selected == rdInsideRange);
                inRangeMin.setAllowBlank(selected != rdInsideRange);
                inRangeMax.setEnabled(selected == rdInsideRange);
                inRangeMax.setAllowBlank(selected != rdInsideRange);
                outRangeMin.setEnabled(selected == rdOutsideRange);
                outRangeMin.setAllowBlank(selected != rdOutsideRange);
                outRangeMax.setEnabled(selected == rdOutsideRange);
                outRangeMax.setAllowBlank(selected != rdOutsideRange);
            }
        });
    }

    /**
     * Create a set of controls on the bottom
     */
    private void createControls() {

        rdAboveThresh = new Radio();
        rdAboveThresh.setName("numtriggertype");
        rdAboveThresh.setBoxLabel("Above threshold:");
        rdAboveThresh.setHideLabel(true);

        rdBelowThresh = new Radio();
        rdBelowThresh.setName("numtriggertype");
        rdBelowThresh.setBoxLabel("Below threshold:");
        rdBelowThresh.setHideLabel(true);

        rdInsideRange = new Radio();
        rdInsideRange.setName("numtriggertype");
        rdInsideRange.setBoxLabel("Inside range:");
        rdInsideRange.setHideLabel(true);

        rdOutsideRange = new Radio();
        rdOutsideRange.setName("numtriggertype");
        rdOutsideRange.setBoxLabel("Outside range:");
        rdOutsideRange.setHideLabel(true);

        // above threshold
        add(rdAboveThresh, new FormData("100%"));
        aboveThreshPanel = new LayoutContainer();
        aboveThreshPanel.setLayout(new FormLayout());
        aboveThreshField = new SpinnerField();
        aboveThreshPanel.add(aboveThreshField, new FormData("-15"));
        aboveThreshField.setIncrement(.1d);
        aboveThreshField.getPropertyEditor().setType(Double.class);
        aboveThreshField.getPropertyEditor().setFormat(NumberFormat.getFormat("00.0"));
        aboveThreshField.setFieldLabel("Maximum value");
        aboveThreshField.setEnabled(false);
        FormData fd_aboveThreshPanel = new FormData("100%");
        fd_aboveThreshPanel.setMargins(new Margins(0, 0, 0, 18));
        add(aboveThreshPanel, fd_aboveThreshPanel);
        FormData fd_rdBelowThresh = new FormData("100%");
        fd_rdBelowThresh.setMargins(new Margins(10, 0, 0, 0));

        // below threshold layout
        add(rdBelowThresh, fd_rdBelowThresh);
        belowThreshPanel = new LayoutContainer();
        belowThreshPanel.setLayout(new FormLayout());
        belowThreshField = new SpinnerField();
        belowThreshPanel.add(belowThreshField, new FormData("-15"));
        belowThreshField.setIncrement(.1d);
        belowThreshField.getPropertyEditor().setType(Double.class);
        belowThreshField.getPropertyEditor().setFormat(NumberFormat.getFormat("00.0"));
        belowThreshField.setFieldLabel("Minimum value");
        belowThreshField.setEnabled(false);
        FormData fd_belowThreshPanel = new FormData("100%");
        fd_belowThreshPanel.setMargins(new Margins(0, 0, 0, 18));
        add(belowThreshPanel, fd_belowThreshPanel);
        FormData fd_rdInsideRange = new FormData("100%");
        fd_rdInsideRange.setMargins(new Margins(10, 0, 0, 0));

        // inside range layout
        add(rdInsideRange, fd_rdInsideRange);
        inRangePanel = new LayoutContainer(new FillLayout(Orientation.HORIZONTAL));
        inRangeMinContainer = new LayoutContainer();
        inRangeMinContainer.setLayout(new FormLayout());
        inRangeMin = new SpinnerField();
        inRangeMinContainer.add(inRangeMin, new FormData("-15"));
        inRangeMin.setIncrement(.1d);
        inRangeMin.getPropertyEditor().setType(Double.class);
        inRangeMin.getPropertyEditor().setFormat(NumberFormat.getFormat("00.0"));
        inRangeMin.setEnabled(false);
        inRangeMin.setFieldLabel("Range minimum");
        inRangePanel.add(inRangeMinContainer);
        inRangeMinContainer.setHeight("");
        inRangeMaxContainer = new LayoutContainer();
        inRangeMaxContainer.setLayout(new FormLayout());
        inRangeMax = new SpinnerField();
        inRangeMaxContainer.add(inRangeMax, new FormData("-15"));
        inRangeMax.setIncrement(.1d);
        inRangeMax.getPropertyEditor().setType(Double.class);
        inRangeMax.getPropertyEditor().setFormat(NumberFormat.getFormat("00.0"));
        inRangeMax.setEnabled(false);
        inRangeMax.setFieldLabel("Range maximum");

        // outside range layout
        inRangePanel.add(inRangeMaxContainer);
        FormData fd_inRangePanel = new FormData("\"0\"");
        fd_inRangePanel.setMargins(new Margins(0, 0, 0, 18));
        add(inRangePanel, fd_inRangePanel);
        inRangePanel.setHeight("24");
        FormData fd_rdOutsideRange = new FormData("100%");
        fd_rdOutsideRange.setMargins(new Margins(10, 0, 0, 0));
        add(rdOutsideRange, fd_rdOutsideRange);
        outRangePanel = new LayoutContainer();
        outRangePanel.setLayout(new FillLayout(Orientation.HORIZONTAL));
        outRangeMinContainer = new LayoutContainer();
        outRangeMinContainer.setLayout(new FormLayout());
        outRangeMin = new SpinnerField();
        outRangeMinContainer.add(outRangeMin, new FormData("-15"));
        outRangeMin.setIncrement(.1d);
        outRangeMin.getPropertyEditor().setType(Double.class);
        outRangeMin.getPropertyEditor().setFormat(NumberFormat.getFormat("00.0"));
        outRangeMin.setEnabled(false);
        outRangeMin.setFieldLabel("Range minimum");
        outRangePanel.add(outRangeMinContainer);
        outRangeMinContainer.setHeight("");
        outRangeMaxContainer = new LayoutContainer();
        outRangeMaxContainer.setLayout(new FormLayout());
        outRangeMax = new SpinnerField();
        outRangeMaxContainer.add(outRangeMax, new FormData("-15"));
        outRangeMax.setIncrement(.1d);
        outRangeMax.getPropertyEditor().setType(Double.class);
        outRangeMax.getPropertyEditor().setFormat(NumberFormat.getFormat("00.0"));
        outRangeMax.setEnabled(false);
        outRangeMax.setFieldLabel("Range maximum");
        outRangePanel.add(outRangeMaxContainer);
        FormData fd_outRangePanel = new FormData("\"0\"");
        fd_outRangePanel.setMargins(new Margins(0, 0, 0, 18));
        add(outRangePanel, fd_outRangePanel);
        outRangePanel.setHeight("24");
    }

    public SpinnerField getAboveThreshField() {
        return aboveThreshField;
    }

    public SpinnerField getBelowThreshField() {
        return belowThreshField;
    }

    public SpinnerField getInRangeMax() {
        return inRangeMax;
    }

    public SpinnerField getInRangeMin() {
        return inRangeMin;
    }

    public SpinnerField getOutRangeMax() {
        return outRangeMax;
    }

    public SpinnerField getOutRangeMin() {
        return outRangeMin;
    }

    public Radio getRdAboveThresh() {
        return rdAboveThresh;
    }

    public Radio getRdBelowThresh() {
        return rdBelowThresh;
    }

    public Radio getRdInsideRange() {
        return rdInsideRange;
    }

    public Radio getRdOutsideRange() {
        return rdOutsideRange;
    }

    public RadioGroup getRadios() {
        return radios;
    }
}
