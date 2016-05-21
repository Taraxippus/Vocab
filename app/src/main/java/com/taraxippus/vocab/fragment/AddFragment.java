package com.taraxippus.vocab.fragment;

import android.app.*;
import android.content.*;
import android.os.*;
import android.transition.*;
import android.view.*;
import android.widget.*;
import com.taraxippus.vocab.*;
import com.taraxippus.vocab.util.*;
import com.taraxippus.vocab.vocabulary.*;

public class AddFragment extends Fragment
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
		spinner.setSelection(0);
		
		v.findViewById(R.id.cancel_button).setVisibility(View.INVISIBLE);
		
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
						alertDialog.setTitle("Add Vocabulary");
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
						((EditText)v.findViewById(R.id.kanji_text)).getText().clear();
						((EditText)v.findViewById(R.id.reading_text)).getText().clear();
						((EditText)v.findViewById(R.id.meaning_text)).getText().clear();
						((EditText)v.findViewById(R.id.additional_info_text)).getText().clear();
						((EditText)v.findViewById(R.id.notes_text)).getText().clear();
						((EditText)v.findViewById(R.id.image_text)).getText().clear();
						
						MainActivity main = (MainActivity)getActivity();
						
						String[] reading = reading1.split(";");
						for (int i = 0; i < reading.length; ++i)
							reading[i] = StringHelper.trim(reading[i]);
							
						String[] meaning = meaning1.split(";");
						for (int i = 0; i < meaning.length; ++i)
							meaning[i] = StringHelper.trim(meaning[i]);
							
						VocabularyType type = VocabularyType.values()[((Spinner) v.findViewById(R.id.type_spinner)).getSelectedItemPosition()];
							
						Vocabulary vocab = vocab = new Vocabulary(main, type, kanji, reading, meaning, additionalInfo, notes);

						vocab.learned = ((CheckBox) v.findViewById(R.id.learned_checkbox)).isChecked();
						vocab.showInfo = ((CheckBox) v.findViewById(R.id.show_info_checkbox)).isChecked();
						vocab.imageFile = imageUrl;
						vocab.add(main.vocabulary, ImportType.ASK, getActivity());
						
						main.vocabulary_selected = main.vocabulary.lastIndexOf(vocab);
						main.updateFilter();
						
						((Spinner) v.findViewById(R.id.type_spinner)).setSelection(0);
						((CheckBox) v.findViewById(R.id.learned_checkbox)).setChecked(false);
						((CheckBox) v.findViewById(R.id.show_info_checkbox)).setChecked(true);
						
						main.changeFragment(main.getDetailFragment(), "add");
					}
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
