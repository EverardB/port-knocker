/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.htcl.android.common.checkboxlist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

/**
 *
 * @author everard
 */
public class CheckboxListView extends ListView {

    private ArrayAdapter<CheckboxListRow> listAdapter;

    CheckboxListView(Context context) {
        super(context);
    }

    CheckboxListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    CheckboxListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initialise(Context context, List<CheckboxListRow> listItems) {
        // When item is tapped, toggle 'selected' property of CheckBox and text.
        setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
                CheckboxListRow row = listAdapter.getItem(position);
                row.toggleSelected();
                SelectViewHolder viewHolder = (SelectViewHolder) item.getTag();
                viewHolder.getCheckBox().setChecked(row.isSelected());
            }
        });

        // When item has long click, display the context menu
        setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });

        // Set our custom array adapter as the ListView's adapter.
        listAdapter = new SelectArrayAdapter(context, listItems);

        setAdapter(listAdapter);
    }

}
