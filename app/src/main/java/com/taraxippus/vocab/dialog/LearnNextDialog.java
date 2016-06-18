package com.taraxippus.vocab.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.vocabulary.Vocabulary;
import java.util.ArrayList;
import com.taraxippus.vocab.vocabulary.DBHelper;
import com.taraxippus.vocab.vocabulary.SortType;
import com.taraxippus.vocab.vocabulary.ShowType;
import com.taraxippus.vocab.util.StringHelper;

public class LearnNextDialog extends DialogFragment
{
	TextView text_preview;
	ScrollView scroll_preview;
	View divider_scroll_top, divider_scroll_bottom;
	Spinner spinner_which;
	EditText text_edit_count;
	
	ArrayList<Integer> vocabularies, vocabularies_filtered;
	DBHelper dbHelper;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		dbHelper = new DBHelper(getContext());

		vocabularies = dbHelper.getVocabularies(SortType.TIME_ADDED, ShowType.UNLEARNED, null);
		
		if (preferences.getInt("showType", 0) != ShowType.LEARNED.ordinal())
			vocabularies_filtered = dbHelper.getVocabularies(SortType.values()[preferences.getInt("sortType", 0)], ShowType.UNLEARNED, StringHelper.toBooleanArray(preferences.getString("show", "")));
		else
			vocabularies_filtered = new ArrayList<>();
			
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
		alertDialog.setTitle("Learn next vocabularies");
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_learn, null);

		spinner_which = (Spinner) v.findViewById(R.id.spinner_which);
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, new String[] {"Oldest", "Filtered"});
		text_edit_count = (EditText) v.findViewById(R.id.text_edit_count);
		final Button plus = (Button) v.findViewById(R.id.button_plus);
		final Button minus = (Button) v.findViewById(R.id.button_minus);
		text_preview = (TextView) v.findViewById(R.id.text_preview);
		scroll_preview = (ScrollView) v.findViewById(R.id.scroll_preview);
		divider_scroll_top = v.findViewById(R.id.divider_scroll_top);
		divider_scroll_bottom = v.findViewById(R.id.divider_scroll_bottom);

		scroll_preview.setOnScrollChangeListener(
			new View.OnScrollChangeListener()
			{
				@Override
				public void onScrollChange(View p1, int p2, int p3, int p4, int p5)
				{
					if (scroll_preview.canScrollVertically(1)) 
						divider_scroll_bottom.setVisibility(View.VISIBLE);
					else
						divider_scroll_bottom.setVisibility(View.INVISIBLE);

					if (scroll_preview.canScrollVertically(-1)) 
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

		text_edit_count.setText("" + Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("learnAddCount", "10")));
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
					ArrayList<Integer> vocabularies_learn = spinner_which.getSelectedItemPosition() == 0 ? vocabularies : vocabularies_filtered;
					
					for (int i = 0; i < vocabularies_learn.size() && i < count; ++i)
						dbHelper.updateVocabularyLearned(vocabularies_learn.get(i), true);
						

					preferences.edit().putString("learnAddCount", "" + count).apply();
				}
			});

		alertDialog.setView(v);
		return alertDialog.create();
	}
	
	public void updatePreview()
	{
		StringBuilder sb = new StringBuilder();

		ArrayList<Integer> vocabularies_learn = spinner_which.getSelectedItemPosition() == 0 ? vocabularies : vocabularies_filtered;

		int count = Integer.parseInt(text_edit_count.getText().toString());
		for (int i = 0; i < count; ++i)
		{
			if (i >= vocabularies_learn.size())
			{
				sb.append(" - - - ");
				break;
			}
			
			if (i > 0)
				sb.append("\n");
				
			sb.append(dbHelper.getKanji(vocabularies_learn.get(i)));
		}

		text_preview.setText(sb.toString());

		if (scroll_preview.canScrollVertically(1)) 
			divider_scroll_bottom.setVisibility(View.VISIBLE);
		else
			divider_scroll_bottom.setVisibility(View.INVISIBLE);

		if (scroll_preview.canScrollVertically(-1)) 
			divider_scroll_top.setVisibility(View.VISIBLE);
		else
			divider_scroll_top.setVisibility(View.INVISIBLE);
	}
}
