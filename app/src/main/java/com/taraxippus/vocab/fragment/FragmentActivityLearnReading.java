package com.taraxippus.vocab.fragment;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.taraxippus.vocab.IVocabActivity;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.util.JishoHelper;
import com.taraxippus.vocab.util.OnProcessSuccessListener;
import com.taraxippus.vocab.util.StringHelper;
import com.taraxippus.vocab.vocabulary.Vocabulary;

public class FragmentActivityLearnReading extends Fragment
{
	private IVocabActivity vocabActivity;
	private String soundFile;
	
	public FragmentActivityLearnReading() {}

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
		return inflater.inflate(R.layout.activity_learn_reading, container, false);
	}

	@Override
	public void onViewCreated(final View v, Bundle savedInstanceState)
	{
		int id = getArguments().getInt("id");
		SQLiteDatabase db = vocabActivity.getDBHelper().getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT kanji, reading, reading_trimed, sameReading, soundFile FROM vocab WHERE id = ?", new String[] {"" + id});
		if (res.getCount() <= 0)
		{
			res.close();
			return;
		}

		res.moveToFirst();
		
		final String kanji = res.getString(0);
		final String[] reading = StringHelper.toStringArray(res.getString(res.getColumnIndex("reading")));
		final String[] reading_trimed = StringHelper.toStringArray(res.getString(res.getColumnIndex("reading_trimed")));
		final int[] sameReading = StringHelper.toIntArray(res.getString(res.getColumnIndex("sameReading")));
		soundFile = res.getString(res.getColumnIndex("soundFile"));
		
		res.close();
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < reading.length; ++i)
		{
			sb.append(reading[i]);
			if (i < reading.length - 1)
				sb.append(",\n");
		}
		
		((TextView) v.findViewById(R.id.text_reading)).setText(sb.toString());
		
		if (sameReading.length == 0)
		{
			v.findViewById(R.id.text_title_same_reading).setVisibility(View.GONE);
			v.findViewById(R.id.recycler_same_reading).setVisibility(View.GONE);
		}

		RecyclerView recyclerView = (RecyclerView)v.findViewById(R.id.recycler_same_reading);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
		recyclerView.setAdapter(new FragmentDetail.VocabularyAdapter(getActivity(), vocabActivity.getDBHelper(), recyclerView, sameReading));	
		
		recyclerView = (RecyclerView)v.findViewById(R.id.recycler_kanji_contained);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
		recyclerView.setAdapter(new FragmentDetail.KanjiAdapter(getActivity(), vocabActivity.getDBHelper(), recyclerView, StringHelper.getKanji(kanji)));	
		
		v.findViewById(R.id.card_question).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Vocabulary.playSound(getContext(), soundFile);
				}
			});

		if (soundFile == null || soundFile.isEmpty() || soundFile.contains("\""))
		{
			final Vocabulary v1 = new Vocabulary(id);
			v1.kanji = kanji;
			v1.reading_trimed = reading_trimed;
			
			JishoHelper.findSoundFile(vocabActivity.getDBHelper(), v1, new OnProcessSuccessListener()
				{
					@Override
					public void onProcessSuccess(Object... args)
					{
						soundFile = v1.soundFile;
						if (soundFile != null && !soundFile.isEmpty() && !"-".equals(soundFile))
							v.findViewById(R.id.button_sound).setVisibility(View.VISIBLE);
					}
				});
			return;
		}
		else if (soundFile != null && !soundFile.isEmpty() && !"-".equals(soundFile))
			v.findViewById(R.id.button_sound).setVisibility(View.VISIBLE);
	}
	
	@Override
    public void setUserVisibleHint(boolean isVisibleToUser)
	{
        super.setUserVisibleHint(isVisibleToUser);
        
		if (isVisibleToUser) 
			if (soundFile != null && !soundFile.isEmpty() && !"-".equals(soundFile) && PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("soundLearn", true))
				Vocabulary.playSound(getContext(), soundFile);
     
	}
}
