package com.taraxippus.vocab;

import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.ViewHolder>
{
	private static final int TYPE_HEADER = 0; 
	private static final int TYPE_ITEM = 1;

	public static class ViewHolder extends RecyclerView.ViewHolder 
	{
		int id;      

		TextView textView; 
		ImageView imageView;

		public ViewHolder(View itemView, int ViewType) 
		{                 
			super(itemView);

			if (ViewType == TYPE_ITEM) 
			{
				textView = (TextView) itemView.findViewById(R.id.rowText); 
				imageView = (ImageView) itemView.findViewById(R.id.rowIcon);
				id = 1;                                              
			}
			else
			{
				id = 0;                                                
			}
		}


	}
	
	MainActivity activity;

	public NavigationAdapter(MainActivity activity)
	{
		this.activity = activity;
	}
	
	@Override
	public NavigationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) 
	{
		if (viewType == TYPE_ITEM)
		{
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false);

			v.setOnClickListener(activity);
			
			v.setSelected(true);
			
			ViewHolder vhItem = new ViewHolder(v,viewType); 

			return vhItem; 

		}
		else if (viewType == TYPE_HEADER) 
		{

			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.header,parent,false); 

			ViewHolder vhHeader = new ViewHolder(v, viewType); 

			return vhHeader; 
		}

		return null;

	}

	@Override
	public void onBindViewHolder(NavigationAdapter.ViewHolder holder, int position)
	{
		if (holder.id == 1) 
		{                 
			holder.textView.setText(MainActivity.item_names[position - 1]); 
			holder.imageView.setImageResource(MainActivity.item_icons[position -1]);
			
			if (position == activity.selectedTap + 1)
			{
				holder.itemView.setBackgroundColor(0xFF_EEEEEE);
			}
			else
			{
				TypedValue outValue = new TypedValue();
				activity.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
				holder.itemView.setBackgroundResource(outValue.resourceId);
			}
		}
		else
		{
		
		}
	}

	@Override
	public int getItemCount()
	{
		return MainActivity.item_names.length + 1;
	}


	@Override
	public int getItemViewType(int position)
	{ 
		if (position == 0)
			return TYPE_HEADER;

		return TYPE_ITEM;
	}
}
