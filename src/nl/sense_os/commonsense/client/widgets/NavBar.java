package nl.sense_os.commonsense.client.widgets;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Hyperlink;

import nl.sense_os.commonsense.dto.UserModel;

public class NavBar extends LayoutContainer implements ValueChangeHandler<String> {

    public static final String HELP = "help";
    public static final String HOME = "home";
    public static final String SETTINGS = "settings";
    public static final String SHARE_DATA = "share";
    public static final String SIGN_IN = "signin";
    public static final String SIGN_OUT = "signout";
    public static final String TRAINING_DATA = "train";
    public static final String VISUALIZATION = "viz";
    private final LayoutContainer container;
    private Hyperlink current = this.home;
    private final Hyperlink help = new Hyperlink("help", HELP);
    private final Hyperlink home = new Hyperlink("home", HOME);
    private boolean isLoggedIn;
    private final Hyperlink login = new Hyperlink("sign in", SIGN_IN);
    private final Hyperlink logout = new Hyperlink("sign out", SIGN_OUT);
    private final Margins margins = new Margins(5);
    private final Hyperlink settings = new Hyperlink("settings", SETTINGS);
    private final Hyperlink share = new Hyperlink("share data", SHARE_DATA);
    private final Text stretch = new Text();
    private final Hyperlink train = new Hyperlink("training data", TRAINING_DATA);
    private final Text userName = new Text();
    private final Hyperlink viz = new Hyperlink("visualizations", VISUALIZATION);
    
    public NavBar() {       
        
        this.userName.setStyleAttribute("font-weight", "bold");
        this.userName.setStyleAttribute("text-align", "right"); 
        
        this.current = home;
        this.isLoggedIn = false;
                
        // container for the horizontal box layout
        final HBoxLayout boxLayout = new HBoxLayout();  
        boxLayout.setPadding(new Padding(3, 5, 0, 5));  
        boxLayout.setHBoxLayoutAlign(HBoxLayoutAlign.TOP);  
        this.container = new LayoutContainer(new HBoxLayout());
        this.container.setLayout(boxLayout);  
        
        setLayout(new FitLayout());
        add(this.container);
        
        History.addValueChangeHandler(this);
        
        relayout();
    }
    
    @Override
    public void onValueChange(ValueChangeEvent<String> event) {
        final String token = event.getValue();
        
        // reset style of previously selected navigation label 
        this.current.setStyleName("sense-nav-deselected");
        
        // set new navigation label selected
        if (token.equals(HOME)) {
            this.current = this.home;
        } else if (token.equals(VISUALIZATION)) {
            this.current = this.viz;
        } else if (token.equals(SHARE_DATA)) {
            this.current = this.share;
        } else if (token.equals(TRAINING_DATA)) {
            this.current = this.train;
        } else if (token.equals(SETTINGS)) {
            this.current = this.settings;
        } else if (token.equals(HELP)) {
            this.current = this.help;
        } else if (token.equals(SIGN_OUT)) {
            this.current = this.logout;
        } else if (token.equals(SIGN_IN)) {
            this.current = this.login;
        }        
        this.current.setStyleName("sense-nav-selected");
        
        relayout();
    }

    private void relayout() {        
        
        this.container.removeAll(); 
        
        final HBoxLayoutData boxData = new HBoxLayoutData(this.margins);
        final HBoxLayoutData flexData = new HBoxLayoutData(this.margins);  
        flexData.setFlex(1);  

        if (this.isLoggedIn) {
            this.container.add(this.home, boxData);
            this.container.add(this.viz, boxData);
            this.container.add(this.share, boxData);
            this.container.add(this.train, boxData);
            this.container.add(this.stretch, flexData);
            this.container.add(this.userName, boxData);
            this.container.add(this.settings, boxData);
            this.container.add(this.help, boxData);
            this.container.add(this.logout, boxData);
        } else {
            this.container.add(this.home, boxData);
            this.container.add(this.stretch, flexData);
            this.container.add(this.help, boxData);
            this.container.add(this.login, boxData);
        }
    }

    /**
     * Shows or hides some navigation options based on login status
     */
    public void setLogin(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
        relayout();
    }

    /**
     * Changes the displayed user name
     */
    public void setUser(UserModel user) {
        if (null != user) {
            this.userName.setText(user.getName());
        } else {
            // should never be visible
            this.userName.setText("NULL");
        }
    }
}
