/*
 * Ext GWT - Ext for GWT
 * Copyright(c) 2007, 2008, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
package nl.sense_os.testing.client.common.models;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeModel;

public class Music extends BaseTreeModel {

  public Music() {

  }

  public Music(String name) {
    set("name", name);
  }

  public Music(String name, String author, String genre) {
    set("name", name);
    set("author", author);
    set("genre", genre);
  }

  public String getName() {
    return (String) get("name");
  }

  public String getAuthor() {
    return (String) get("author");
  }

  public String getGenre() {
    return (String) get("genre");
  }

  public String toString() {
    return getName();
  }
}
