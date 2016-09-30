package com.taraxippus.vocab.fragment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.text.format.DateFormat;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.vocabulary.Vocabulary;
import java.util.ArrayList;
import java.util.Calendar;

public class FragmentGrammar extends Fragment
{
	public FragmentGrammar() {}
	
	public Fragment setDefaultTransitions(Context context)
	{
		this.setEnterTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.slide_left));
		this.setReturnTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.slide_right));

		this.setAllowEnterTransitionOverlap(false);
		this.setAllowReturnTransitionOverlap(false);
		
		return this;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        return inflater.inflate(R.layout.fragment_grammar, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState)
	{
		super.onViewCreated(v, savedInstanceState);
		
//		Button startQuiz = (Button) v.findViewById(R.id.button_start_quiz);
//		startQuiz.getBackground().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.MULTIPLY);
//		startQuiz.setOnClickListener(new View.OnClickListener()
//			{
//				@Override
//				public void onClick(View p1)
//				{
//					String url = "http://guidetojapanese.org/learn/grammar";
//					CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
//					builder.setToolbarColor(getContext().getColor(R.color.primary));
//					builder.setStartAnimations(getActivity(), android.R.anim.slide_in_left, android.R.anim.slide_out_right);
//					builder.setExitAnimations(getActivity(), android.R.anim.slide_in_left, android.R.anim.slide_out_right);
//					CustomTabsIntent customTabsIntent = builder.build();
//					customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
//				}
//			});
	}
	
	@Override
	public void onResume()
	{
		super.onResume();

		getActivity().setTitle("Grammar");
	}
}
