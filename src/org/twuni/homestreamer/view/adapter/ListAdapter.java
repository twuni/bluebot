package org.twuni.homestreamer.view.adapter;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ListAdapter<T> extends BaseAdapter {

	private final int layoutResourceID;
	private final List<T> list;

	public ListAdapter( int layoutResourceID, List<T> list ) {
		this.layoutResourceID = layoutResourceID;
		this.list = list;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem( int position ) {
		return list.get( position );
	}

	@Override
	public long getItemId( int position ) {
		return getItem( position ).hashCode();
	}

	@Override
	public View getView( int position, View convertView, ViewGroup parent ) {
		View view = convertView;
		if( view == null ) {
			view = View.inflate( parent.getContext(), layoutResourceID, null );
		}
		view.setTag( getItem( position ) );
		return view;
	}

}
