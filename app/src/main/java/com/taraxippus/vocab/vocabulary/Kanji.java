package com.taraxippus.vocab.vocabulary;

public class Kanji
{
	public int id;
	public char kanji;
	public String[] reading_kun;
	public String[] reading_on;
	public String[] meaning;

	public String notes;
	public int strokes;
	
	public int streak_kanji, streak_kanji_best;
	public int streak_reading_kun, streak_reading_kun_best;
	public int streak_reading_on, streak_reading_on_best;
	public int streak_meaning, streak_meaning_best;

	public int timesChecked_kanji;
	public int timesChecked_reading_kun;
	public int timesChecked_reading_on;
	public int timesChecked_meaning;

	public int timesCorrect_kanji;
	public int timesCorrect_reading_kun;
	public int timesCorrect_reading_on;
	public int timesCorrect_meaning;

	public int[] timesChecked_reading;
	public int[] timesCorrect_reading;
	
	public int[] meaning_used;
	public int[] category_history;
	public int[] vocabularies;
	
	public boolean learned;
	
	public boolean answered_kanji;
	public boolean answered_reading_kun;
	public boolean answered_reading_on;
	public boolean answered_meaning;
	public boolean answered_correct = true;

	public int category;

	public long lastChecked;
	public long added;
	public long nextReview;

	public String imageFile;
	
	public Kanji(int id)
	{
		this.id = id;
	}
	
	public String correctAnswer(QuestionType type)
	{
		if (type == QuestionType.MEANING || type == QuestionType.MEANING_INFO)
		{
			if (meaning.length == 0)
				return "";

			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < meaning.length - 2; ++i)
			{
				sb.append(meaning[i]);
				sb.append(", ");
			}

			if (meaning.length >= 2)
			{
				sb.append(meaning[meaning.length - 2]);
				sb.append(" or ");
			}

			sb.append(meaning[meaning.length - 1]);
			return sb.toString();
		}
		else if (type == QuestionType.READING_KUN)
		{
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < reading_kun.length - 1; ++i)
			{
				sb.append(reading_kun[i]);
				sb.append(" 、");
			}

			sb.append(reading_kun[reading_kun.length - 1]);

			return sb.toString();
		}
		else if (type == QuestionType.READING_ON)
		{
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < reading_on.length - 1; ++i)
			{
				sb.append(reading_on[i]);
				sb.append(" 、");
			}

			sb.append(reading_on[reading_on.length - 1]);

			return sb.toString();
		}
		else if (type == QuestionType.KANJI)
		{
			return "" + kanji;
		}	
	
		return "*error*";
	}
}
