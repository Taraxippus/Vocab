package com.taraxippus.vocab.util;

import android.app.*;
import android.content.*;
import android.support.v7.view.menu.*;
import android.support.v7.widget.*;
import android.transition.*;
import android.view.*;
import android.widget.*;
import com.taraxippus.vocab.*;
import com.taraxippus.vocab.fragment.*;
import com.taraxippus.vocab.vocabulary.*;

import android.support.v7.widget.PopupMenu;

public class ViewVocabularyDialog implements DialogInterface.OnDismissListener, View.OnClickListener, View.OnTouchListener, GestureDetector.OnGestureListener
{
	final MainActivity main;
	public final AlertDialog alertDialog;
	
	private GestureDetector gestureDetector; 
	
	final TextView kanji, reading, meaning, type;
	final EditText practice;
	
	int index = 0;
	
	public ViewVocabularyDialog(MainActivity main)
	{
		this.main = main;
		
		gestureDetector = new GestureDetector(main, this);
		
		alertDialog = new AlertDialog.Builder(main).create();
		alertDialog.setOnDismissListener(this);
		
		final View v = main.getLayoutInflater().inflate(R.layout.learn, null);
		kanji = (TextView)v.findViewById(R.id.kanji_text);
		reading = (TextView)v.findViewById(R.id.reading_text);
		meaning = (TextView)v.findViewById(R.id.meaning_text);
		type = (TextView)v.findViewById(R.id.type);
		practice = (EditText)v.findViewById(R.id.answer_text);
		
		v.findViewById(R.id.overflow_button).setOnClickListener(this);
		
		v.setOnTouchListener(this);
		
		alertDialog.setView(v);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Next",
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) {}
			});
		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Close",
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.cancel();
				}
			});
		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Previous",
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) {}
			});

		alertDialog.show();
		alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					index++;
					updateView();
				}
			});
	
		alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					index--;
					updateView();
				}
			});
			
			updateView();
	}
	
	public void updateView()
	{
		Vocabulary vocab = main.vocabulary_learned_new.get(index);

		kanji.setText(vocab.correctAnswer(QuestionType.KANJI));
		reading.setText(vocab.correctAnswer(QuestionType.READING));
		meaning.setText(vocab.correctAnswer(QuestionType.MEANING_INFO));
		type.setText(vocab.getType());

		alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(main.vocabulary_learned_new.size() > index + 1);
		alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(index > 0);
		
		practice.getText().clear();
	}
	
	public void removeCurrent()
	{
		main.vocabulary_learned_new.remove(index);

		if (index == main.vocabulary_learned_new.size())
		{
			index--;
			if (index < 0)
			{
				alertDialog.dismiss();
				return;
			}
		}

		updateView();
	}
	
	@Override
	public void onDismiss(DialogInterface p1)
	{
		if (main.viewVocabularyDialog == this)
		{
			main.viewVocabularyDialog = null;
		}
	}
	
	@Override
	public void onClick(View v)
	{
		PopupMenu popup = new PopupMenu(main, v)
		{
			@Override
			public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item)
			{
				switch (item.getItemId()) 
				{
					case R.id.detail:
						alertDialog.dismiss();
						main.vocabulary_selected = main.vocabulary.indexOf(main.vocabulary_learned_new.get(index));
						
						Fragment fragment = new VocabularyFragment();
						fragment.setSharedElementEnterTransition(TransitionInflater.from(main).inflateTransition(R.transition.change_image_transform));
						fragment.setSharedElementReturnTransition(TransitionInflater.from(main).inflateTransition(R.transition.change_image_transform));

						fragment.setAllowEnterTransitionOverlap(false);
						fragment.setAllowReturnTransitionOverlap(false);

						fragment.setEnterTransition(TransitionInflater.from(main).inflateTransition(android.R.transition.slide_top));
						fragment.setReturnTransition(TransitionInflater.from(main).inflateTransition(android.R.transition.fade));

						FragmentManager fragmentManager = main.getFragmentManager();
						fragmentManager.beginTransaction()
							.replace(R.id.content_frame, fragment)
							.addToBackStack("detail")
							.commit();
							
						return true;
					
					case R.id.open_jisho:
						main.searchJisho(kanji.getText().toString());
						
						return true;
						
					case R.id.open_jisho_kanji:
						main.searchJisho(kanji.getText().toString() + "%23kanji");

						return true;
						
					case R.id.learn_remove:
						main.vocabulary_learned_new.get(index).learned = false;
						
					case R.id.known:
						main.vocabulary_learned_new.get(index).category = 1;
						
					case R.id.exclude:
						main.vocabulary_learned.remove(main.vocabulary_learned_new.get(index));
						
						if (main.vocabulary.get(main.vocabulary_selected) == main.vocabulary_learned_new.get(index))
						{
							main.quiz.answer = Answer.CORRECT;
							main.quiz.next();
							
							main.setTap(main.quiz);
						}
						
						removeCurrent();

						return true;

					default:
						return super.onMenuItemSelected(menu, item);
				}
			}
		};
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.vocabulary_show, popup.getMenu());
		
		popup.show();
	}
	
	@Override
	public boolean onTouch(View p1, MotionEvent p2)
	{
		return gestureDetector.onTouchEvent(p2);
	}

	@Override
	public boolean onDown(MotionEvent p1)
	{
		return false;
	}

	@Override
	public void onShowPress(MotionEvent p1)
	{
	}

	@Override
	public boolean onSingleTapUp(MotionEvent p1)
	{
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent p1, MotionEvent p2, float p3, float p4)
	{
		return false;
	}

	@Override
	public void onLongPress(MotionEvent p1)
	{
	}

	@Override
	public boolean onFling(MotionEvent p1, MotionEvent p2, float p3, float p4)
	{
		if (p3 < -3000 && main.vocabulary_learned_new.size() > index + 1)
		{
			index++;
			updateView();

            return true;
		}
		else if (p3 > 3000 && index > 0)
		{
			index--;
			updateView();
			
			return true;
		}
		return false;
	}
}