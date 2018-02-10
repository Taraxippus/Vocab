package com.taraxippus.vocab.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.util.StringHelper;
import com.taraxippus.vocab.vocabulary.DBHelper;
import com.taraxippus.vocab.vocabulary.Vocabulary;

public class DialogQuizPreferences extends DialogFragment
{
	DBHelper dbHelper;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		dbHelper = new DBHelper(getContext());

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
		alertDialog.setTitle("Quiz preferences");
		View v = getActivity().getLayoutInflater().inflate(R.layout.item_quiz_preferences, null);

		final int id = getArguments().getInt("id");
		int quiz_preferences = dbHelper.getInt(id, "quiz_preferences");
		
		final Spinner spinner_kanji = (Spinner) v.findViewById(R.id.spinner_kanji);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, Vocabulary.types_review);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_kanji.setAdapter(adapter);
		spinner_kanji.setSelection(quiz_preferences & 0b11);

		final Spinner spinner_reading = (Spinner) v.findViewById(R.id.spinner_reading);
		spinner_reading.setAdapter(adapter);
		spinner_reading.setSelection((quiz_preferences & 0b1100) >> 2);
		
		if (StringHelper.lengthOfArray(dbHelper.getString(id, "reading")) == 0)
		{
			spinner_reading.setVisibility(View.GONE);
			v.findViewById(R.id.text_reading).setVisibility(View.GONE);
			((TextView) v.findViewById(R.id.text_kanji)).setText("Kana reviews:");
		}
		
		final Spinner spinner_meaning = (Spinner) v.findViewById(R.id.spinner_meaning);
		spinner_meaning.setAdapter(adapter);
		spinner_meaning.setSelection((quiz_preferences & 0b110000) >> 4);
		
		final CheckBox checkbox_quickreview = (CheckBox)v.findViewById(R.id.checkbox_quickreview);
		checkbox_quickreview.setChecked((quiz_preferences & 0b1000000) != 0);
		
		alertDialog.setView(v);		
		alertDialog.setPositiveButton("OK",
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.dismiss();
					dbHelper.updateVocabularyQuizPreferences(id, spinner_kanji.getSelectedItemPosition(), spinner_reading.getSelectedItemPosition(), spinner_meaning.getSelectedItemPosition(), checkbox_quickreview.isChecked());
				}
			});
		return alertDialog.create();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		dbHelper.close();
	}
}
