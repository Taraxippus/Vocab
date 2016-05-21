package com.taraxippus.vocab;

import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.graphics.*;

public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.ViewHolder>
{
	private static final int TYPE_HEADER = 0; 
	private static final int TYPE_ITEM = 1;
	private static final int TYPE_DIVIDER = 2;
	
	public static class ViewHolder extends RecyclerView.ViewHolder 
	{
		TextView textView; 
		ImageView imageView;

		public ViewHolder(View itemView, int ViewType) 
		{                 
			super(itemView);

			if (ViewType == TYPE_ITEM) 
			{
				textView = (TextView) itemView.findViewById(R.id.rowText); 
				imageView = (ImageView) itemView.findViewById(R.id.rowIcon);                                        
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
			ViewHolder vhItem = new ViewHolder(v,viewType); 

			return vhItem; 
		}
		else if (viewType == TYPE_DIVIDER)
		{
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.divider, parent, false);
			ViewHolder vhDivider = new ViewHolder(v,viewType); 

			return vhDivider; 
		}
		else if (viewType == TYPE_HEADER) 
		{
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.header, parent, false); 
			ViewHolder vhHeader = new ViewHolder(v, viewType); 

			return vhHeader; 
		}

		return null;

	}

	@Override
	public void onBindViewHolder(NavigationAdapter.ViewHolder holder, int position)
	{
		if (getItemViewType(position) == TYPE_ITEM)
		{                 
			if (position == getItemCount() - 1)
			{
				position--;
			}
		
			holder.textView.setText(MainActivity.item_names[position - 1]); 
			holder.imageView.setImageResource(MainActivity.item_icons[position -1]);
			
			if (position == activity.selectedTap + 1)
			{
				holder.itemView.setBackgroundColor(activity.getResources().getColor(R.color.navigationSelected));
				holder.textView.setTextColor(activity.getResources().getColor(R.color.primary));
				holder.imageView.getDrawable().setColorFilter(activity.getResources().getColor(R.color.primary), PorterDuff.Mode.MULTIPLY);
			}
			else
			{
				TypedValue outValue = new TypedValue();
				activity.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
				holder.itemView.setBackgroundResource(outValue.resourceId);
				
				holder.textView.setTextColor(activity.getResources().getColor(R.color.navigationText));
				holder.imageView.getDrawable().setColorFilter(activity.getResources().getColor(R.color.navigationIcon), PorterDuff.Mode.MULTIPLY);
			}
		}
	}

	@Override
	public int getItemCount()
	{
		return MainActivity.item_names.length + 2;
	}


	@Override
	public int getItemViewType(int position)
	{ 
		if (position == 0)
			return TYPE_HEADER;

		if (position == MainActivity.item_names.length)
			return TYPE_DIVIDER;
			
		return TYPE_ITEM;
	}
}
