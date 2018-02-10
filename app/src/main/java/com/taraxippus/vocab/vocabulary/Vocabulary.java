package com.taraxippus.vocab.vocabulary;

import android.app.*;
import android.content.*;
import android.media.*;
import android.preference.*;
import android.view.*;
import android.widget.*;
import com.taraxippus.vocab.*;
import com.taraxippus.vocab.util.*;
import java.util.*;
import android.graphics.drawable.*;
import android.util.*;
import android.widget.ActionMenuView.*;

public class Vocabulary
{
	public static final Random random = new Random();
	public static MediaPlayer mediaPlayer;
	
	public int id;
	public String kanji;
	public String[] reading;
	public String[] reading_trimed;
	public String[] meaning;
	public String additionalInfo;
	public String notes;
	
	public int streak_kanji, streak_kanji_best;
	public int streak_reading, streak_reading_best;
	public int streak_meaning, streak_meaning_best;
	
	public int timesChecked_kanji;
	public int timesChecked_reading;
	public int timesChecked_meaning;
	
	public int timesCorrect_kanji;
	public int timesCorrect_reading;
	public int timesCorrect_meaning;
	
	public int[] meaning_used;
	public int[] reading_used;
	
	public int[] category_history;
	
	public int[] sameReading;
	public int[] sameMeaning;
	
	public boolean learned;
	public boolean showInfo;
	
	public boolean answered_kanji;
	public boolean answered_reading;
	public boolean answered_meaning;
	public boolean answered_correct = true;
	private int lastAnswer;
	
	public VocabularyType type;
	
	public int category;
	
	public long lastChecked;
	public long added;
	public long nextReview;
	
	public ReviewType kanjiReview = ReviewType.NORMAL;
	public ReviewType readingReview = ReviewType.NORMAL;
	public ReviewType meaningReview = ReviewType.NORMAL;
	public boolean quickReview;
	
	public String soundFile;
	public String imageFile;
	
	public Vocabulary(int id)
	{
		this.id = id;
	}
	
	public Answer getAnswer(DBHelper dbHelper, String answer, QuestionType type, QuestionType question)
	{
		answer = StringHelper.trim(answer);
		answer = answer.replace("・", "");
		if (reading.length == 0 && type == QuestionType.KANJI || type == QuestionType.READING)
			answer = answer.replace("一", "ー").replace("二", "ニ").replace("才", "オ").replace("口", "ロ").replace("夕", "タ").replace("力", "カ").replace("工", "エ");

		if (type == QuestionType.READING)
			answer = StringHelper.toHiragana(answer);

		lastAnswer = 0;
			
		for (int i = 0; i < meaning.length; ++i)
			if (meaning[i].equalsIgnoreCase(answer) || StringHelper.similiarMeaning(meaning[i], answer))
			{
				lastAnswer = i;
				
				if (type == QuestionType.MEANING)
					return meaning[i].equalsIgnoreCase(answer) ? Answer.CORRECT : Answer.SIMILIAR;
				else
					return Answer.RETRY;
			}

		for (int i = 0; i < reading_trimed.length; ++i)
			if (answer.equalsIgnoreCase(reading_trimed[i]))
			{
				lastAnswer = i;
				
				if (type == QuestionType.READING)
					return Answer.CORRECT;
				else
					return Answer.RETRY;
			}
				
		if (answer.equalsIgnoreCase(kanji) || reading.length == 0 && StringHelper.equalsKana(answer, kanji))
			if (type == QuestionType.KANJI)
				return Answer.CORRECT;
			else
				return Answer.RETRY;
				
		else if (type == QuestionType.KANJI && StringHelper.similiarKanji(kanji, answer))
			return Answer.SIMILIAR;

		if (question == QuestionType.MEANING || question == QuestionType.READING)
		{
			String kanji;
			
			for (int id : question == QuestionType.READING ? sameReading : sameMeaning)
			{
				kanji = dbHelper.getString(id, "kanji");
				if (kanji == null)
					continue;
					
				if (answer.equalsIgnoreCase(kanji))
					if (type == QuestionType.KANJI)
						return Answer.DIFFERENT;
					else
						return Answer.RETRY;
				else if (type == QuestionType.KANJI && StringHelper.similiarKanji(kanji, answer))
					return Answer.DIFFERENT;

				for (String s : dbHelper.getStringArray(id, "reading_trimed"))
					if (answer.equalsIgnoreCase(s))
						if (type == QuestionType.READING)
							return Answer.DIFFERENT;
						else
							return Answer.RETRY;

				for (String s : dbHelper.getStringArray(id, "meaning"))
					if (s.equalsIgnoreCase(answer) || StringHelper.similiarMeaning(s, answer))
						if (type == QuestionType.MEANING)
							return Answer.DIFFERENT;
						else
							return Answer.RETRY;
			}
		}
			
		return Answer.WRONG;
	}
	
