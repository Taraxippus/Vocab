package com.taraxippus.vocab.fragment;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import com.taraxippus.vocab.IVocabActivity;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.util.StringHelper;
import com.taraxippus.vocab.vocabulary.DBHelper;
import com.taraxippus.vocab.vocabulary.Vocabulary;

public class FragmentActivityLearnPreferences extends Fragment
{
	IVocabActivity vocabActivity;

	public FragmentActivityLearnPreferences() {}


    @Override
    public void onAttach(Activity activity)
	{
        super.onAttach(activity);

		try 
		{
            vocabActivity = (IVocabActivity) activity;
        }
		catch (ClassCastException e)
		{
            throw new ClassCastException(activity.toString() + " must implement IVocabActivity!");
        }
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.activity_learn_preferences, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState)
	{
		final int id = getArguments().getInt("id");
		final DBHelper dbHelper = vocabActivity.getDBHelper();
		
		int quiz_preferences = dbHelper.getInt(id, "quiz_preferences");
		final Spinner spinner_kanji = (Spinner) v.findViewById(R.id.spinner_kanji);
		final Spinner spinner_reading = (Spinner) v.findViewById(R.id.spinner_reading);
		final Spinner spinner_meaning = (Spinner) v.findViewById(R.id.spinner_meaning);
		final CheckBox checkbox_quickreview = (CheckBox)v.findViewById(R.id.checkbox_quickreview);
		
		Spinner.OnItemSelectedListener listener = new Spinner.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4)
			{
				dbHelper.updateVocabularyQuizPreferences(id, spinner_kanji.getSelectedItemPosition(), spinner_reading.getSelectedItemPosition(), spinner_meaning.getSelectedItemPosition(), checkbox_quickreview.isChecked());
			}

			@Override
			public void onNothingSelected(AdapterView<?> p1) {}
		};
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, Vocabulary.types_review);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_kanji.setAdapter(adapter);
		spinner_kanji.setSelection(quiz_preferences & 0b11);
		spinner_kanji.setOnItemSelectedListener(listener);
		
		spinner_reading.setAdapter(adapter);
		spinner_reading.setSelection((quiz_preferences & 0b1100) >> 2);
		spinner_reading.setOnItemSelectedListener(listener);
		
		if (StringHelper.lengthOfArray(dbHelper.getString(id, "reading")) == 0)
		{
			spinner_reading.setVisibility(View.GONE);
			v.findViewById(R.id.text_reading).setVisibility(View.GONE);
			((TextView) v.findViewById(R.id.text_kanji)).setText("Kana reviews:");
		}
		
		spinner_meaning.setAdapter(adapter);
		spinner_meaning.setSelection((quiz_preferences & 0b110000) >> 4);
		spinner_meaning.setOnItemSelectedListener(listener);
		
		checkbox_quickreview.setChecked((quiz_preferences & 0b1000000) != 0);
		checkbox_quickreview.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener()
		{
				@Override
				public void onCheckedChanged(CompoundButton p1, boolean p2)
				{
					dbHelper.updateVocabularyQuizPreferences(id, spinner_kanji.getSelectedItemPosition(), spinner_reading.getSelectedItemPosition(), spinner_meaning.getSelectedItemPosition(), checkbox_quickreview.isChecked());
				}
		});
	}
}
