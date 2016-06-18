package com.taraxippus.vocab.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.taraxippus.vocab.MainActivity;
import com.taraxippus.vocab.R;

public class FragmentQuiz extends Fragment
{
	public FragmentQuiz() {}

	public Fragment setDefaultTransitions(Context context)
	{
		this.setEnterTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.slide_left));
		this.setReturnTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.fade));

		this.setAllowEnterTransitionOverlap(false);
		this.setAllowReturnTransitionOverlap(false);

		return this;
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        View v = inflater.inflate(R.layout.fragment_quiz, container, false);

		return v;
	}

	@Override
	public void onResume()
	{
		super.onResume();

		getActivity().setTitle("Quiz");
	}
}
