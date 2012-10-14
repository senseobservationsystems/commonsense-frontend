package nl.sense_os.commonsense.main.client.visualization.component.map;

import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.sense_os.commonsense.common.client.model.Timeseries;
import nl.sense_os.commonsense.main.client.visualization.component.map.resource.MapResources;
import nl.sense_os.commonsense.main.client.viz.panels.map.DateSlider;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SliderEvent;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SliderField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;

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
	private boolean animationPaused = false;
	private ToggleButton modeSelectionButton;
	private SliderField animationSliderField;
	/**
	 * Slider for controlling the animation time
	 */
	private DateSlider animationSlider;
	private AnimationTimer animationTimer;
	private SliderField displayEndField;
	/**
	 * Slider for controlling the max time of the displayed traces
	 */
	private DateSlider displayEndSlider;
	private SliderField displayStartField;
	/**
	 * Slider for controlling the start time of the displayed traces
	 */
	private DateSlider displayStartSlider;
	private MapPanel mapPanel;
	private ToggleButton playPauseButton;
	private PushButton rewindButton;
	private FlowPanel animationButtonPanel;
	private long sliderMax;
	private long sliderMin;
	private FormPanel panel;
	private long sliderValue;
	private Label timeLabel;

	public MapVisualizationControls(MapPanel mapPanel) {

		LOG.setLevel(Level.FINE);

		this.mapPanel = mapPanel;

		animationSlider = createAnimationSlider();

		playPauseButton = createPlayPauseButton();
		rewindButton = createReplayButton();
		modeSelectionButton = createModeSelectionButton();
		timeLabel = new Label();
		timeLabel.setStyleName("timeLabel");

		panel = createControlPanel();

		initComponent(panel);
	}

	/**
	 * Finds the maximum and the minimum values in a set of location timeseries
	 * 
	 * @param dataset
	 */
	private void calcSliderRange(Map<Integer, LocationData> dataset) {

		// find maximum and minimum timestamps
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (LocationData locationData : dataset.values()) {
			Timeseries latitudePoints = locationData.getLatitude();

			int localMin = (int) Math.floor(latitudePoints.getStart() / 1000l);
			if (localMin < min) {
				min = localMin;
			}

			int localMax = (int) Math.ceil(latitudePoints.getEnd() / 1000l);
			if (localMax > max) {
				max = localMax;
			}
		}

		// set display start slider range
		int interval = (max - min) / 100;
		displayStartSlider.setMinValue(min);
		displayStartSlider.setMaxValue(max);
		displayStartSlider.setIncrement(interval);
		displayStartSlider.disableEvents(true);
		// if you set the value to min, the slider starts with the second value
		// for some reason; so i set it to min - 100000, then it equals min anyway
		displayStartSlider.setValue(min);
		displayStartSlider.enableEvents(true);

		// set display end slider range
		displayEndSlider.setMinValue(min);
		displayEndSlider.setMaxValue(max);
		displayEndSlider.setIncrement(interval);
		displayEndSlider.disableEvents(true);
		// if you set the value to max, the slider starts with the second value
		// for some reason; so i add something to the max, then it equals max anyway
		displayEndSlider.setValue(max);
		displayEndSlider.enableEvents(true);

		// set anymation slider range
		int playInterval = (max - min) / 500;
		animationSlider.setMinValue(min);
		animationSlider.setMaxValue(max);
		animationSlider.setIncrement(playInterval);
		animationSlider.disableEvents(true);
		animationSlider.setValue(min);
		animationSlider.enableEvents(true);

		timeLabel.setText(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT).format(
				new Date(min * 1000l)));

		// save the slider ranges in memory
		sliderMin = min * 1000l;
		sliderMax = max * 1000l;
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

	private ToggleButton createModeSelectionButton() {

		final ToggleButton button = new ToggleButton("Show static mode", "Show animation");
		button.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				setAnimationMode(!button.isDown());
			}
		});
		return button;
	}

	private DateSlider createAnimationSlider() {
		Listener<SliderEvent> slideListener = new Listener<SliderEvent>() {

			@Override
			public void handleEvent(SliderEvent be) {
				if (animationPaused == true && animationMode == true) {

					// update the map
					mapPanel.setAnimationTime(animationSlider.getValue() * 1000);

					// show the play button
					if (rewindButton.isVisible()) {
						playPauseButton.setVisible(true);
						rewindButton.setVisible(false);
					}
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
	private FormPanel createControlPanel() {

		FormPanel slidersForm = new FormPanel();
		slidersForm.setHeaderVisible(false);
		slidersForm.setBorders(false);
		slidersForm.setBodyBorder(false);
		slidersForm.setPadding(0);

		Listener<SliderEvent> slideListener = new Listener<SliderEvent>() {

			@Override
			public void handleEvent(SliderEvent be) {
				if (displayStartSlider.equals(be.getSource())) {
					mapPanel.setDisplayStart(displayStartSlider.getValue() * 1000l);
				} else if (displayEndSlider.equals(be.getSource())) {
					mapPanel.setDisplayEnd(displayEndSlider.getValue() * 1000l);
				} else {
					LOG.warning("Unexpected slide event!");
				}
			}
		};

		// slider for display start time
		displayStartSlider = new DateSlider();
		displayStartSlider.setMessage("{0}");
		displayStartSlider.setId("viz-map-startSlider");
		displayStartSlider.addListener(Events.Change, slideListener);

		displayStartField = new SliderField(displayStartSlider);
		displayStartField.setFieldLabel("Trace start");

		// slider for display end time
		displayEndSlider = new DateSlider();
		displayEndSlider.setMessage("{0}");
		displayEndSlider.setValue(displayEndSlider.getMaxValue());
		displayEndSlider.setId("viz-map-endSlider");
		displayEndSlider.addListener(Events.Change, slideListener);

		displayEndField = new SliderField(displayEndSlider);
		displayEndField.setFieldLabel("Trace end");

		// slider for animation time
		animationSliderField = new SliderField(animationSlider);
		animationSliderField.setHideLabel(true);

		animationButtonPanel = new FlowPanel();
		animationButtonPanel.addStyleName("playPanel");

		VerticalPanel modeSelectionPanel = new VerticalPanel();
		modeSelectionPanel.add(modeSelectionButton);

		// ADD THE animate PANEL HERE!
		slidersForm.add(modeSelectionPanel);
		animationButtonPanel.add(playPauseButton);
		animationButtonPanel.add(rewindButton);
		animationButtonPanel.add(timeLabel);

		slidersForm.add(animationButtonPanel, new FormData("-5"));
		slidersForm.add(animationSliderField, new FormData("-5"));

		displayEndSlider.setValue(displayEndSlider.getMaxValue() + 100000);
		animationSlider.setValue(animationSlider.getMinValue() - 100000);

		sliderValue = sliderMin;

		return slidersForm;
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

		button.addStyleName("mediaButton");

		return button;
	}

	private void onPlayPause(boolean play) {
		if (!play) {
			cancelAnimationTimer();
		} else {
			cancelAnimationTimer();
			sliderValue = animationSlider.getValue() * 1000l;
			startAnimationTimer();
		}
		animationPaused = !play;
	}

	private PushButton createReplayButton() {
		Image replayImage = new Image(MapResources.INSTANCE.iconRewind());

		PushButton button = new PushButton(replayImage, new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				animationPaused = false;
				rewindButton.setVisible(false);
				cancelAnimationTimer();

				animationSlider.setValue(animationSlider.getMinValue() - 100000);
				sliderValue = animationSlider.getValue() * 1000l;

				playPauseButton.setVisible(true);
				playPauseButton.setDown(true);
				onPlayPause(true);
			}
		});

		button.addStyleName("mediaButton");
		button.setVisible(false);

		return button;
	}

	private void setAnimationMode(boolean enable) {
		LOG.fine((enable ? "Enable" : "Disable") + " animation mode");
		if (enable) {
			animationMode = true;
			animationPaused = true;

			animationSlider.setMinValue(displayStartSlider.getValue());
			animationSlider.setMaxValue(displayEndSlider.getValue());
			sliderMin = displayStartSlider.getValue() * 1000l;
			sliderMax = displayEndSlider.getValue() * 1000l;
			sliderValue = displayStartSlider.getValue() * 1000l;
			animationSlider.setValue(displayStartSlider.getValue());

			panel.remove(displayStartField);
			panel.remove(displayEndField);

			panel.add(animationButtonPanel, new FormData("-5"));
			panel.add(animationSliderField, new FormData("-5"));
			playPauseButton.setVisible(true);
			rewindButton.setVisible(false);

			mapPanel.setAnimationMode(true);

			cancelAnimationTimer();

			panel.layout();

		} else {
			animationMode = false;
			cancelAnimationTimer();

			mapPanel.setAnimationMode(false);

			animationSlider.setValue(animationSlider.getMinValue());
			panel.remove(animationButtonPanel);
			panel.remove(animationSliderField);
			panel.add(displayStartField, new FormData("-5"));
			panel.add(displayEndField, new FormData("-5"));

			panel.layout();

			displayStartSlider.setValue(displayStartSlider.getMinValue());
			displayEndSlider.setValue(displayEndSlider.getMaxValue());
		}
	}

	public void setLocatonDataSet(Map<Integer, LocationData> dataset) {
		calcSliderRange(dataset);
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

		long minim = sliderMin;
		long maxim = sliderMax;
		long step = (maxim - minim) / 500;
		DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);

		if (sliderValue + step < maxim) {
			// step animation slider
			sliderValue = sliderValue + step;
			animationSlider.setValue((int) (sliderValue / 1000l));
			timeLabel.setText(format.format(new Date(sliderValue)));

		} else {
			// stop the animation slider at the max
			sliderValue = sliderMax;
			animationSlider.setValue((int) (sliderMax / 1000l + 100000));
			timeLabel.setText(format.format(new Date(sliderValue)));

			cancelAnimationTimer();

			playPauseButton.setVisible(false);
			rewindButton.setVisible(true);
		}

		mapPanel.setAnimationTime(sliderValue);
	}
}
