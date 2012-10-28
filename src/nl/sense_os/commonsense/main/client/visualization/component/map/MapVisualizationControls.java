package nl.sense_os.commonsense.main.client.visualization.component.map;

import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.model.Timeseries;
import nl.sense_os.commonsense.main.client.visualization.component.map.resource.MapResources;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SliderEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

public class MapVisualizationControls extends Composite {

    /**
     * Timer class for animation
     */
    private class AnimationTimer extends Timer {

        @Override
        public void run() {
            stepAnimationSlider();
        }
    }

    private static final Logger LOG = Logger.getLogger(MapVisualizationControls.class.getName());
    private boolean animationMode = false;
    private ToggleButton modeSelectionButton;
    private Widget animationControls;
    private ToggleButton playPauseButton;
    private PushButton rewindButton;
    /**
     * Slider for controlling the animation time
     */
    private DateSlider animationSlider;
    private AnimationTimer animationTimer;
    private Widget displayControls;
    /**
     * Slider for controlling the max time of the displayed traces
     */
    private DateSlider displayEndSlider;
    /**
     * Slider for controlling the start time of the displayed traces
     */
    private DateSlider displayStartSlider;
    private MapPanel mapPanel;
    private long sliderMax;
    private long sliderMin;
    private long sliderAnimation;
    private FlowPanel panel;
    private Label timeLabel;

    public MapVisualizationControls(MapPanel mapPanel) {
        this.mapPanel = mapPanel;

        panel = new FlowPanel();

        modeSelectionButton = createModeSelectionButton();
        panel.add(modeSelectionButton);

        displayControls = createDisplayControls();
        displayControls.setVisible(false);
        panel.add(displayControls);

        animationControls = createAnimationControls();
        panel.add(animationControls);

        initWidget(panel);
    }

    /**
     * Finds the maximum and the minimum values in a set of location timeseries
     * 
     * @param dataset
     */
    private void calcSliderRange(Map<String, LocationData> dataset) {

        // find maximum and minimum timestamps
        long minTimestamp = Long.MAX_VALUE;
        long maxTimestamp = Long.MIN_VALUE;
        for (LocationData locationData : dataset.values()) {
            Timeseries latitudePoints = locationData.getLatitudes();

            long localMin = latitudePoints.getStart();
            if (localMin < minTimestamp) {
                minTimestamp = localMin;
            }

            long localMax = latitudePoints.getEnd();
            if (localMax > maxTimestamp) {
                maxTimestamp = localMax;
            }
        }

        // set display start slider range
        int minValue = (int) (minTimestamp / 1000);
        int maxValue = (int) (maxTimestamp / 1000);
        int increment = (maxValue - minValue) / 100;
        displayStartSlider.setMinValue(minValue);
        displayStartSlider.setMaxValue(maxValue);
        displayStartSlider.setIncrement(increment);
        displayStartSlider.setValue(minValue, true);

        // set display end slider range
        displayEndSlider.setMinValue(minValue);
        displayEndSlider.setMaxValue(maxValue);
        displayEndSlider.setIncrement(increment);
        displayEndSlider.setValue(maxValue, true);

        // set anymation slider range
        int playInterval = (maxValue - minValue) / 500;
        animationSlider.setMinValue(minValue);
        animationSlider.setMaxValue(maxValue);
        animationSlider.setIncrement(playInterval);
        animationSlider.setValue(minValue, true);

        timeLabel.setText(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT).format(
                new Date(minTimestamp)));

