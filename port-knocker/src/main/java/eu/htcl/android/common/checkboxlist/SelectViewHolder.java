/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.htcl.android.common.checkboxlist;

import android.widget.CheckBox;
import android.widget.TextView;

/**
 *
 * Holds child views for one row.
 *
 * @author everard
 */
public class SelectViewHolder {

    private CheckBox checkBox;
    private TextView textView;

    public SelectViewHolder() {
    }

    public SelectViewHolder(TextView textView, CheckBox checkBox) {
        this.checkBox = checkBox;
        this.textView = textView;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    public void setCheckBox(CheckBox checkBox) {
        this.checkBox = checkBox;
    }

    public TextView getTextView() {
        return textView;
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }
}
