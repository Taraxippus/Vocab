package com.taraxippus.vocab.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.fragment.FragmentDetail;
import com.taraxippus.vocab.util.JishoHelper;
import com.taraxippus.vocab.util.NotificationHelper;
import com.taraxippus.vocab.util.OnProcessSuccessListener;
import com.taraxippus.vocab.util.StringHelper;
import com.taraxippus.vocab.vocabulary.DBHelper;
import com.taraxippus.vocab.vocabulary.ImportType;
import com.taraxippus.vocab.vocabulary.Kanji;
import com.taraxippus.vocab.vocabulary.Vocabulary;

public class DialogImportKanjiJisho extends DialogFragment
{
	DBHelper dbHelper;
	View.OnClickListener searchListener;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		dbHelper = new DBHelper(getContext());

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
		alertDialog.setTitle("Import Kanji from jisho.org");
		final View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_import_jisho, null);

		final View divider_scroll_top = v.findViewById(R.id.divider_scroll_top);
		final View divider_scroll_bottom = v.findViewById(R.id.divider_scroll_bottom);

		final RecyclerView recycler_preview = (RecyclerView)v.findViewById(R.id.recycler_preview);
		recycler_preview.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
		recycler_preview.setAdapter(new FragmentDetail.KanjiAdapter(getActivity(), dbHelper, recycler_preview, true, true, null));

		final Spinner spinner_import = (Spinner) v.findViewById(R.id.spinner_import);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, Vocabulary.types_import);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_import.setAdapter(adapter);
		spinner_import.setSelection(ImportType.ASK.ordinal());

		final CheckBox checkbox_learned = (CheckBox) v.findViewById(R.id.checkbox_learned);
		final TextView text_search = (TextView) v.findViewById(R.id.text_search);
		final TextView text_import = (TextView) v.findViewById(R.id.text_import);
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
		alertDialog.setNeutralButton("Import", new AlertDialog.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					if (((FragmentDetail.KanjiAdapter) recycler_preview.getAdapter()).data != null)
						new OnKanjiImported(getActivity(), dbHelper, ((FragmentDetail.KanjiAdapter) recycler_preview.getAdapter()).data, 0, ImportType.values()[spinner_import.getSelectedItemPosition()], 0).onProcessSuccess(Boolean.valueOf(false));
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

				text_import.setVisibility(View.VISIBLE);
				spinner_import.setVisibility(View.VISIBLE);
				text_results.setVisibility(View.VISIBLE);
				checkbox_learned.setVisibility(View.VISIBLE);
				recycler_preview.setVisibility(View.VISIBLE);

				char[] data = (char[]) args[0];
				text_results.setText(data == null ? "An error occured" : data.length == 1 ? "1 Result" : data.length + " Results");
				
				if (data != null)
				{
					((FragmentDetail.KanjiAdapter) recycler_preview.getAdapter()).data = data;
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
					DialogHelper.createDialog(getActivity(), "Import from jisho.org", "Please enter a search query to search for kanji on jisho.org");
					return;
				}
				
				TransitionManager.beginDelayedTransition((ViewGroup) v);
				progress_jisho.setVisibility(View.VISIBLE);
				text_search.setEnabled(false);
				dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

				JishoHelper.searchKanji(getActivity(), text_search.getText().toString(), listener);
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
	public void onDestroy()
	{
		super.onDestroy();

		dbHelper.close();
	}
	
	public static class OnKanjiImported implements OnProcessSuccessListener
	{
		final Context context;
		final DBHelper dbHelper;
		final char[] kanji;
		final int index;
		final ImportType importType;
		int count;
		
		public OnKanjiImported(Context context, DBHelper dbHelper, char[] kanji, int index, ImportType importType, int count)
		{
			this.context = context;
			this.dbHelper = dbHelper;
			this.kanji = kanji;
			this.index = index;
			this.importType = importType;
			this.count = count;
		}
		
		@Override
		public void onProcessSuccess(Object... args)
		{
			if ((Boolean) args[0])
				count++;
			
			if (index >= kanji.length)
			{
				PreferenceManager.getDefaultSharedPreferences(dbHelper.context).edit().putLong("kanjiChanged", System.currentTimeMillis()).apply();
				dbHelper.context.sendBroadcast(new Intent(dbHelper.context, NotificationHelper.class));

				dbHelper.close();
				DialogHelper.createDialog(dbHelper.context, "Import", "Imported " + count + " new kanji!");
			}
			else
			{
				final Kanji k = new Kanji(kanji[index]);
				k.vocabularies = dbHelper.findVocabulariesForKanji(dbHelper.getReadableDatabase(), k.kanji);
				JishoHelper.importKanji(context, k, new OnProcessSuccessListener()
					{
						@Override
						public void onProcessSuccess(Object... args)
						{
							dbHelper.updateKanji(k, importType, new OnKanjiImported(context, dbHelper, kanji, index + 1, importType, count));
						}
					});
			}
		}
	}
}
