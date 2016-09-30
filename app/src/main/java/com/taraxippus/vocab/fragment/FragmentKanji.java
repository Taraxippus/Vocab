package com.taraxippus.vocab.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.taraxippus.vocab.ActivityLearn;
import com.taraxippus.vocab.ActivityQuiz;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.ActivityDetailKanji;
import com.taraxippus.vocab.ActivityAddKanji;

public class FragmentKanji extends Fragment
{
	public FragmentKanji() {}

	public Fragment setDefaultTransitions(Context context)
	{
		this.setEnterTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.explode));
		this.setReturnTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.slide_bottom));

		this.setAllowEnterTransitionOverlap(false);
		this.setAllowReturnTransitionOverlap(false);

		return this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        return inflater.inflate(R.layout.fragment_kanji, container, false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.item_filter:
				return true;
				
			case R.id.item_add:
				getContext().startActivity(new Intent(getContext(), ActivityAddKanji.class));
				return true;
				
			case R.id.item_learn_add_next:
				return true;

			case R.id.item_import_jisho:
				return true;
				
			case R.id.item_debug:
				getContext().startActivity(new Intent(getContext(), ActivityDetailKanji.class));
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.fragment_kanji, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		getActivity().setTitle("Kanji");
	}
}
