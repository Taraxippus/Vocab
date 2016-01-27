package com.taraxippus.vocab;

import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.widget.FrameLayout.*;
import com.taraxippus.vocab.vocabulary.*;
import java.text.*;
import java.util.*;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.ViewHolder> implements View.OnClickListener
{
	@Override
	public void onClick(View v)
	{
		if (v instanceof Button)
		{
			if (v.getId() == R.id.filter_button)
			{
				main.showFilterMenu();
			}
			else if (v.getId() == R.id.jisho_button)
			{
				main.searchJisho(main.queryText);
			}
		}
		else
			main.onVocabularyClicked(v);
	}
	
    public static class ViewHolder extends RecyclerView.ViewHolder 
	{
        public ViewHolder(View v) 
		{
            super(v);
        }
    }

	final MainActivity main;
	final boolean learned;
	
    public VocabularyAdapter(MainActivity main, boolean learned)
	{
       this.main = main;
	   this.learned = learned;
    }

    @Override
    public VocabularyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View v;
		
		if (viewType == 0)
		{
			v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stats, parent, false);
			v.findViewById(R.id.jisho_button).setOnClickListener(this);
			v.findViewById(R.id.filter_button).setOnClickListener(this);
		}
		else
		{
			v = LayoutInflater.from(parent.getContext()).inflate(R.layout.vocabulary_item, parent, false);
			v.findViewById(R.id.card_vocabulary).setOnClickListener(this);
		}

		ViewHolder vh = new ViewHolder(v);
		return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) 
	{
		if (position == 0)
		{
			if (main.queryText != null && !main.queryText.isEmpty())
			{
				holder.itemView.findViewById(R.id.stats_layout).setVisibility(View.INVISIBLE);
				holder.itemView.findViewById(R.id.stats_layout).setLayoutParams(new FrameLayout.LayoutParams(0, 0));
			
				holder.itemView.findViewById(R.id.results_layout).setVisibility(View.VISIBLE);
				holder.itemView.findViewById(R.id.results_layout).setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				
				((TextView) holder.itemView.findViewById(R.id.results_text)).setText(main.vocabulary_filtered.size() + " results for search \"" + main.queryText + "\"");
			}
			else
			{
				holder.itemView.findViewById(R.id.stats_layout).setVisibility(View.VISIBLE);
				holder.itemView.findViewById(R.id.stats_layout).setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			
				holder.itemView.findViewById(R.id.results_layout).setVisibility(View.INVISIBLE);
				holder.itemView.findViewById(R.id.results_layout).setLayoutParams(new FrameLayout.LayoutParams(0, 0));
				
			}
			
			int learned_total = 0;
			int critical = 0;
			int correct_kanji = 0;
			int total_kanji = 0;
			int correct_reading = 0;
			int total_reading = 0;
			int correct_meaning = 0;
			int total_meaning = 0;
			long nextReview = 0;
			int review_now = 0, review_hour = 0, review_day = 0;
			
			for (Vocabulary v : main.vocabulary)
			{
				if (v.learned)
				{
					learned_total++;
					critical += v.category <= 1 ? 1 : 0;
					correct_kanji += v.timesCorrect_kanji;
					total_kanji += v.timesChecked_kanji;
					correct_reading += v.timesCorrect_reading;
					total_reading += v.timesChecked_reading;
					correct_meaning += v.timesCorrect_meaning;
					total_meaning += v.timesChecked_meaning;
					
					if (nextReview == 0)
						nextReview = v.lastChecked + v.getNextReview();
					else
						nextReview = Math.min(nextReview, v.lastChecked + v.getNextReview());
						
					if (v.lastChecked + v.getNextReview() < System.currentTimeMillis())
						review_now++;
					if (v.lastChecked + v.getNextReview() < System.currentTimeMillis() + 1000 * 60 * 60)
						review_hour++;
					if (v.lastChecked + v.getNextReview() < System.currentTimeMillis() + 1000 * 60 * 60 * 24)
						review_day++;
				}
			}
			
			TextView learned = (TextView)holder.itemView.findViewById(R.id.learned_progress_number);
			learned.setText(learned_total + " / " + main.vocabulary.size());

			ProgressBar learned_progress = (ProgressBar)holder.itemView.findViewById(R.id.learned_progress);
			learned_progress.setMax(main.vocabulary.size());
			learned_progress.setProgress(learned_total - critical);
			learned_progress.setSecondaryProgress(learned_total);
			
			TextView total_succes = (TextView)holder.itemView.findViewById(R.id.total_progress_number);
			total_succes.setText((correct_meaning + correct_reading + correct_kanji) + " / " + (total_meaning + total_reading + total_meaning));

			ProgressBar total_progress = (ProgressBar)holder.itemView.findViewById(R.id.total_progress);
			total_progress.setMax(total_meaning + total_reading + total_kanji);
			total_progress.setProgress(correct_meaning + correct_reading + correct_kanji);
			
			TextView kanji_success = (TextView)holder.itemView.findViewById(R.id.kanji_progress_number);
			kanji_success.setText(correct_kanji + " / " + total_kanji);

			ProgressBar kanji_progress = (ProgressBar)holder.itemView.findViewById(R.id.kanji_progress);
			kanji_progress.setMax(total_kanji);
			kanji_progress.setProgress(correct_kanji);
			
			TextView reading_succes = (TextView)holder.itemView.findViewById(R.id.reading_progress_number);
			reading_succes.setText(correct_reading + " / " + total_reading);

			ProgressBar reading_progress = (ProgressBar)holder.itemView.findViewById(R.id.reading_progress);
			reading_progress.setMax(total_reading);
			reading_progress.setProgress(correct_reading);
			
			TextView meaning_succes = (TextView)holder.itemView.findViewById(R.id.meaning_progress_number);
			meaning_succes.setText(correct_meaning + " / " + total_meaning);

			ProgressBar meaning_progress = (ProgressBar)holder.itemView.findViewById(R.id.meaning_progress);
			meaning_progress.setMax(total_meaning);
			meaning_progress.setProgress(correct_meaning);
			
			TextView next_review_now = (TextView)holder.itemView.findViewById(R.id.next_review_now);
			next_review_now.setText("Vocabularies to review now: " + review_now);
			
			TextView next_review_hour = (TextView)holder.itemView.findViewById(R.id.next_review_hour);
			next_review_hour.setText("Vocabularies to review in the next hour: " + review_hour);

			TextView next_review_day = (TextView)holder.itemView.findViewById(R.id.next_review_day);
			next_review_day.setText("Vocabularies to review in the next day: " + review_day);
			
			TextView next_review = (TextView)holder.itemView.findViewById(R.id.next_review);
			next_review.setText("Next Review: " + (nextReview < System.currentTimeMillis() ? "Now" : nextReview == 0 ? "Never" : new SimpleDateFormat().format(new Date(nextReview))));
		}
		else
		{
			position--;
			
			Vocabulary v = main.vocabulary_filtered.get(position);
			
			if (newCategory(position, v))
			{
				TextView category = (TextView)holder.itemView.findViewById(R.id.category_text);
			
				category.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
				category.setVisibility(View.VISIBLE);
				
				if (main.sortType == Vocabulary.SortType.CATEGORY || main.sortType == Vocabulary.SortType.CATEGORY_REVERSED)
				{
					category.setText(!v.learned ? "Not in learned vocabulary" : v.category == 0 ? "Critical vocabularies" : "Category " + v.category);
				}
				else if (main.sortType == Vocabulary.SortType.TYPE)
				{
					category.setText(Vocabulary.types.get(v.type.ordinal()));
				}
				else if (main.sortType == Vocabulary.SortType.NEXT_REVIEW)
				{
					category.setText((!v.learned ? "Not in learned vocabulary" : v.lastChecked + v.getNextReview() < System.currentTimeMillis() ? "Review now" 
					: v.lastChecked + v.getNextReview() < System.currentTimeMillis() + 1000 * 60 * 60 ? "Review in the next hour"
					: v.lastChecked + v.getNextReview() < System.currentTimeMillis() + 1000 * 60 * 60 * 48  ? "Review in " + ((v.lastChecked + v.getNextReview() - System.currentTimeMillis()) / 1000 / 60 / 60 + 1) + " hours"
				 	: "Review in " + ((v.lastChecked + v.getNextReview() - System.currentTimeMillis()) / 1000 / 60 / 60 / 24 + 1) + " days"));
				}
				else
					category.setText("");
			}
			else
			{
				TextView category = (TextView)holder.itemView.findViewById(R.id.category_text);
				category.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
				category.setVisibility(View.INVISIBLE);
			}
			
			holder.itemView.findViewById(R.id.card_vocabulary).setTransitionName("card" + position);
			TextView kanji = (TextView)holder.itemView.findViewById(R.id.kanji_text);
			kanji.setText(v.correctAnswer(QuestionType.KANJI));
			kanji.setTransitionName("kanji" + position);
			TextView reading = (TextView)holder.itemView.findViewById(R.id.reading_text);
			reading.setText(v.correctAnswer(QuestionType.READING));
			reading.setTransitionName("reading" + position);
			TextView meaning = (TextView)holder.itemView.findViewById(R.id.meaning_text);
			meaning.setText(v.correctAnswer(QuestionType.MEANING));
			meaning.setTransitionName("meaning" + position);

			if (main.viewType == Vocabulary.ViewType.LARGE)
			{
				kanji.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 50);
				reading.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
				meaning.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
			}
			else if (main.viewType == Vocabulary.ViewType.MEDIUM)
			{
				kanji.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 35);
				reading.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
				meaning.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
			}
			else if (main.viewType == Vocabulary.ViewType.SMALL)
			{
				kanji.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
				reading.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
				meaning.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
			}
			
			ImageView check = (ImageView)holder.itemView.findViewById(R.id.check);
			check.setVisibility(v.learned ? View.VISIBLE : View.INVISIBLE);
			check.setImageResource(v.category == 0 ? R.drawable.alert : R.drawable.check);
		}
	}

	public boolean newCategory(int position, Vocabulary v)
	{
		Vocabulary v1 = v;
		
		if (position > 0)
			v1 = main.vocabulary_filtered.get(position - 1);
		
		switch (main.sortType)
		{
			case CATEGORY:
			case CATEGORY_REVERSED:
				return position == 0 || v.category != v1.category && v.learned || v.learned != v1.learned;
			
			case TYPE:
				return position == 0 || v.type != v1.type;
				
			case NEXT_REVIEW:
				return position == 0 || v.learned != v1.learned || v.learned &&
				( v.lastChecked + v.getNextReview() >= System.currentTimeMillis() && v1.lastChecked + v1.getNextReview() < System.currentTimeMillis()
				|| v.lastChecked + v.getNextReview() >= System.currentTimeMillis() + 1000 * 60 * 60 && v1.lastChecked + v1.getNextReview() < System.currentTimeMillis() + 1000 * 60 * 60
				|| v.lastChecked + v.getNextReview() >= System.currentTimeMillis() && (v.lastChecked + v.getNextReview() - System.currentTimeMillis() < 1000 * 60 * 60 * 48 ? 
				((int)(v.lastChecked + v.getNextReview() - System.currentTimeMillis()) / 1000 / 60 / 60 > (int)((v1.lastChecked + v1.getNextReview() - System.currentTimeMillis()) / 1000 / 60 / 60))
				: ((int)((v.lastChecked + v.getNextReview() - System.currentTimeMillis()) / 1000 / 60 / 60 / 24) > (int)(((v1.lastChecked + v1.getNextReview() - System.currentTimeMillis()) / 1000 / 60 / 60 / 24))
				)));
				
			default:
				return false;
		}
	}
	
    @Override
    public int getItemCount() 
	{
        return main.vocabulary_filtered.size() + 1;
    }
	
	@Override
	public int getItemViewType(int position)
	{ 
		if (position == 0)
			return 0;

		return 1;
	}
}