        // save the slider ranges in memory
        sliderAnimation = minTimestamp;
        sliderMin = minTimestamp;
        sliderMax = maxTimestamp;
    }

    /**
     * Stops and removes the animation timers
     */
    private void cancelAnimationTimer() {
        LOG.fine("Cancel animation");
        if (animationTimer != null) {
            animationTimer.cancel();
            animationTimer = null;
        }
    }

    private FlowPanel createAnimationControls() {

        FlowPanel panel = new FlowPanel();

        animationSlider = createAnimationSlider();
        panel.add(animationSlider);

        playPauseButton = createPlayPauseButton();
        panel.add(playPauseButton);

        rewindButton = createRewindButton();
        rewindButton.setVisible(false);
        panel.add(rewindButton);

        timeLabel = new Label();
        timeLabel.addStyleName("animationTime");
        panel.add(timeLabel);

        return panel;
    }

    private DateSlider createAnimationSlider() {
        Listener<SliderEvent> slideListener = new Listener<SliderEvent>() {

            @Override
            public void handleEvent(SliderEvent be) {
                if (animationMode == true) {

                    // update the map
                    mapPanel.setAnimationTime(animationSlider.getValue() * 1000);

                    // pause animation
                    onPlayPause(false);
                }
            }
        };

        DateSlider slider = new DateSlider();
        slider.setMessage("{0}");
        slider.addListener(Events.Change, slideListener);
        slider.addStyleName("playSlider");
        slider.setValue(slider.getMinValue() - 100000);

        return slider;
    }

    /**
     * Create a set of sliders on the bottom, to filter the points to draw according to a time
     * specified with the sliders. Add the Animate panel.
     */
    private Grid createDisplayControls() {

        Grid grid = new Grid(2, 2);

        Listener<SliderEvent> slideListener = new Listener<SliderEvent>() {

            @Override
            public void handleEvent(SliderEvent be) {
                if (displayStartSlider.equals(be.getSource())) {
                    sliderMin = displayStartSlider.getValue() * 1000l;
                    mapPanel.setDisplayStart(sliderMin);
                } else if (displayEndSlider.equals(be.getSource())) {
                    sliderMax = displayEndSlider.getValue() * 1000l;
                    mapPanel.setDisplayEnd(sliderMax);
                } else {
                    LOG.warning("Unexpected slide event!");
                }
            }
        };

        grid.setWidget(0, 0, new Label("Trace start:"));
        grid.getCellFormatter().setWidth(0, 0, "10%");

        // slider for display start time
        displayStartSlider = new DateSlider();
        displayStartSlider.setMessage("{0}");
        displayStartSlider.addListener(Events.Change, slideListener);
        grid.setWidget(0, 1, displayStartSlider);
        grid.getCellFormatter().setWidth(0, 1, "90%");

        grid.setWidget(1, 0, new Label("Trace end:"));
        grid.getCellFormatter().setWidth(1, 0, "10%");

        // slider for display end time
        displayEndSlider = new DateSlider();
        displayEndSlider.setMessage("{0}");
        displayEndSlider.addListener(Events.Change, slideListener);
        grid.setWidget(1, 1, displayEndSlider);
        grid.getCellFormatter().setWidth(1, 1, "90%");

        grid.setWidth("100%");

        sliderAnimation = sliderMin;

        return grid;
    }

    private ToggleButton createModeSelectionButton() {

        final ToggleButton button = new ToggleButton("Show trace length controls",
                "Show animation controls");
        button.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                setAnimationMode(!button.isDown());
            }
        });
        button.addStyleName("animationButton");
        return button;
    }

    private ToggleButton createPlayPauseButton() {

        Image playImage = new Image(MapResources.INSTANCE.iconPlay());
        Image pauseImage = new Image(MapResources.INSTANCE.iconPause());

        final ToggleButton button = new ToggleButton(playImage, pauseImage);
        button.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                onPlayPause(button.isDown());
            }
        });

        button.addStyleName("animationButton");

        return button;
    }

    private PushButton createRewindButton() {
        Image rewindImage = new Image(MapResources.INSTANCE.iconRewind());

        PushButton button = new PushButton(rewindImage, new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                animationSlider.setValue((int) ((sliderMin / 1000) - 100000));
                sliderAnimation = animationSlider.getValue() * 1000l;
                onPlayPause(true);
            }
        });
        button.addStyleName("animationButton");
        return button;
    }

    private void onPlayPause(boolean play) {
        if (!play) {
            cancelAnimationTimer();
        } else {
            // update UI
            rewindButton.setVisible(false);
            playPauseButton.setVisible(true);
            playPauseButton.setDown(true);

            // start stepping in time
            cancelAnimationTimer();
            sliderAnimation = animationSlider.getValue() * 1000l;
            startAnimationTimer();
        }
    }

    private void setAnimationMode(boolean enable) {
        LOG.fine((enable ? "Enable" : "Disable") + " animation mode");

        mapPanel.setAnimationMode(enable);
        displayControls.setVisible(!enable);
        animationControls.setVisible(enable);

        if (enable) {
            animationMode = true;
            cancelAnimationTimer();

            animationSlider.setMinValue((int) (sliderMin / 1000l));
            animationSlider.setMaxValue((int) (sliderMax / 1000l));
            sliderAnimation = sliderMin;
            animationSlider.setValue(animationSlider.getMinValue());

            playPauseButton.setVisible(true);
            playPauseButton.setDown(false);
            rewindButton.setVisible(false);

        } else {
            animationMode = false;
            cancelAnimationTimer();

            displayStartSlider.setValue((int) (sliderMin / 1000l));
            displayEndSlider.setValue((int) (sliderMax / 1000l));
            displayStartSlider.setWidth("95%");
            displayEndSlider.setWidth("95%");
        }
    }

    public void setLocatonDataSet(Map<String, LocationData> dataset) {
        calcSliderRange(dataset);
        LOG.fine("Display start slider value: " + displayStartSlider.getValue()
                + ", end slider value: " + displayEndSlider.getValue() + ", sliders min: "
                + (sliderMin / 1000l) + ", max: " + (sliderMax / 1000l));
    }

    /**
     * Create animation timers for each trace
     */
    private void startAnimationTimer() {
        LOG.fine("Start animation");
        animationTimer = new AnimationTimer();
        animationTimer.scheduleRepeating(10);
    }

    private void stepAnimationSlider() {
        LOG.finest("Update animation slider");

        long step = (sliderMax - sliderMin) / 500;
        DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);

        if (sliderAnimation + step < sliderMax) {
            // step animation slider
            sliderAnimation = sliderAnimation + step;
            animationSlider.setValue((int) (sliderAnimation / 1000l));
            timeLabel.setText(format.format(new Date(sliderAnimation)));

        } else {
            // stop the animation slider at the max
            sliderAnimation = sliderMax;
            animationSlider.setValue((int) (sliderMax / 1000l));
            timeLabel.setText(format.format(new Date(sliderAnimation)));

            cancelAnimationTimer();

            playPauseButton.setVisible(false);
            rewindButton.setVisible(true);
        }

        mapPanel.setAnimationTime(sliderAnimation);
    }
}
