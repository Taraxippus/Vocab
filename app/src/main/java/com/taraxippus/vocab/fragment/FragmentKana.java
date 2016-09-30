package com.taraxippus.vocab.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.taraxippus.vocab.R;

public class FragmentKana extends Fragment
{
	public FragmentKana() {}

	public Fragment setDefaultTransitions(Context context)
	{
		this.setEnterTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.explode));
		this.setReturnTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.slide_bottom));

		this.setAllowEnterTransitionOverlap(false);
		this.setAllowReturnTransitionOverlap(false);

		return this;
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        return inflater.inflate(R.layout.fragment_kana, container, false);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		getActivity().setTitle("Kana");
	}
}
