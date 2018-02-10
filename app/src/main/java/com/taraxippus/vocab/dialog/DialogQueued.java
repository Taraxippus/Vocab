package com.taraxippus.vocab.dialog;

import android.app.*;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.taraxippus.vocab.*;
import com.taraxippus.vocab.vocabulary.*;
import java.util.*;

import android.support.v7.widget.PopupMenu;

public class DialogQueued extends DialogFragment
{
	RecyclerView recycler_preview;
	DBHelper dbHelper;
	
	SharedPreferences preferences;
	String[] queue;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		dbHelper = new DBHelper(getContext());
		preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
	
		checkAndUpdateQueue();
		
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
		alertDialog.setTitle("Add queued Vocabularies");
		alertDialog.setPositiveButton("OK", new AlertDialog.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					p1.dismiss();
				}
			});
			
		if (queue.length == 0)
		{
			alertDialog.setMessage("There are no queued vocabularies");
			
			return alertDialog.create();
		}
		
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_queue, null);

		final View divider_scroll_bottom = v.findViewById(R.id.divider_scroll_bottom);
		final View divider_scroll_top = v.findViewById(R.id.divider_scroll_top);
		
		recycler_preview = (RecyclerView) v.findViewById(R.id.recycler_preview);
		recycler_preview.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
		recycler_preview.setAdapter(new VocabularyAdapter());
		
		recycler_preview.setOnScrollChangeListener(
			new View.OnScrollChangeListener()
			{
				@Override
				public void onScrollChange(View p1, int p2, int p3, int p4, int p5)
				{
					if (recycler_preview.canScrollVertically(1)) 
						divider_scroll_bottom.setVisibility(View.VISIBLE);
					else
						divider_scroll_bottom.setVisibility(View.INVISIBLE);
						
					if (recycler_preview.canScrollVertically(-1)) 
						divider_scroll_top.setVisibility(View.VISIBLE);
					else
						divider_scroll_top.setVisibility(View.INVISIBLE);
				}
			});

		alertDialog.setView(v);

		return alertDialog.create();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		
		checkAndUpdateQueue();
	}
	
	public void checkAndUpdateQueue()
	{
		Set<String> queue1 = preferences.getStringSet("addQueue", new HashSet<String>());
		
		boolean refresh = false;
		
		for (Iterator<String> i = queue1.iterator(); i.hasNext();)
			if (dbHelper.getId(i.next()) != -1)
			{
				refresh = true;
				i.remove();
			}
				
		queue = queue1.toArray(new String[queue1.size()]);
		
		if (refresh)
			preferences.edit().putStringSet("addQueue", queue1).commit();
		
		if (recycler_preview != null)
			recycler_preview.getAdapter().notifyDataSetChanged();
	}
	
	public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabularyViewHolder> implements View.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			getContext().startActivity(new Intent(getContext(), ActivityAdd.class).setAction(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, queue[recycler_preview.getChildAdapterPosition(v)]));
		}

		public class VocabularyViewHolder extends RecyclerView.ViewHolder 
		{
			final TextView text_kanji;

			public VocabularyViewHolder(final View v) 
			{
				super(v);

				text_kanji = (TextView) v.findViewById(R.id.text_kanji);
				text_kanji.setTextLocale(Locale.JAPANESE);

				v.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getContext().getResources().getDisplayMetrics());
				v.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

				text_kanji.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 84, getContext().getResources().getDisplayMetrics());
				text_kanji.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

				v.setOnLongClickListener(new View.OnLongClickListener()
					{
						@Override
						public boolean onLongClick(View p1)
						{
							PopupMenu popup = new PopupMenu(getContext(), v);
							popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
								{
									@Override
									public boolean onMenuItemClick(MenuItem item)
									{
										int index = recycler_preview.getChildAdapterPosition(v);
										
										Set<String> queue1 = new HashSet<>();
										for (int i = 0; i < queue.length; ++i)
											if (i != index)
												queue1.add(queue[i]);
										
										queue = queue1.toArray(new String[queue1.size()]);
										preferences.edit().putStringSet("addQueue", queue1).commit();
										
										notifyItemRemoved(index);

										return true;
									}
								});
							popup.getMenu().add("Remove");
							popup.show();
							return true;
						}
					});
			}
		}

		@Override
		public VocabularyAdapter.VocabularyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_synonym, parent, false);
			v.setOnClickListener(this);

			return new VocabularyViewHolder(v);
		}

		@Override
		public void onBindViewHolder(VocabularyViewHolder holder, int position) 
		{
			holder.text_kanji.setText(queue[position]);
		}

		@Override
		public int getItemCount() 
		{
			return queue.length;
		}
	}
}
