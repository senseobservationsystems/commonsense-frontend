package nl.sense_os.commonsense.client.common.grid;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.google.gwt.user.client.ui.Image;

/**
 * Panel to indicate loading activity, displays an animated gif, a title text and "Loading...". 
 */
public class LoadingPanel extends ContentPanel {

    private final Text title = new Text();
    
    public LoadingPanel() {
        
        // set up and format the text
        title.setStyleAttribute("font-weight", "bold");
        final Text loading = new Text("Loading...");        
        final LayoutContainer txtContainer = new LayoutContainer();
        VBoxLayout textLayout = new VBoxLayout();
        textLayout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);
        txtContainer.setLayout(textLayout);
        txtContainer.setSize(150, 30);
        txtContainer.add(title);
        txtContainer.add(loading);

        // 32x32 loading animation
        final Image img = new Image("/gxt/images/gxt/shared/large-loading.gif");
        img.setPixelSize(32, 32);

        // prepare panel to hold the image and text
        setHeaderVisible(false);
        setSize(200, 52);
        setBorders(true);
        
        // add image and text side by side to the panel
        HBoxLayout hLayout = new HBoxLayout();
        hLayout.setHBoxLayoutAlign(HBoxLayoutAlign.MIDDLE);
        hLayout.setPadding(new Padding(5));
        setLayout(hLayout);
        add(img, new HBoxLayoutData(new Margins(5)));
        add(txtContainer, new HBoxLayoutData(new Margins(5)));
    }
    
    @Override
    public void setTitle(String title) {
        super.setTitle("Loading...");
        this.title.setText(title);
    }
}
