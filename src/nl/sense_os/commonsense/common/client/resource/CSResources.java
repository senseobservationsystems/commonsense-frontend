package nl.sense_os.commonsense.common.client.resource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.TextResource;

public interface CSResources extends ClientBundle {

	public static final CSResources INSTANCE = GWT.create(CSResources.class);

	@Source("google.gif")
	ImageResource iconGoogle();

	@Source("loading.gif")
	ImageResource iconLoading();

	@Source("page-next.gif")
	ImageResource iconNext();

	@Source("logo_dev-header.png")
	ImageResource logoDev();

	@Source("logo_sense-header.png")
	ImageResource logoSense();

	@Source("logo_test-header.png")
	ImageResource logoTest();

	@Source("lastupdated.txt")
	TextResource lastUpdated();
}
