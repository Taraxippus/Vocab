package com.taraxippus.vocab.fragment;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.taraxippus.vocab.*;
import com.taraxippus.vocab.vocabulary.*;

public class AddFragment extends Fragment
{
	public AddFragment()
	{
		this.edit = false;
	}
	
	final boolean edit;
	
	public AddFragment(boolean edit)
	{
		this.edit = edit;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        final View v = inflater.inflate(R.layout.add, container, false);
		
		Spinner spinner = (Spinner) v.findViewById(R.id.type_spinner);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, Vocabulary.types);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		if (edit)
		{
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
			
			spinner.setSelection(vocab.type.ordinal());
			
			((CheckBox) v.findViewById(R.id.learned_checkbox)).setChecked(vocab.learned);
			((CheckBox) v.findViewById(R.id.show_info_checkbox)).setChecked(vocab.showInfo);
			
		}
		else
		{
			v.findViewById(R.id.cancel_button).setVisibility(View.INVISIBLE);
			
			spinner.setSelection(0);
		}
		
		v.findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					String kanji = Vocabulary.trim(((TextView)v.findViewById(R.id.kanji_text)).getText().toString());
					String reading1 = Vocabulary.toHiragana(Vocabulary.trim(((TextView)v.findViewById(R.id.reading_text)).getText().toString()));
					String meaning1 = Vocabulary.trim(((TextView)v.findViewById(R.id.meaning_text)).getText().toString());
					String additionalInfo = Vocabulary.trim(((TextView)v.findViewById(R.id.additional_info_text)).getText().toString());
					
					if (kanji.isEmpty() || meaning1.isEmpty() || (!Vocabulary.isKana(kanji)) && reading1.isEmpty())
					{
						AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
						alertDialog.setTitle(edit ? "Edit Vocabulary" : "Add Vocabulary");
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

						
						MainActivity main = (MainActivity)getActivity();
						FragmentManager fragmentManager = main.getFragmentManager();
						fragmentManager.beginTransaction()
							.replace(R.id.content_frame, main.home)
							.addToBackStack("add")
							.commit();
						
						String[] reading = reading1.split(";");
						for (int i = 0; i < reading.length; ++i)
							reading[i] = Vocabulary.trim(reading[i]);
							
						String[] meaning = meaning1.split(";");
						for (int i = 0; i < meaning.length; ++i)
							meaning[i] = Vocabulary.trim(meaning[i]);
							
						Vocabulary.Type type = Vocabulary.Type.values()[((Spinner) v.findViewById(R.id.type_spinner)).getSelectedItemPosition()];
							
						Vocabulary vocab = vocab = new Vocabulary(main, type, kanji, reading, meaning, additionalInfo);

						vocab.learned = ((CheckBox) v.findViewById(R.id.learned_checkbox)).isChecked();
						vocab.showInfo = ((CheckBox) v.findViewById(R.id.show_info_checkbox)).isChecked();
						
						if (edit)
							vocab.add(main.vocabulary, main.vocabulary_selected, Vocabulary.ImportType.REPLACE_KEEEP_STATS, getActivity());
						else		
							vocab.add(main.vocabulary, Vocabulary.ImportType.ASK, getActivity());
						
						main.updateFilter();
						
						((Spinner) v.findViewById(R.id.type_spinner)).setSelection(0);
						
					}
				}
		});
		
		v.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					((EditText)v.findViewById(R.id.kanji_text)).getText().clear();
					((EditText)v.findViewById(R.id.reading_text)).getText().clear();
					((EditText)v.findViewById(R.id.meaning_text)).getText().clear();
					((EditText)v.findViewById(R.id.additional_info_text)).getText().clear();
					
					((Spinner) v.findViewById(R.id.type_spinner)).setSelection(0);
					
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
