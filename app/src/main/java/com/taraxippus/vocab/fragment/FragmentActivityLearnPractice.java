package com.taraxippus.vocab.fragment;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.taraxippus.vocab.ActivityQuiz;
import com.taraxippus.vocab.IVocabActivity;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.util.StringHelper;
import com.taraxippus.vocab.dialog.DialogHelper;
import android.support.design.widget.TextInputLayout;

public class FragmentActivityLearnPractice extends Fragment
{
	IVocabActivity vocabActivity;

	public FragmentActivityLearnPractice() {}

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
		return inflater.inflate(R.layout.activity_learn_practice, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState)
	{
		final int id = getArguments().getInt("id");
		SQLiteDatabase db = vocabActivity.getDBHelper().getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT kanji, reading, meaning FROM vocab WHERE id = ?", new String[] {"" + id});
		if (res.getCount() <= 0)
		{
			res.close();
			return;
		}

		res.moveToFirst();

		final String kanji = res.getString(res.getColumnIndex("kanji"));
		final String[] reading = StringHelper.toStringArray(res.getString(res.getColumnIndex("reading")));
		final String[] meaning = StringHelper.toStringArray(res.getString(res.getColumnIndex("meaning")));
		
		res.close();
		
		final TextInputEditText text_kanji = (TextInputEditText) v.findViewById(R.id.text_kanji);
		final TextInputEditText text_reading = (TextInputEditText) v.findViewById(R.id.text_reading);
		final TextInputEditText text_meaning = (TextInputEditText) v.findViewById(R.id.text_meaning);
		
		if (reading.length == 0)
		{
			v.findViewById(R.id.layout_text_reading).setVisibility(View.GONE);
			//text_kanji.setHint("Kana");
			((TextInputLayout) v.findViewById(R.id.layout_text_kanji)).setHint("Kana");
		}
		
		final int[] newVocabularies = getParentFragment().getArguments().getIntArray("newVocabularies");
		final int index = getParentFragment().getArguments().getInt("index") + 1;
		
		Button button_next = (Button) v.findViewById(R.id.button_next);
		
		if (index >= newVocabularies.length)
			button_next.setText("Start Quiz");
			
		button_next.setOnClickListener(new View.OnClickListener()
		{
				@Override
				public void onClick(View p1)
				{
					String input = StringHelper.trim(text_kanji.getText().toString());
					if (!input.equalsIgnoreCase(kanji))
					{
						DialogHelper.createDialog(getContext(), "Practice", reading.length == 0 ? "Please type the correct kana!" : "Please type the correct kanji!");
						return;
					}
					
					if (reading.length > 0)
					{
						input = StringHelper.trim(text_reading.getText().toString());
						
						if (input.contains("・"))
						{
							DialogHelper.createDialog(getContext(), "Practice", "You don't have to type '・'!");
							return;
						}

						boolean flag = false;
						for (int i = 0; i < reading.length; ++i)
							if (input.equalsIgnoreCase(reading[i].replace("・", "")))
							{
								flag = true;
								break;
							}
						
						if (!flag)
						{
							DialogHelper.createDialog(getContext(), "Practice", "Please type a correct reading!");
							return;
						}
					}
					
					input = StringHelper.trim(text_meaning.getText().toString());
					
					boolean flag = false;
					for (int i = 0; i < meaning.length; ++i)
						if (input.equalsIgnoreCase(meaning[i]))
						{
							flag = true;
							break;
						}

					if (!flag)
					{
						DialogHelper.createDialog(getContext(), "Practice", "Please type a correct meaning!");
						return;
					}
					
					vocabActivity.getDBHelper().learnVocabulary(id, false);
					
					if (index >= newVocabularies.length)
					{
						getActivity().finish();
						getContext().startActivity(new Intent(getContext(), ActivityQuiz.class));
						return;
					}
					
					final Fragment fragment = new FragmentActivityLearn().setDefaultTransitions(getContext());
					final Bundle args = new Bundle();
					args.putIntArray("newVocabularies", newVocabularies);
					args.putInt("index", index);
					fragment.setArguments(args);
					getParentFragment().getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
				}
		});
	}
}
