package com.taraxippus.vocab.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.taraxippus.vocab.MainActivity;
import com.taraxippus.vocab.QuizActivity;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.vocabulary.Vocabulary;
import java.util.ArrayList;
import java.util.Calendar;

public class FragmentGrammarQuiz extends Fragment
{
	public FragmentGrammarQuiz() {}
	
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
        View v = inflater.inflate(R.layout.fragment_quiz_grammar, container, false);
		
		Button startQuiz = (Button) v.findViewById(R.id.button_start_quiz);
		startQuiz.getBackground().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.MULTIPLY);
		startQuiz.setOnClickListener(new View.OnClickListener()
		{
				@Override
				public void onClick(View p1)
				{
					getActivity().startActivity(new Intent(getActivity(), QuizActivity.class));
				}
		});
		
		final LinearLayout layoutGrammarType = (LinearLayout)v.findViewById(R.id.layout_grammar_type);
		final ArrayList<CheckBox> boxes_grammar = new ArrayList<>();
		
		for (int i = 0; i < Vocabulary.types.size(); ++i)
		{
			CheckBox box = new CheckBox(getActivity());
			box.setChecked(true);
			box.setText("Show " + Vocabulary.types.get(i));
			layoutGrammarType.addView(box);

			boxes_grammar.add(box);
		}
		
		final LinearLayout layoutVocabularyType = (LinearLayout)v.findViewById(R.id.layout_vocabulary_type);

		final ArrayList<CheckBox> boxes_type = new ArrayList<>();
		
		for (int i = 0; i < Vocabulary.types.size(); ++i)
		{
			CheckBox box = new CheckBox(getActivity());
			box.setChecked(true);
			box.setText("Show " + Vocabulary.types.get(i));
			layoutVocabularyType.addView(box);

			boxes_type.add(box);
		}
		
		TextView text_date = (TextView) v.findViewById(R.id.text_date);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -30);
		text_date.setText(DateFormat.getDateFormat(getContext()).format(calendar.getTime()));
		
		return v;
	}

	@Override
	public void onResume()
	{
		super.onResume();

		getActivity().setTitle("Grammar Quiz");
	}
}