	public Answer answer(DBHelper dbHelper, Context context, String answer, QuestionType type, QuestionType question)
	{
		answer = StringHelper.trim(answer);
		answer = answer.replace("・", "");
		if (reading.length == 0 && type == QuestionType.KANJI || type == QuestionType.READING)
			answer = answer.replace("一", "ー").replace("二", "ニ").replace("才", "オ").replace("口", "ロ").replace("夕", "タ").replace("力", "カ").replace("工", "エ");
		
		if (type == QuestionType.READING)
			answer = StringHelper.toHiragana(answer);
			
		for (int i = 0; i < meaning.length; ++i)
			if (meaning[i].equalsIgnoreCase(answer) || StringHelper.similiarMeaning(meaning[i], answer))
			{
				if (type == QuestionType.MEANING)
				{
					streak_meaning++;
					streak_meaning_best = Math.max(streak_meaning_best, streak_meaning);
					timesChecked_meaning++;
					timesCorrect_meaning++;
					answered_meaning = true;
					meaning_used[i]++;
					return meaning[i].equalsIgnoreCase(answer) ? Answer.CORRECT : Answer.SIMILIAR;
				}
				else
					return Answer.RETRY;
			}

		for (int i = 0; i < reading_trimed.length; ++i)
			if (answer.equalsIgnoreCase(reading_trimed[i]))
			{
				if (type == QuestionType.READING)
				{
					streak_reading++;
					streak_reading_best = Math.max(streak_reading_best, streak_reading);
					timesChecked_reading++;
					timesCorrect_reading++;
					answered_reading = true;
					reading_used[i]++;
					return Answer.CORRECT;
				}
				else
					return Answer.RETRY;
			}
		
		if (answer.equalsIgnoreCase(kanji) || reading.length == 0 && StringHelper.equalsKana(answer, kanji))
		{
			if (type == QuestionType.KANJI)
			{
				streak_kanji++;
				streak_kanji_best = Math.max(streak_kanji_best, streak_kanji);
				timesChecked_kanji++;
				timesCorrect_kanji++;
				answered_kanji = true;
				return Answer.CORRECT;
			}
			else
				return Answer.RETRY;
		}
		else if (type == QuestionType.KANJI && StringHelper.similiarKanji(kanji, answer))
		{
			timesChecked_kanji++;
			streak_kanji = 0;
			answered_correct = false;
			return Answer.SIMILIAR;
		}

		if (question == QuestionType.MEANING || question == QuestionType.READING)
		{
			String kanji;
			
			for (int id : question == QuestionType.READING ? sameReading : sameMeaning)
			{
				kanji = dbHelper.getString(id, "kanji");
				if (answer.equalsIgnoreCase(kanji))
				{
					if (type == QuestionType.KANJI)
						return Answer.DIFFERENT;
					else
						return Answer.RETRY;
				}
				else if (type == QuestionType.KANJI && StringHelper.similiarKanji(kanji, answer))
					return Answer.DIFFERENT;

				for (String s : dbHelper.getStringArray(id, "reading_trimed"))
					if (answer.equalsIgnoreCase(s))
					{
						if (type == QuestionType.READING)
							return Answer.DIFFERENT;
						else
							return Answer.RETRY;
					}

				for (String s : dbHelper.getStringArray(id, "meaning"))
					if (s.equalsIgnoreCase(answer) || StringHelper.similiarMeaning(s, answer))
					{
						if (type == QuestionType.MEANING)
							return Answer.DIFFERENT;
						else
							return Answer.RETRY;
					}
			}
		}
		
		if (type == QuestionType.MEANING)
		{
			timesChecked_meaning++;
			streak_meaning = 0;
		}
		else if (type == QuestionType.READING)
		{
			timesChecked_reading++;
			streak_reading = 0;
		}
		else if (type == QuestionType.KANJI)
		{
			timesChecked_kanji++;
			streak_kanji = 0;
		}
		
		answered_correct = false;
		category = getLastSavePoint(context);
			
		return Answer.WRONG;
	}

