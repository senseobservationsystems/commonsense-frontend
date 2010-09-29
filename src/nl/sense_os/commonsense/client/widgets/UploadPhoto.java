package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

import nl.sense_os.commonsense.client.services.UserImageService;
import nl.sense_os.commonsense.client.services.UserImageServiceAsync;
import nl.sense_os.commonsense.client.utility.Log;
import nl.sense_os.commonsense.dto.UploadedImage;

/**
 * @see <a href=
 *      "http://ikaisays.com/2010/09/08/gwt-blobstore-the-new-high-performance-image-serving-api-and-cute-dogs-on-office-chairs/"
 *      >blobstore image service</a>
 */
public class UploadPhoto extends LayoutContainer {

    private static final String TAG = "UploadPhoto";
    Button btn;
    final FormPanel panel = new FormPanel();

    UserImageServiceAsync userImageService = GWT.create(UserImageService.class);

    public UploadPhoto() {

        // Disable the button until we get the URL to POST to
        panel.setHeading("Image Upload Example");
        panel.setFrame(true);
        panel.setEncoding(Encoding.MULTIPART);
        panel.setMethod(Method.POST);
        panel.setButtonAlign(HorizontalAlignment.CENTER);
        panel.setWidth(350);

        FileUploadField file = new FileUploadField();
        file.setAllowBlank(false);
        file.setName("image");
        file.setFieldLabel("File");
        panel.add(file);

        btn = new Button("Loading...");
        btn.setEnabled(false);
        btn.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {

                if (!panel.isValid()) {
                    return;
                }

                panel.submit();
            }
        });
        panel.addButton(btn);

        // Now we use out GWT-RPC service and get an URL
        startNewBlobstoreSession();

        // Once we've hit submit and it's complete, let's set the form to a new session.
        // We could also have probably done this on the onClick handler
        panel.addListener(Events.Submit, new Listener<FormEvent>() {

            @Override
            public void handleEvent(FormEvent be) {
                Log.d("UploadPhoto", "Image located at: " + be.getResultHtml());

                refreshGallery();
            }

        });

        setLayout(new VBoxLayout());
        add(panel);

        refreshGallery();
    }

    private Image createImageWidget(final UploadedImage image) {
        final Image imageWidget = new Image();
        try {
            imageWidget.setUrl(image.getServingUrl() + "=s200");
        } catch (Exception e) {
            Log.d(TAG, "hoi");
        }
        final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);

        imageWidget.addMouseOverHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                Widget source = (Widget) event.getSource();
                int left = source.getAbsoluteLeft() + 10;
                int top = source.getAbsoluteTop() + source.getOffsetHeight() + 10;

                simplePopup.setWidth("150px");
                simplePopup.setWidget(new HTML("Uploaded: " + image.getCreatedAt()));
                simplePopup.show();
                simplePopup.setPopupPosition(left, top);
            }
        });

        imageWidget.addMouseOutHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                simplePopup.hide();
            }
        });

        imageWidget.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                final PopupPanel imagePopup = new PopupPanel(true);
                imagePopup.setAnimationEnabled(true);
                // imagePopup.setWidget(imageOverlay);
                imagePopup.setGlassEnabled(true);
                imagePopup.setAutoHideEnabled(true);

                imagePopup.center();
            }
        });

        return imageWidget;
    }

    public void refreshGallery() {
        for (int i = 1; i < this.getItemCount(); i++) {
            this.remove(this.getItem(i));
        }
        userImageService.getRecentlyUploaded(new AsyncCallback<List<UploadedImage>>() {

            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onSuccess(List<UploadedImage> images) {

                for (final UploadedImage image : images) {

                    Log.d(TAG, "OwnerId: " + image.getOwnerId());
                    UploadPhoto.this.add(new Image(image.getServingUrl()));
                }
                
                UploadPhoto.this.layout();

            }
        });
    }

    private void startNewBlobstoreSession() {
        userImageService.getBlobstoreUploadUrl(new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                // We probably want to do something here
            }

            @Override
            public void onSuccess(String result) {
                panel.setAction(result);
                btn.setText("Upload!");
                btn.setEnabled(true);
            }
        });
    }
}
