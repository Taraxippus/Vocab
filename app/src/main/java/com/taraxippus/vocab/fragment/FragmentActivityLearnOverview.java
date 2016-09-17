package com.taraxippus.vocab.fragment;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import com.taraxippus.vocab.ActivityDetail;
import com.taraxippus.vocab.ActivityQuiz;
import com.taraxippus.vocab.IVocabActivity;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.util.JishoHelper;
import com.taraxippus.vocab.util.StringHelper;
import com.taraxippus.vocab.vocabulary.QuestionType;
import com.taraxippus.vocab.vocabulary.Vocabulary;
import com.taraxippus.vocab.vocabulary.VocabularyType;
import java.util.Locale;

public class FragmentActivityLearnOverview extends Fragment
{
	IVocabActivity vocabActivity;
    
	public FragmentActivityLearnOverview() {}

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
		return inflater.inflate(R.layout.activity_learn_overview, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState)
	{
		final int id = getArguments().getInt("id");
		SQLiteDatabase db = vocabActivity.getDBHelper().getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT type, kanji, reading, meaning, additionalInfo, notes, imageFile FROM vocab WHERE id = ?", new String[] {"" + id});
		if (res.getCount() <= 0)
		{
			res.close();
			return;
		}

		res.moveToFirst();

		final int type = res.getInt(res.getColumnIndex("type"));
		final String kanji = res.getString(res.getColumnIndex("kanji"));
		final String[] reading = StringHelper.toStringArray(res.getString(res.getColumnIndex("reading")));
		final String[] meaning = StringHelper.toStringArray(res.getString(res.getColumnIndex("meaning")));
		final String additionalInfo = res.getString(res.getColumnIndex("additionalInfo"));
		final String notes = res.getString(res.getColumnIndex("notes"));
		final String imageFile = res.getString(res.getColumnIndex("imageFile"));
		
		res.close();
		
		TextView text_kanji = (TextView) v.findViewById(R.id.text_kanji);
		text_kanji.setText(Vocabulary.correctAnswer(QuestionType.KANJI, kanji, reading, meaning, additionalInfo, true));
		((TextView)v.findViewById(R.id.text_reading)).setText(reading.length == 0 ? "" : Vocabulary.correctAnswer(QuestionType.READING_INFO, kanji, reading, meaning, additionalInfo, true));
		((TextView)v.findViewById(R.id.text_meaning)).setText(Vocabulary.correctAnswer(QuestionType.MEANING_INFO, kanji, reading, meaning, additionalInfo, true));
		((TextView)v.findViewById(R.id.text_type)).setText(Vocabulary.getType(VocabularyType.values()[type], kanji));
		TextView text_notes = (TextView) v.findViewById(R.id.text_notes);
		text_notes.setText(notes.isEmpty() ? "Notes can help you remember a difficult vocabulary better" : notes);
		final ImageView image_notes = (ImageView) v.findViewById(R.id.image_notes);
		final View progress_image_notes = v.findViewById(R.id.progress_image_notes);

		text_kanji.setTextLocale(Locale.JAPANESE);
		text_notes.setTextLocale(Locale.JAPANESE);
		
		if (!imageFile.isEmpty() && JishoHelper.isInternetAvailable(getContext()))
			new FragmentDetail.DownloadImageTask(image_notes, progress_image_notes).execute(imageFile);
			
		v.findViewById(R.id.button_overflow).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					PopupMenu popup = new PopupMenu(getContext(), view);
					popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
						{
							@Override
							public boolean onMenuItemClick(MenuItem item)
							{
								switch (item.getItemId()) 
								{
									case R.id.item_detail:
										getContext().startActivity(new Intent(getContext(), ActivityDetail.class).putExtra("id", id));
										return true;
										
									case R.id.item_open_jisho:
										JishoHelper.search(getContext(), kanji);
										return true;
										
									case R.id.item_open_jisho_kanji:
										JishoHelper.search(getContext(), kanji + " #kanji");
										return true;
										
									case R.id.item_learn_remove:
										vocabActivity.getDBHelper().updateVocabularyLearned(id, false);
										
									case R.id.item_known:
										if (item.getItemId() == R.id.item_known)
											vocabActivity.getDBHelper().learnVocabulary(id, true);
										
									case R.id.item_skip:
										final int[] newVocabularies = getParentFragment().getArguments().getIntArray("newVocabularies");
										final int index = getParentFragment().getArguments().getInt("index") + 1;
										if (index >= newVocabularies.length)
										{
											getActivity().finish();
											getContext().startActivity(new Intent(getContext(), ActivityQuiz.class));
										}
										else
										{
											final Fragment fragment = new FragmentActivityLearn().setDefaultTransitions(getContext());
											final Bundle args = new Bundle();
											args.putIntArray("newVocabularies", newVocabularies);
											args.putInt("index", index);
											fragment.setArguments(args);
											getParentFragment().getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
										}
										
										return true;
										
									default:
										return false;
								}
							}
						});
					MenuInflater inflater = popup.getMenuInflater();
					inflater.inflate(R.menu.activity_learn, popup.getMenu());
					popup.show();
				}
			});
	}
}