	public String correctAnswer(QuestionType type)
	{
		return correctAnswer(type, kanji, reading, meaning, additionalInfo, showInfo);
	}
	
	public static String correctAnswer(QuestionType type, String kanji, String[] reading, String[] meaning, String additionalInfo, boolean showInfo)
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
			
			if (!additionalInfo.isEmpty() && (showInfo || type == QuestionType.MEANING_INFO))
				sb.append((type == QuestionType.MEANING_INFO ? "\n" : " ") + "(" + additionalInfo + ")");
				
			return sb.toString();
		}
		else if (type == QuestionType.READING || type == QuestionType.READING_INFO)
		{
			if (reading.length == 0)
				return kanji;
			
			StringBuilder sb = new StringBuilder();
			
			for (int i = 0; i < reading.length - 1; ++i)
			{
				sb.append(reading[i]);
				sb.append(type == QuestionType.READING_INFO ? "\n" : " / ");
			}
			
			sb.append(reading[reading.length - 1]);
			
			return sb.toString();
		}
		else if (type == QuestionType.KANJI)
		{
			return kanji;
		}
		
		return "*error*";
	}

	public String question(QuestionType type)
	{
		if (type == QuestionType.MEANING || type == QuestionType.MEANING_INFO)
		{
			if (meaning.length == 0)
				return "";
			
			StringBuilder sb = new StringBuilder();
			
			if (sameMeaning.length == 0)
			{
				float meaning_used_sum = 0;
				for (int i : meaning_used)
					meaning_used_sum += i;
					
				if (meaning.length == 1 || meaning_used_sum == 0 || random.nextBoolean())
				{
					sb.append(meaning[0]);
				}
				else
				{
					float f = random.nextFloat();
					int i;
					for (i = 0; i < meaning_used.length; ++i)
					{
						if (f > meaning_used[i] / meaning_used_sum)
						{
							sb.append(meaning[i]);
							i = -1;
							break;
						}

						f += meaning_used[i] / meaning_used_sum;
					}
					
					if (i != -1)
						sb.append(meaning[meaning.length - 1]);
				}
			}
			else
			{
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
			}
			
			if (!additionalInfo.isEmpty() && (showInfo || type == QuestionType.MEANING_INFO))
				sb.append(" (" + additionalInfo + ")");

			return sb.toString();
		}
		else if (type == QuestionType.READING || type == QuestionType.READING_INFO)
		{
			if (reading.length == 0)
				return "";
			
			StringBuilder sb = new StringBuilder();

			if (sameReading.length == 0)
				sb.append(reading[random.nextInt(reading.length)]);
				
			else
			{
				for (int i = 0; i < reading.length - 1; ++i)
				{
					sb.append(reading[i]);
					sb.append(" / ");
				}

				sb.append(reading[reading.length - 1]);
			}

			return sb.toString().replace("・", "");
		}
		else if (type == QuestionType.KANJI)
		{
			return kanji;
		}

		return "*error*";
	}
	
	public String makeSuggestion(QuestionType type, String answer)
	{
		answer = StringHelper.trim(answer);
		answer = answer.replace("・", "");
		
		if ((type == QuestionType.MEANING || type == QuestionType.MEANING_INFO) && meaning.length > 1)
		{
			float average;
			int sum = 0, max = 0, min = 10000, indexMin = 0;
			for (int i = 0; i < meaning_used.length; ++i)
			{
				sum += meaning_used[i];
				if (meaning_used[i] > max)
					max = meaning_used[i];
				if (meaning_used[i] < min)
				{
					min = meaning_used[i];
					indexMin = i;
				}
			}
			
			if (lastAnswer == indexMin)
				return "";
			
			average = (float) sum / meaning_used.length;
			if (min <= 2 && average > 3 || max - min > average || average - min > 2)
				return "\nAlso try: " + meaning[indexMin] + (showInfo && !additionalInfo.isEmpty() ? " (" + additionalInfo + ")" : "");
			else if (max - average > 3)
				return "\nAll solutions are: " + correctAnswer(type);
		}
		else if ((type == QuestionType.READING || type == QuestionType.READING_INFO) && reading.length > 1)
		{
			answer = StringHelper.toHiragana(answer);
			
			float average;
			int sum = 0, max = 0, min = 10000, indexMin = 0;
			for (int i = 0; i < reading_used.length; ++i)
			{
				sum += reading_used[i];
				if (reading_used[i] > max)
					max = reading_used[i];
				if (reading_used[i] < min)
				{
					min = reading_used[i];
					indexMin = i;
				}
			}

			if (lastAnswer == indexMin)
				return "";
			
			average = (float) sum / reading_used.length;
			if (min <= 2 && average > 3 || max - min > average || average - min > 2)
				return "\nAlso try: " + reading[indexMin];
			else if (max - average > 3)
				return "\nAll solutions are: " + correctAnswer(type);
		}
		
		
		return "";
	}
	
	public void prepareSound(final DBHelper dbHelper, OnProcessSuccessListener listener)
	{
		if ("-".equals(soundFile))
		{
			return;
		}
		
		if (JishoHelper.isInternetAvailable(dbHelper.context))
		{
			if (soundFile == null || soundFile.isEmpty())
				JishoHelper.findSoundFile(dbHelper, this, listener);
			
			else
				listener.onProcessSuccess();
		}
	}
	
	public void playSound(final DBHelper dbHelper)
	{
		if ("-".equals(soundFile))
			return;

		if (soundFile == null || soundFile.isEmpty() || soundFile.contains("\""))
		{
			JishoHelper.findSoundFile(dbHelper, this, new OnProcessSuccessListener()
				{
					@Override
					public void onProcessSuccess(Object... args)
					{
						playSound(dbHelper);
					}
				});
			return;
		}
		
		playSound(dbHelper.context, soundFile);
	}
	
	public static void playSound(final Context context, final String soundFile)
	{
		if (soundFile == null || soundFile.isEmpty() ||"-".equals(soundFile))
			return;
		
		try
		{
			if (!JishoHelper.isInternetAvailable(context))
			{
				Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
				return;
			}
			
			if (mediaPlayer != null)
				return;
				
			MediaPlayer player = new MediaPlayer();
			mediaPlayer = player;
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setDataSource(soundFile);
			player.prepareAsync();
			
			final AudioManager.OnAudioFocusChangeListener listener = new AudioManager.OnAudioFocusChangeListener()
			{
				@Override
				public void onAudioFocusChange(int p1)
				{
					
				}
			};
			
			player.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
			{
					@Override
					public void onPrepared(MediaPlayer p1)
					{
						((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
						
						p1.start();
					}
			});
			player.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
			{
					@Override
					public void onCompletion(MediaPlayer p1)
					{
						((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).abandonAudioFocus(listener);
						
						p1.stop();
						p1.release();
						mediaPlayer = null;
					}
			});
			player.setOnErrorListener(new MediaPlayer.OnErrorListener()
			{
					@Override
					public boolean onError(MediaPlayer p1, int p2, int p3)
					{
						System.err.println("Mediaplayer error: " + p2 + "/" + p3 + " (" + soundFile + ")");
						return false;
					}
			});
		}
		catch (Exception e)
		{
			System.err.println("Mediaplayer error (" + soundFile + ")");
			
			e.printStackTrace();
		}
	}
	
	public void setSynonyms(ArrayList<Integer> sameReading, ArrayList<Integer> sameMeaning)
	{
		this.sameReading = new int[sameReading.size()];
		this.sameMeaning = new int[sameMeaning.size()];
		
		int i;
		for (i = 0; i < sameReading.size(); ++i)
			this.sameReading[i] = sameReading.get(i);
			
		for (i = 0; i < sameMeaning.size(); ++i)
			this.sameMeaning[i] = sameMeaning.get(i);
	}
	
	public static final ArrayList<String> types = new ArrayList<>();
	public static final ArrayList<String> types_import = new ArrayList<>();
	public static final ArrayList<String> types_sort = new ArrayList<>();
	public static final ArrayList<String> types_sort_kanji = new ArrayList<>();
	public static final ArrayList<String> types_view = new ArrayList<>();
	public static final ArrayList<String> types_show = new ArrayList<>();
	public static final ArrayList<String> types_hide = new ArrayList<>();
	public static final ArrayList<String> types_review = new ArrayList<>();

	static
	{
		types.add("None");
		types.add("Other");
		types.add("Noun");
		types.add("I-Adjective");
		types.add("Na-Adjective");
		types.add("Ru-Verb");
		types.add("U-Verb");
		types.add("Adverb");
		types.add("Expression");
		types.add("Particle");
		types.add("Conjunction");
		types.add("Counter");
		
		types_import.add("Merge");
		types_import.add("Replace old");
		types_import.add("Replace old, but keep stats");
		types_import.add("Keep old");
		types_import.add("Ask every time");
		
		types_sort.add("Category");
		types_sort.add("Date added");
		types_sort.add("Vocabulary type");
		types_sort.add("Next review");
		
		types_sort_kanji.add("Category");
		types_sort_kanji.add("Date added");
		types_sort_kanji.add("Stroke count");
		types_sort_kanji.add("Next review");
		
		types_view.add("Large");
		types_view.add("Medium");
		types_view.add("Small");
		
		types_show.add("All");
		types_show.add("Learned");
		types_show.add("Not yet learned");
		
		types_hide.add("Nothing");
		types_hide.add("Kanji");
		types_hide.add("Reading");
		types_hide.add("Meaning");
		
		types_review.add("Don't Review");
		types_review.add("Normal");
		types_review.add("Fast");
		types_review.add("Mixed");
	}
	
	public String getType()
	{
		return getType(type, kanji);
	}
	
	public static String getType(VocabularyType type, String kanji)
	{
		switch (type)
		{
			case NOUN:
				return "Noun";
			case I_ADJECTIVE:
				return "I-adjective";
			case NA_ADJECTIVE:
				return "Na-adjective";
			case RU_VERB:
				return "Ru-verb";
			case U_VERB:
				return "U-verb with " + StringHelper.toRomaji(kanji.charAt(kanji.length() - 1)) + " ending";
			case ADVERB:
				return "Adverb";
			case EXPRESSION:
				return "Expression";
			case PARTICLE:
				return "Particle";
			case CONJUNCTION:
				return "Conjunction";
			default:
				return "";
		}
	}

	public static long getNextReview(int category)
	{
		switch (category)
		{
			case 0:
				return 1000L * 60 * 30;
			case 1:
				return 1000L * 60 * 60;
			case 2:
				return 1000L * 60 * 60 * 3;
			case 3:
				return 1000L * 60 * 60 * 7;
			case 4:
				return 1000L * 60 * 60 * 11;
			case 5:
				return 1000L * 60 * 60 * 25;
			case 6:
				return 1000L * 60 * 60 * (24 * 3 + 1);
			case 7:
				return 1000L * 60 * 60 * (24 * 5 + 7);
			case 8:
				return 1000L * 60 * 60 * (24 * 7 + 5);
			case 9:
				return 1000L * 60 * 60 * (24 * 14 + 3);
			case 10:
				return 1000L * 60 * 60 * 24 * 30;
			case 11:
			case 12:
				return 1000L * 60 * 60 * 24 * (30 * 2 + 1);
			case 13:
				return 1000L * 60 * 60 * (24 * (30 * 4 + 2) + 2);
			case 14:
				return 1000L * 60 * 60 * (24 * (30 * 6 + 5) + 3);
			case 15:
				return 1000L * 60 * 60 * (24 * (30 * 9 + 7) + 5);
			case 16:
				return 1000L * 60 * 60 * 24 * 365;
			default:
				return 1000L * 60 * 60 * 24 * 365 * 2;
				
		}
	}
	
	public int getLastSavePoint(Context context)
	{
		if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("savePoint", true) || (category <= 3))
		{
			if ((float)(timesCorrect_kanji + timesCorrect_reading + timesCorrect_meaning) / (timesChecked_kanji + timesChecked_reading + timesChecked_meaning) < 0.75F)
				return 0;

			return 1;
		}
		else if (category > 9)
			return 9;
		
		else if (category > 6)
			return 6;
			
		else
			return 3;
	}

	public float getSuccessRate(QuestionType q)
	{
		switch (q)
		{
			case KANJI:
				return timesCorrect_kanji / (float) timesChecked_kanji;
			case READING:
			case READING_INFO:
				return timesCorrect_reading / (float) timesChecked_reading;
			case MEANING:
			case MEANING_INFO:
				return timesCorrect_meaning / (float) timesChecked_meaning;
			default:
				return 1;
		}
	}
	
	@Override
	public boolean equals(Object o)
	{
		return o instanceof Vocabulary && ((Vocabulary) o).kanji.equalsIgnoreCase(kanji);
	}
}
