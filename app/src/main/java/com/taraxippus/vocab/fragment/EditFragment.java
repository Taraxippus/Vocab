package com.taraxippus.vocab.fragment;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.transition.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.taraxippus.vocab.*;
import com.taraxippus.vocab.util.*;
import com.taraxippus.vocab.vocabulary.*;
import java.io.*;

public class EditFragment extends Fragment
{
	public void setTransitions(MainActivity main)
	{
		this.setEnterTransition(TransitionInflater.from(main).inflateTransition(android.R.transition.slide_left));
		this.setReturnTransition(TransitionInflater.from(main).inflateTransition(android.R.transition.fade));

		this.setAllowEnterTransitionOverlap(false);
		this.setAllowReturnTransitionOverlap(false);
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        final View v = inflater.inflate(R.layout.add, container, false);
	
		Spinner spinner = (Spinner) v.findViewById(R.id.type_spinner);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, Vocabulary.types);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		((Button)v.findViewById(R.id.add_button)).setText("Edit");

		Vocabulary vocab = ((MainActivity) getActivity()).vocabulary.get(((MainActivity) getActivity()).vocabulary_selected);

		((TextView)v.findViewById(R.id.kanji_text)).setText(vocab.correctAnswer(QuestionType.KANJI));
		StringBuilder reading = new StringBuilder();
		if (vocab.reading.length != 0)
		{
			for (int i = 0; i < vocab.reading.length - 1; ++i)
			{
				reading.append(vocab.reading[i]);
				reading.append("; ");
			}

			reading.append(vocab.reading[vocab.reading.length - 1]);
		}

		((TextView)v.findViewById(R.id.reading_text)).setText(reading.toString());
		StringBuilder meaning = new StringBuilder();
		for (int i = 0; i < vocab.meaning.length - 1; ++i)
		{
			meaning.append(vocab.meaning[i]);
			meaning.append("; ");
		}

		meaning.append(vocab.meaning[vocab.meaning.length - 1]);

		((TextView)v.findViewById(R.id.meaning_text)).setText(meaning.toString());
		((TextView)v.findViewById(R.id.additional_info_text)).setText(vocab.additionalInfo);
		((TextView)v.findViewById(R.id.notes_text)).setText(vocab.notes);
		((TextView)v.findViewById(R.id.image_text)).setText(vocab.imageFile);
		
		spinner.setSelection(vocab.type.ordinal());

		((CheckBox) v.findViewById(R.id.learned_checkbox)).setChecked(vocab.learned);
		((CheckBox) v.findViewById(R.id.show_info_checkbox)).setChecked(vocab.showInfo);
		
		v.findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					String kanji = StringHelper.trim(((TextView)v.findViewById(R.id.kanji_text)).getText().toString());
					String reading1 = StringHelper.toHiragana(StringHelper.trim(((TextView)v.findViewById(R.id.reading_text)).getText().toString()));
					String meaning1 = StringHelper.trim(((TextView)v.findViewById(R.id.meaning_text)).getText().toString());
					String additionalInfo = StringHelper.trim(((TextView)v.findViewById(R.id.additional_info_text)).getText().toString());
					String notes = StringHelper.trim(((TextView)v.findViewById(R.id.notes_text)).getText().toString());
					String imageUrl = StringHelper.trim(((TextView)v.findViewById(R.id.image_text)).getText().toString());
					
					if (kanji.isEmpty() || meaning1.isEmpty() || (!StringHelper.isKana(kanji)) && reading1.isEmpty())
					{
						AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
						alertDialog.setTitle("Edit Vocabulary");
						alertDialog.setMessage("Please enter kanji, reading and meaning for the vocabulary! (You can leave out the reading if the kanji is written in kana only)");
						alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
							new DialogInterface.OnClickListener() 
							{
								public void onClick(DialogInterface dialog, int which) 
								{
									dialog.dismiss();
								}
							});
						alertDialog.show();
					}
					else
					{
						MainActivity main = (MainActivity)getActivity();

						String[] reading = reading1.split(";");
						for (int i = 0; i < reading.length; ++i)
							reading[i] = StringHelper.trim(reading[i]);

						String[] meaning = meaning1.split(";");
						for (int i = 0; i < meaning.length; ++i)
							meaning[i] = StringHelper.trim(meaning[i]);

						VocabularyType type = VocabularyType.values()[((Spinner) v.findViewById(R.id.type_spinner)).getSelectedItemPosition()];

						Vocabulary vocab = vocab = new Vocabulary(main, type, kanji, reading, meaning, additionalInfo, notes);

						vocab.imageFile = imageUrl;
						vocab.learned = ((CheckBox) v.findViewById(R.id.learned_checkbox)).isChecked();
						vocab.showInfo = ((CheckBox) v.findViewById(R.id.show_info_checkbox)).isChecked();

						if (!vocab.equals(main.vocabulary.get(main.vocabulary_selected)))
						{
							main.vocabulary.get(main.vocabulary_selected).remove(main.vocabulary);
						}

						vocab.add(main.vocabulary, main.vocabulary_selected, ImportType.REPLACE_KEEEP_STATS, getActivity());
						
						main.updateFilter();

						main.changeFragment(main.getDetailFragment(), "edit");
					}
				}
			});

		v.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					getFragmentManager().popBackStack();
				}
			});

		return v;
	}

	@Override
	public void onResume()
	{
		super.onResume();

		((MainActivity)getActivity()).setTap(this);
	}
}
