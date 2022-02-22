package com.nernar.android.wrapped;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

public class WrappedListActivity extends WrappedLifecycleActivity {
	
	public WrappedListActivity() {
		super();
	}
	
	@Override
    public void onContentChanged() {
		super.onContentChanged();
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4) {
				onListItemClick((ListView) p1, p2, p3, p4);
			}
		});
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {}
	
    public void setListAdapter(ListAdapter adapter) {
		getListView().setAdapter(adapter);
	}
	
    public void setSelection(int position) {
		getListView().setSelection(position);
	}
	
    public int getSelectedItemPosition() {
		return getListView().getSelectedItemPosition();
	}
	
    public long getSelectedItemId() {
		return getListView().getSelectedItemId();
	}
	
    public ListView getListView() {
		return findViewById(android.R.id.list);
	}
	
    public ListAdapter getListAdapter() {
		return getListView().getAdapter();
	}
}
