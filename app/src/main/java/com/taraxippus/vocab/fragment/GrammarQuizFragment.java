package com.taraxippus.vocab.fragment;

import android.app.*;
import android.os.*;
import android.view.*;
import com.taraxippus.vocab.*;

public class GrammarQuizFragment extends Fragment
{
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        return inflater.inflate(R.layout.quiz_grammar, container, false);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		((MainActivity)getActivity()).setTap(this);
	}
}
