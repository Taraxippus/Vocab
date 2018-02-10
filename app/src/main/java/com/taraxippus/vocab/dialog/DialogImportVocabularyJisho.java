package com.taraxippus.vocab.dialog;

import android.app.*;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.support.v7.widget.*;
import android.transition.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.taraxippus.vocab.*;
import com.taraxippus.vocab.fragment.*;
import com.taraxippus.vocab.util.*;
import com.taraxippus.vocab.vocabulary.*;
import java.util.*;

import android.widget.PopupMenu;
import android.widget.CompoundButton.*;

public class DialogImportVocabularyJisho extends DialogFragment
{
	DBHelper dbHelper;
	SharedPreferences preferences;
	View.OnClickListener searchListener;
	
	CheckBox checkbox_new;
	RecyclerView recycler_preview;
	
	ArrayList<String> vocabularies;
	ArrayList<String> vocabularies_new;
	
	String vocabularyClicked;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		dbHelper = new DBHelper(getContext());
		preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
		alertDialog.setTitle("Import Vocabularies from jisho.org");
		final View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_import_jisho, null);

		final View divider_scroll_top = v.findViewById(R.id.divider_scroll_top);
		final View divider_scroll_bottom = v.findViewById(R.id.divider_scroll_bottom);

		recycler_preview = (RecyclerView)v.findViewById(R.id.recycler_preview);
		recycler_preview.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
		recycler_preview.setAdapter(new VocabularyAdapter());
		
		checkbox_new = (CheckBox) v.findViewById(R.id.checkbox_learned);
		checkbox_new.setText("Show only new vocabularies");
		checkbox_new.setChecked(true);
		checkbox_new.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
				@Override
				public void onCheckedChanged(CompoundButton p1, boolean p2)
				{
					recycler_preview.getAdapter().notifyDataSetChanged();
				}
		});
		final TextView text_search = (TextView) v.findViewById(R.id.text_search);
		final TextView text_results = (TextView) v.findViewById(R.id.text_results);
		final View progress_jisho = v.findViewById(R.id.progress_jisho);

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

		alertDialog.setNegativeButton("Cancel", new AlertDialog.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					p1.cancel();
				}
			});
		alertDialog.setPositiveButton("Search", new AlertDialog.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface p1, int p2) {}
			});
		alertDialog.setNeutralButton("Queue", new AlertDialog.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					Set<String> queue = preferences.getStringSet("addQueue", new HashSet<String>());
					
					ArrayList<String> vocabularies = getVocabularies();
					for (int i = 0; i < vocabularies.size(); ++i)
						queue.add(vocabularies.get(i));

					preferences.edit().putStringSet("addQueue", queue).commit();
					
					Toast.makeText(getContext(), "Added " + (vocabularies.size() == 1 ? "1 Vocabulary" : vocabularies.size() + " Vocabularies") + " to queue", Toast.LENGTH_SHORT).show();
				}
			});

		alertDialog.setView(v);
		final AlertDialog dialog = alertDialog.create();

		final OnProcessSuccessListener listener = new OnProcessSuccessListener()
		{
			@Override
			public void onProcessSuccess(Object[] args)
			{
				TransitionManager.beginDelayedTransition((ViewGroup) v);

				progress_jisho.setVisibility(View.GONE);
				text_search.setEnabled(true);
				dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
				dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setEnabled(true);

				text_results.setVisibility(View.VISIBLE);
				recycler_preview.setVisibility(View.VISIBLE);
				checkbox_new.setVisibility(View.VISIBLE);
				
				ArrayList<String> data = (ArrayList<String>) args[0];
				if (data == null)
					text_results.setText("An error occured");

				if (data != null)
				{
					vocabularies = data;
					vocabularies_new = new ArrayList<>();
					for (String v : vocabularies)
						if (dbHelper.getId(v) == -1)
							vocabularies_new.add(v);
							
					text_results.setText(data.size() == 1 ? "1 Result" : data.size() + " Results" + (vocabularies_new.size() > 0 ? ", " + vocabularies_new.size() + " new" : ""));
					
					recycler_preview.getAdapter().notifyDataSetChanged();

					if (recycler_preview.canScrollVertically(1)) 
						divider_scroll_bottom.setVisibility(View.VISIBLE);
					else
						divider_scroll_bottom.setVisibility(View.INVISIBLE);

					if (recycler_preview.canScrollVertically(-1)) 
						divider_scroll_top.setVisibility(View.VISIBLE);
					else
						divider_scroll_top.setVisibility(View.INVISIBLE);
				}
			}
		};

		searchListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View p1)
			{
				if (StringHelper.trim(text_search.getText().toString()).isEmpty())
				{
					DialogHelper.createDialog(getActivity(), "Import from jisho.org", "Please enter a search query to search for vocabularies on jisho.org");
					return;
				}

				TransitionManager.beginDelayedTransition((ViewGroup) v);
				progress_jisho.setVisibility(View.VISIBLE);
				text_search.setEnabled(false);
				dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

				JishoHelper.searchVocabulary(getActivity(), text_search.getText().toString(), listener);
			}
		};

		return dialog;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		AlertDialog dialog = (AlertDialog) getDialog();
		if (dialog != null)
		{
			dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(searchListener);
			dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setEnabled(false);
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();

		if (vocabularyClicked != null)
		{
			if (dbHelper.getId(vocabularyClicked) != -1)
			{
				int index = vocabularies_new.indexOf(vocabularyClicked);
				if (recycler_preview != null && getVocabularies() == vocabularies_new)
					recycler_preview.getAdapter().notifyItemRemoved(index);
				vocabularies_new.remove(index);
			}
				
			vocabularyClicked = null;
		}
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();

		dbHelper.close();
	}
	
	private ArrayList<String> getVocabularies()
	{
		return checkbox_new.isChecked() ? vocabularies_new : vocabularies;
	}
	
	public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabularyViewHolder> implements View.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			vocabularyClicked = getVocabularies().get(recycler_preview.getChildAdapterPosition(v));
			int id = dbHelper.getId(vocabularyClicked);
			
			if (id == -1)
				getContext().startActivity(new Intent(getContext(), ActivityAdd.class).setAction(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, vocabularyClicked));
			
			else
			{
				getContext().startActivity(new Intent(getContext(), ActivityDetail.class).putExtra("id", id));
				vocabularyClicked = null;
			}
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
										String v = getVocabularies().get(index);
										
										vocabularies.remove(v);
										vocabularies_new.remove(v);
										
										if (item.getTitle().equals("Queue"))
										{
											Set<String> queue = preferences.getStringSet("addQueue", new HashSet<String>());
											if (!queue.contains(v))
												queue.add(v);
											preferences.edit().putStringSet("addQueue", queue).commit();
											
											Toast.makeText(getContext(), "Added " + v + " to queue", Toast.LENGTH_SHORT).show();
										}
										
										notifyItemRemoved(index);

										return true;
									}
								});
							popup.getMenu().add("Remove");
							popup.getMenu().add("Queue");
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
			holder.text_kanji.setText(getVocabularies().get(position));
		}

		@Override
		public int getItemCount() 
		{
			return getVocabularies() == null ? 0 : getVocabularies().size();
		}
	}
}
