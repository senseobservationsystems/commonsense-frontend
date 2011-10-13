package nl.sense_os.commonsense.client.alerts.create.utils;

import com.google.gwt.user.client.ui.HorizontalPanel;

public class MyWidget extends HorizontalPanel {
    private String Id;
    private boolean equalFieldOrNot;

    public MyWidget() {
        super();
    }

    public void setId(String Id) {
        this.Id = Id;
    }

    public String getId() {
        return this.Id;
    }

    public void setEqual(boolean equal) {
        this.equalFieldOrNot = equal;
    }

    public boolean getEqual() {
        return this.equalFieldOrNot;
    }
}