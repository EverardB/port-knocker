/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.htcl.android.common.checkboxlist;

/**
 *
 * Row data encapsulation
 *
 * @author everard
 */
public class CheckboxListRow {

    protected int id = 0;
    protected String name = "";
    protected boolean selected = false;

    public CheckboxListRow() {
    }

    public CheckboxListRow(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public CheckboxListRow(int id, String name, boolean selected) {
        this.id = id;
        this.name = name;
        this.selected = selected;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void toggleSelected() {
        setSelected( !isSelected() );
    }

    @Override
    public String toString() {
        return name;
    }
}
