package com.taraxippus.vocab;

import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.taraxippus.vocab.util.*;
import com.taraxippus.vocab.vocabulary.*;
import java.text.*;
import java.util.*;
import android.app.*;
import android.support.v7.widget.*;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.ViewHolder> implements View.OnClickListener
{
	@Override
	public void onClick(View v1)
	{
		if (v1 instanceof Button)
		{
			if (v1.getId() == R.id.filter_button)
			{
				main.showFilterMenu();
			}
			else if (v1.getId() == R.id.jisho_button)
			{
				main.jishoHelper.search(main.queryText);
			}
		}
		else if (v1 instanceof ImageButton)
		{
			if (!main.jishoHelper.offlineStrokeOrder() && !main.jishoHelper.isInternetAvailable())
			{
				Toast.makeText(main, "No internet connection", Toast.LENGTH_SHORT).show();
				return;
			}

			AlertDialog alertDialog = new AlertDialog.Builder(main).create();
			View v = main.getLayoutInflater().inflate(R.layout.stroke_order_dialog, null);

			v.findViewById(R.id.overflow_button).setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{

					}

				});

			final LinearLayout layout_kanji = (LinearLayout) v.findViewById(R.id.layout_kanji);

			char[] kanji = new char[main.queryText.length()];
			main.queryText.getChars(0, main.queryText.length(), kanji, 0);
			String[] hex = new String[kanji.length];

			for (int i = 0; i < kanji.length; ++i)
			{
				hex[i] = Integer.toHexString(kanji[i]);
				while (hex[i].length() < 5)
				{
					hex[i] = "0" + hex[i];
				}
			}

			final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
			params.gravity = Gravity.CENTER;

			main.jishoHelper.createStrokeOrderView(hex, new OnProcessSuccessListener()
				{
					@Override
					public void onProcessSuccess(Object... args)
					{
						layout_kanji.addView((View) args[0], params);
					}
				}, false, false);
				
			alertDialog.setView(v);		
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						dialog.dismiss();
					}
				});

			alertDialog.show();
		}
		else
			main.onVocabularyClicked(v1);
	}
	
    public static class ViewHolder extends RecyclerView.ViewHolder 
	{
        public ViewHolder(View v) 
		{
            super(v);
        }
    }

	final MainActivity main;
	
    public VocabularyAdapter(MainActivity main)
	{
       this.main = main;
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
			v.findViewById(R.id.stroke_order).setOnClickListener(this);
			
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
				holder.itemView.findViewById(R.id.stats_layout).setVisibility(View.GONE);
				holder.itemView.findViewById(R.id.results_layout).setVisibility(View.VISIBLE);
				holder.itemView.findViewById(R.id.stroke_order).setVisibility((main.jishoHelper.offlineStrokeOrder() || main.jishoHelper.isInternetAvailable()) && StringHelper.isKanaOrKanji(main.queryText) ? View.VISIBLE : View.GONE);
				
				((TextView) holder.itemView.findViewById(R.id.results_text)).setText(main.vocabulary_filtered.size() + " results for search \"" + main.queryText + "\"");
			}
			else
			{
				holder.itemView.findViewById(R.id.stats_layout).setVisibility(View.VISIBLE);
				holder.itemView.findViewById(R.id.results_layout).setVisibility(View.GONE);
				
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

				TextView next_review = (TextView)holder.itemView.findViewById(R.id.text_next_review);
				next_review.setText(
					"Vocabularies to review now: " + review_now + "\n"
					+ "Vocabularies to review in the next hour: " + review_hour + "\n"
					+ "Vocabularies to review in the next day: " + review_day
				);

				TextView date_next_review = (TextView)holder.itemView.findViewById(R.id.date_next_review);
				date_next_review.setText("Next Review: " + (nextReview < System.currentTimeMillis() ? "Now" : nextReview == 0 ? "Never" : new SimpleDateFormat().format(new Date(nextReview))));
				
			}
		}
		else
		{
			position--;
			
			Vocabulary v = main.vocabulary_filtered.get(position);
			
			if ((main.queryText == null || main.queryText.isEmpty()) && newCategory(position, v))
			{
				TextView category = (TextView)holder.itemView.findViewById(R.id.category_text);
				category.setVisibility(View.VISIBLE);
				
				if (main.sortType == SortType.CATEGORY || main.sortType == SortType.CATEGORY_REVERSED)
				{
					category.setText(!v.learned ? "Not in learned vocabulary" : v.category == 0 ? "Critical vocabularies" : "Category " + v.category);
				}
				else if (main.sortType == SortType.TYPE)
				{
					category.setText(Vocabulary.types.get(v.type.ordinal()));
				}
				else if (main.sortType == SortType.NEXT_REVIEW)
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
				category.setVisibility(View.GONE);
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

			if (main.viewType == ViewType.LARGE)
			{
				kanji.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 50);
				reading.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
				meaning.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
			}
			else if (main.viewType == ViewType.MEDIUM)
			{
				kanji.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 35);
				reading.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
				meaning.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
			}
			else if (main.viewType == ViewType.SMALL)
			{
				kanji.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
				reading.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
				meaning.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
			}
			
			reading.setVisibility(v.reading.length > 0 ? View.VISIBLE : View.GONE);
			
			ImageView check = (ImageView)holder.itemView.findViewById(R.id.check);
			check.setVisibility(v.learned ? View.VISIBLE : View.GONE);
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
