/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.htcl.android.common.checkboxlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import eu.htcl.android.portknocker.R;

/**
 *
 * Custom adapter for displaying/updating an array of CheckboxListRow objects.
 *
 * @author everard
 */
public class SelectArrayAdapter extends ArrayAdapter<CheckboxListRow> {

    private LayoutInflater inflater;

    public SelectArrayAdapter(Context context, List<CheckboxListRow> list) {
        super(context, R.layout.checkbox_list_row, R.id.rowText, list);

        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // item to display
        CheckboxListRow checkboxListRow = (CheckboxListRow) this.getItem(position);

        // The child views in each row.
        TextView textView;
        CheckBox checkBox;

        // Create a new row view
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.checkbox_list_row, null);

            // Find the child views.
            textView = (TextView) convertView.findViewById(R.id.rowText);
            checkBox = (CheckBox) convertView.findViewById(R.id.selectedCheckBox);

            // Tag the row with it's child views, so we don't have to
            // call findViewById() later when we reuse the row.
            SelectViewHolder viewHolder = new SelectViewHolder(textView, checkBox);
            convertView.setTag(viewHolder);
            checkboxListRow.setViewHolder(viewHolder);

            // If View is tapped, update the checkboxListRow it is associated (tagged) with.
            //convertView.setOnClickListener(new View.OnClickListener() {
            //    public void onClick(View v) {
            //        SelectViewHolder viewHolder = (SelectViewHolder) v.getTag();
            //        CheckboxListRow checkboxListRow = (CheckboxListRow) viewHolder.getCheckBox().getTag();
            //        checkboxListRow.toggleSelected();
            //        viewHolder.getCheckBox().setChecked(checkboxListRow.isSelected());
            //    }
            //});

            // If TextView is tapped, update the checkboxListRow it is associated (tagged) with.
            //textView.setOnClickListener(new View.OnClickListener() {
            //    public void onClick(View v) {
            //        TextView textView = (TextView) v;
            //        CheckboxListRow checkboxListRow = (CheckboxListRow) textView.getTag();
            //        SelectViewHolder viewHolder = checkboxListRow.getViewHolder();
            //        checkboxListRow.toggleSelected();
            //        viewHolder.getCheckBox().setChecked(checkboxListRow.isSelected());
            //    }
            //});

            // If CheckBox is toggled, update the checkboxListRow it is associated (tagged) with.
            checkBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox checkBox = (CheckBox) v;
                    CheckboxListRow checkboxListRow = (CheckboxListRow) checkBox.getTag();
                    checkboxListRow.setSelected(checkBox.isChecked());
                }
            });
        } else {
            // Reuse existing row view

            // By using a ViewHolder, we avoid having to call findViewById().
            SelectViewHolder viewHolder = (SelectViewHolder) convertView.getTag();
            textView = viewHolder.getTextView();
            checkBox = viewHolder.getCheckBox();
        }

        // Associate (tag) the CheckBox/TextView with the data they are displaying, so that we can
        // access the checkboxListRow in onClick() when the CheckBox/TextView is toggled/tapped.
        textView.setTag(checkboxListRow);
        checkBox.setTag(checkboxListRow);

        // Display checkboxListRow data
        textView.setText(checkboxListRow.toString());
        checkBox.setChecked(checkboxListRow.isSelected());

        return convertView;
    }
}
