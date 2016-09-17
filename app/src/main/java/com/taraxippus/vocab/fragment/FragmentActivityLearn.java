package com.taraxippus.vocab.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.taraxippus.vocab.IVocabActivity;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.util.StringHelper;

public class FragmentActivityLearn extends Fragment
{
	private ViewPager viewPager;
	private boolean hasReading;
	private IVocabActivity vocabActivity;
	
	public FragmentActivityLearn() {}
	
	public Fragment setDefaultTransitions(Context context)
	{
		this.setEnterTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.fade));
		
		return this;
	}

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
		return inflater.inflate(R.layout.activity_learn, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState)
	{
		hasReading = StringHelper.toStringArray(vocabActivity.getDBHelper().getString(getArguments().getIntArray("newVocabularies")[getArguments().getInt("index")], "reading")).length > 0;
		
		viewPager = (ViewPager) v.findViewById(R.id.pager_learn);
		viewPager.setAdapter(new LearnAdapter(getChildFragmentManager()));
	}
	
	public class LearnAdapter extends FragmentStatePagerAdapter
	{
		public LearnAdapter(FragmentManager fragmentManager)
		{
			super(fragmentManager);
		}

		@Override
		public Fragment getItem(int pos)
		{
			final Fragment f;
			
			if (pos == 0)
				f = new FragmentActivityLearnOverview();
			else if (pos == 1)
				f = new FragmentActivityLearnKanji();
			else if (pos == 2 && hasReading)
				f = new FragmentActivityLearnReading();
			else if (pos == 3 && hasReading || pos == 2 && ! hasReading)
				f = new FragmentActivityLearnMeaning();
			else
				f = new FragmentActivityLearnPractice();
			
			final Bundle args = new Bundle();
			args.putInt("id", getArguments().getIntArray("newVocabularies")[getArguments().getInt("index")]);
			f.setArguments(args);
			
			return f;
		}

		@Override
		public int getCount()
		{
			return hasReading ? 5 : 4;
		}

		private static final String[] TITLES = new String[] { "Overview", "Kanji", "Reading", "Meaning", "Practice"};
		private static final String[] TITLES_NO_READING = new String[] { "Overview", "Kana", "Meaning", "Practice"};
		
		@Override
		public CharSequence getPageTitle(int position)
		{
			return hasReading ? TITLES[position] : TITLES_NO_READING[position];
		}
	}
}
