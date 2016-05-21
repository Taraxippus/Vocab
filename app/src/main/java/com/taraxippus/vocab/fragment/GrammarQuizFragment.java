package com.taraxippus.vocab.fragment;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.support.v7.app.*;
import android.transition.*;
import android.view.*;
import android.widget.*;
import com.taraxippus.vocab.*;
import com.taraxippus.vocab.vocabulary.*;
import java.text.*;
import java.util.*;

public class GrammarQuizFragment extends Fragment
{
	public GrammarQuizFragment()
	{

	}
	
	public void setTransitions(MainActivity main)
	{
		this.setEnterTransition(TransitionInflater.from(main).inflateTransition(android.R.transition.slide_left));
		this.setReturnTransition(TransitionInflater.from(main).inflateTransition(android.R.transition.fade));

		this.setAllowEnterTransitionOverlap(false);
		this.setAllowReturnTransitionOverlap(false);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        View v = inflater.inflate(R.layout.quiz_grammar, container, false);
		
		Button startQuiz = (Button) v.findViewById(R.id.button_start_quiz);
		startQuiz.getBackground().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.MULTIPLY);
		startQuiz.setOnClickListener(new View.OnClickListener()
		{
				@Override
				public void onClick(View p1)
				{
					Intent intent = new Intent().setClass(getActivity(), QuizActivity.class);
					getActivity().startActivityForResult(intent, 0);
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
		text_date.setText(new SimpleDateFormat("MM/dd/yy").format(calendar.getTime()));
		
		return v;
	}

	@Override
	public void onResume()
	{
		super.onResume();

		((MainActivity)getActivity()).setTap(this);
	}
}
