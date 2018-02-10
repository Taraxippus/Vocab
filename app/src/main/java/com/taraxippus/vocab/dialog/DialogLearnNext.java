package com.taraxippus.vocab.dialog;

import android.app.*;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.support.v7.widget.*;
import android.transition.*;
import android.view.*;
import android.widget.*;
import com.taraxippus.vocab.*;
import com.taraxippus.vocab.fragment.*;
import com.taraxippus.vocab.util.*;
import com.taraxippus.vocab.vocabulary.*;
import java.util.*;

public class DialogLearnNext extends DialogFragment
{
	RecyclerView recycler_preview;
	View divider_scroll_top, divider_scroll_bottom;
	Spinner spinner_which;
	EditText text_edit_count;
	
	int[] vocabularies, vocabularies_filtered;
	char[] kanji, kanji_filtered;
	DBHelper dbHelper;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		dbHelper = new DBHelper(getContext());

		if (getArguments() == null || !getArguments().getBoolean("kanji", false))
		{
			vocabularies = dbHelper.getVocabularies(SortType.TIME_ADDED, ShowType.UNLEARNED, null, false, null);

			if (preferences.getInt("showType", 0) != ShowType.LEARNED.ordinal())
				vocabularies_filtered = dbHelper.getVocabularies(SortType.values()[preferences.getInt("sortType", 0)], ShowType.UNLEARNED, StringHelper.toBooleanArray(preferences.getString("show", "")), preferences.getBoolean("sortReversed", false), null);
			else
				vocabularies_filtered = new int[0];
		}
		else
		{
			kanji = dbHelper.getKanji(SortType.TIME_ADDED, ShowType.UNLEARNED, false, null);

			if (preferences.getInt("showTypeKanji", 0) != ShowType.LEARNED.ordinal())
				kanji_filtered = dbHelper.getKanji(SortType.values()[preferences.getInt("sortTypeKanji", 0)], ShowType.UNLEARNED, preferences.getBoolean("sortReversedKanji", false), null);
			else
				kanji_filtered = new char[0];
		}
		
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
		alertDialog.setTitle(vocabularies == null ? "Learn next kanji" : "Learn Next Vocabularies");
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_learn, null);

		spinner_which = (Spinner) v.findViewById(R.id.spinner_which);
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, new String[] {"Oldest", "Filtered"});
		text_edit_count = (EditText) v.findViewById(R.id.text_edit_count);
		final Button plus = (Button) v.findViewById(R.id.button_plus);
		final Button minus = (Button) v.findViewById(R.id.button_minus);
		divider_scroll_top = v.findViewById(R.id.divider_scroll_top);
		divider_scroll_bottom = v.findViewById(R.id.divider_scroll_bottom);
		
		recycler_preview = (RecyclerView)v.findViewById(R.id.recycler_preview);
		recycler_preview.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
		recycler_preview.setAdapter(vocabularies == null ? new FragmentDetail.KanjiAdapter(getActivity(), dbHelper, recycler_preview, true, true, null) : new FragmentDetail.VocabularyAdapter(getActivity(), dbHelper, recycler_preview, true, true, null));
		
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

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_which.setAdapter(adapter);
		spinner_which.setOnItemSelectedListener(new Spinner.OnItemSelectedListener()
			{
				@Override
				public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4)
				{
					updatePreview();
				}

				@Override
				public void onNothingSelected(AdapterView<?> p1)
				{

				}
			});

		text_edit_count.setText("" + Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(vocabularies == null ? "learnAddCountKanji" : "learnAddCount", "10")));
		text_edit_count.setOnEditorActionListener(new EditText.OnEditorActionListener()
			{
				@Override
				public boolean onEditorAction(TextView p1, int p2, KeyEvent p3)
				{
					updatePreview();
					return true;
				}
			});

		plus.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					text_edit_count.setText("" + (Integer.parseInt(text_edit_count.getText().toString()) + 1));

					updatePreview();
				}
			});
		minus.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					int count = Integer.parseInt(text_edit_count.getText().toString());

					if (count > 1)
						text_edit_count.setText("" + (count - 1));

					updatePreview();
				}
			});

		updatePreview();

		alertDialog.setNegativeButton("Cancel", new AlertDialog.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					p1.cancel();
				}
			});
		alertDialog.setPositiveButton("Learn", new AlertDialog.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					int count = Integer.parseInt(text_edit_count.getText().toString());
					
					if (vocabularies == null)
					{
						char[] kanji_learn = ((FragmentDetail.KanjiAdapter) recycler_preview.getAdapter()).data;
						
						for (int i = 0; i < kanji_learn.length && i < count; ++i)
							dbHelper.updateKanjiLearned(kanji_learn[i], true);
					}
					else
					{
						int[] vocabularies_learn = ((FragmentDetail.VocabularyAdapter) recycler_preview.getAdapter()).data ;

						for (int i = 0; i < vocabularies_learn.length && i < count; ++i)
							dbHelper.updateVocabularyLearned(vocabularies_learn[i], true);
					}
					
					getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));
					preferences.edit().putLong(vocabularies == null ? "kanjiChanged" : "vocabulariesChangef", System.currentTimeMillis()).apply();
					preferences.edit().putString(vocabularies == null ? "learnAddCountKanji" : "learmAddCount", "" + count).apply();
					
					getContext().startActivity(new Intent(getContext(), ActivityMain.class).setAction(ActivityMain.ACTION_QUIZ));
				}
			});

		alertDialog.setView(v);
		
		return alertDialog.create();
	}
	
	public void updatePreview()
	{
		int count = Integer.parseInt(text_edit_count.getText().toString());
		
		if (vocabularies == null)
		{
			char[] kanji_learn = spinner_which.getSelectedItemPosition() == 0 ? kanji : kanji_filtered;
			char[] data = new char[Math.min(count, kanji_learn.length)];
			System.arraycopy(kanji_learn, 0, data, 0, data.length);
			((FragmentDetail.KanjiAdapter) recycler_preview.getAdapter()).data = data;
		}
		else
		{
			int[] vocabularies_learn = spinner_which.getSelectedItemPosition() == 0 ? vocabularies : vocabularies_filtered;
			int[] data = new int[Math.min(count, vocabularies_learn.length)];
			System.arraycopy(vocabularies_learn, 0, data, 0, data.length);
			((FragmentDetail.VocabularyAdapter) recycler_preview.getAdapter()).data = data;
		}
		
		TransitionManager.beginDelayedTransition(recycler_preview);
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

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		dbHelper.close();
	}
}
