package nl.sense_os.commonsense.main.client.visualization.component.map.resource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface MapResources extends ClientBundle {

	public static final MapResources INSTANCE = GWT.create(MapResources.class);

	@Source("play.png")
	ImageResource iconPlay();

	@Source("pause.png")
	ImageResource iconPause();

	@Source("rewind.png")
	ImageResource iconRewind();

	@Source("circle blue.png")
	ImageResource markerBlueDot();
}
