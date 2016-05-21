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

public class Vocabulary implements Comparable<Vocabulary>
{
	public static final Random random = new Random();
	
	public final MainActivity main;
	
	public final String kanji;
	public final String[] reading;
	public final String[] reading_trimed;
	public final String[] meaning;
	public final String additionalInfo;
	public final String notes;
	
	public int streak_kanji, streak_kanji_best;
	public int streak_reading, streak_reading_best;
	public int streak_meaning, streak_meaning_best;
	
	public int timesChecked_kanji;
	public int timesChecked_reading;
	public int timesChecked_meaning;
	
	public int timesCorrect_kanji;
	public int timesCorrect_reading;
	public int timesCorrect_meaning;
	
	public final int[] meaning_used;
	public final int[] reading_used;
	
	public final int[] category_history = new int[32];
	
	public final ArrayList<Vocabulary> sameMeaning = new ArrayList<>();
	public final ArrayList<Vocabulary> sameReading = new ArrayList<>();
	
	public boolean learned;
	public boolean showInfo = true;
	
	public boolean answered_kanji;
	public boolean answered_reading;
	public boolean answered_meaning;
	public boolean answered_correct = true;
	
	public final VocabularyType type;
	
	public int category = 1;
	
	public long lastChecked;
	public long added;
	
	public String soundFile = "";
	public String imageFile = "";
	
	public Vocabulary(MainActivity main, VocabularyType type, String kanji, String[] reading, String[] meaning, String additionalInfo, String notes)
	{
		this.main = main;
		this.type = type;
		
		this.kanji = kanji;
		this.reading = reading.length == 1 && (reading[0] == null || reading[0].isEmpty()) ? new String[0] : reading;
		this.reading_used = new int[this.reading.length];
		this.reading_trimed = new String[this.reading.length];
		for (int i = 0; i < this.reading.length; ++i)
			this.reading_trimed[i] = reading[i].replace("・", "");
			
		this.meaning = meaning;
		this.meaning_used = new int[meaning.length];
		
		this.additionalInfo = additionalInfo;
		this.notes = notes;
		
		this.added = System.currentTimeMillis();
		
		this.category_history[category_history.length - 1] = category;
		for (int i = 0; i < category_history.length - 1; ++i)
			category_history[i] = -1;
	}
	
	public Vocabulary add(ArrayList<Vocabulary> vocabulary, ImportType type, Context context)
	{
		return add(vocabulary, vocabulary.size(), type, context);
	}
		
	public Vocabulary add(final ArrayList<Vocabulary> vocabulary, int position, ImportType importType, final Context context)
	{
		if (kanji.isEmpty() || meaning.length == 0)
		{
			return this;
		}
		
		if (vocabulary.contains(this))
		{
			if (importType == ImportType.KEEP)
			{
				return this;
			}
			else if (importType == ImportType.MERGE)
			{
				Vocabulary old = vocabulary.get(position = vocabulary.indexOf(this));
				old.remove(vocabulary);
				
				VocabularyType type = old.type == VocabularyType.NONE ? this.type : old.type;
				ArrayList<String> reading = new ArrayList<>();
				ArrayList<String> meaning = new ArrayList<>();
				ArrayList<Integer> reading_used = new ArrayList<>();
				ArrayList<Integer> meaning_used = new ArrayList<>();
				
				for (int i = 0; i < this.reading.length; ++i)
				{
					reading.add(this.reading[i]);
					reading_used.add(this.reading_used[i]);
				}
				
				int index;
				for (int i = 0; i < old.reading.length; ++i)
				{
					index = reading.indexOf(old.reading[i]);
					
					if (index == -1)
					{
						reading.add(old.reading[i]);
						reading_used.add(old.reading_used[i]);
					}
					else
					{
						reading_used.set(index, reading_used.get(index) + old.reading_used[i]);
					}
				}
				
				for (int i = 0; i < this.meaning.length; ++i)
				{
					meaning.add(this.meaning[i]);
					meaning_used.add(this.meaning_used[i]);
				}

				for (int i = 0; i < old.meaning.length; ++i)
				{
					index = meaning.indexOf(old.meaning[i]);

					if (index == -1)
					{
						meaning.add(old.meaning[i]);
						meaning_used.add(old.meaning_used[i]);
					}
					else
					{
						meaning_used.set(index, meaning_used.get(index) + old.meaning_used[i]);
					}
				}
				
				String additionalInfo_merged = additionalInfo + (!additionalInfo.isEmpty() && !old.additionalInfo.isEmpty() && ! old.additionalInfo.equalsIgnoreCase(additionalInfo) ? ", " : "") + (old.additionalInfo.equalsIgnoreCase(additionalInfo) ? "" : old.additionalInfo);
				String notes_merged = notes + (!notes.isEmpty() && !old.notes.isEmpty() && ! old.notes.equalsIgnoreCase(notes) ? ", " : "") + (old.notes.equalsIgnoreCase(notes) ? "" : old.notes);
				
				Vocabulary merged = new Vocabulary(main, type, old.kanji, reading.toArray(new String[reading.size()]), meaning.toArray(new String[meaning.size()]), additionalInfo_merged, notes_merged);
				
				for (int i = 0; i < merged.reading_used.length; ++i)
					merged.reading_used[i] = reading_used.get(i);
			
				for (int i = 0; i < merged.meaning_used.length; ++i)
					merged.meaning_used[i] = meaning_used.get(i);
				
				merged.learned = learned || old.learned;
				merged.category = Math.max(category, old.category);
					
				if (merged.category == category)
					System.arraycopy(category_history, 0, merged.category_history, 0, category_history.length);
				else
					System.arraycopy(old.category_history, 0, merged.category_history, 0, category_history.length);
				
				merged.timesChecked_kanji = timesCorrect_kanji + old.timesChecked_kanji;
				merged.timesChecked_reading = timesChecked_reading + old.timesChecked_reading;
				merged.timesChecked_meaning = timesChecked_meaning + old.timesChecked_meaning;
				merged.timesCorrect_kanji = timesCorrect_kanji + old.timesCorrect_kanji;
				merged.timesCorrect_reading = timesCorrect_reading + old.timesCorrect_reading;
				merged.timesCorrect_meaning = timesCorrect_meaning + old.timesCorrect_meaning;
				merged.streak_kanji = streak_kanji + old.streak_kanji;
				merged.streak_reading = streak_reading + old.streak_reading;
				merged.streak_meaning = streak_meaning + old.streak_meaning;
				merged.streak_kanji_best = Math.max(Math.max(streak_kanji, old.streak_kanji_best), streak_kanji_best);
				merged.streak_reading_best = Math.max(Math.max(streak_reading, old.streak_reading_best), streak_reading_best);
				merged.streak_meaning_best = Math.max(Math.max(streak_meaning, old.streak_meaning_best), streak_meaning_best);
				
				merged.showInfo = showInfo && old.showInfo;
				
				merged.added = Math.min(added, old.added);
				merged.lastChecked = Math.max(lastChecked, old.lastChecked);
				
				if (merged.category <= 1 && (float)(merged.timesCorrect_kanji + merged.timesCorrect_reading + merged.timesCorrect_meaning) / (merged.timesChecked_kanji + merged.timesChecked_reading + merged.timesChecked_meaning) < 0.75F)
				{
					merged.category = 0;
				}
				
				merged.imageFile = imageFile.isEmpty() ? old.imageFile : imageFile;
				
				merged.add(vocabulary, position, ImportType.REPLACE, context);
				
				return merged;
			}
			else if (importType == ImportType.REPLACE)
			{
				int old = vocabulary.indexOf(this);
				vocabulary.get(old).remove(vocabulary);
				
				position = old;
			}
			else if (importType == ImportType.REPLACE_KEEEP_STATS)
			{
				Vocabulary old = vocabulary.get(position = vocabulary.indexOf(this)).remove(vocabulary);

				learned = learned || old.learned;
				category = Math.max(category, old.category);
				timesChecked_kanji += old.timesChecked_kanji;
				timesChecked_reading += old.timesChecked_reading;
				timesChecked_meaning += old.timesChecked_meaning;
				timesCorrect_kanji += old.timesCorrect_kanji;
				timesCorrect_reading += old.timesCorrect_reading;
				timesCorrect_meaning += old.timesCorrect_meaning;
				streak_kanji += old.streak_kanji;
				streak_reading += old.streak_reading;
				streak_meaning += old.streak_meaning;
				streak_kanji_best = Math.max(Math.max(streak_kanji, old.streak_kanji_best), streak_kanji_best);
				streak_reading_best = Math.max(Math.max(streak_reading, old.streak_reading_best), streak_reading_best);
				streak_meaning_best = Math.max(Math.max(streak_meaning, old.streak_meaning_best), streak_meaning_best);
				answered_correct = true;
				answered_kanji = answered_reading = answered_meaning = false;
				
				int i, i1;
				for (i = 0; i < old.reading_trimed.length; ++i)
				{
					for (i1 = 0; i1 < reading_trimed.length; ++i1)
					{
						if (reading_trimed[i1].equalsIgnoreCase(old.reading_trimed[i]))
						{
							reading_used[i1] = old.reading_used[i];
						}
					}
				}
				
				for (i = 0; i < old.meaning.length; ++i)
				{
					for (i1 = 0; i1 < meaning.length; ++i1)
					{
						if (meaning[i1].equalsIgnoreCase(old.meaning[i]))
						{
							meaning_used[i1] = old.meaning_used[i];
						}
					}
				}
				
				added = Math.min(added, old.added);
				lastChecked = Math.max(lastChecked, old.lastChecked);
				
				if (category <= 1 && (float)(timesCorrect_kanji + timesCorrect_reading + timesCorrect_meaning) / (timesChecked_kanji + timesChecked_reading + timesChecked_meaning) < 0.75F)
				{
					category = 0;
				}
				
				System.arraycopy(old.category_history, 0, category_history, 0, category_history.length);
				
				old.remove(vocabulary);
			}
			else if (importType == ImportType.ASK)
			{
				final int position1 = position;
				
				AlertDialog alertDialog = new AlertDialog.Builder(context).create();
				alertDialog.setTitle("Conflict");
				alertDialog.setMessage("There already exists an vocabulary with the kanji \"" + kanji + "\"! What do you want to do?");
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Replace",
					new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							Vocabulary.this.add(vocabulary, position1, ImportType.REPLACE, context);
							dialog.dismiss();
						}
					});
				alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Keep",
					new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							Vocabulary.this.add(vocabulary, position1, ImportType.KEEP, context);
							dialog.dismiss();
						}
					});
				alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Merge",
					new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							Vocabulary.this.add(vocabulary, position1, ImportType.MERGE, context);
							dialog.dismiss();
						}
					});
				alertDialog.show();
				
				return this;
			}
		}
		
		for (Vocabulary v : vocabulary)
		{
			l1:
			for (String s : reading_trimed)
			{
				for (String s1 : v.reading_trimed)
				{
					if (s.equalsIgnoreCase(s1))
					{
						v.sameReading.add(this);
						this.sameReading.add(v);
						break l1;
					}
				}
			}
		
			l2:
			for (String s : v.meaning)
			{
				for (String s1 : this.meaning)
				{
					if (s.equalsIgnoreCase(s1))
					{
						v.sameMeaning.add(this);
						this.sameMeaning.add(v);
						break l2;
					}
				}
			}
		}
		
		vocabulary.add(position, this);
		
		if (vocabulary == main.vocabulary)
			if (main.show[type.ordinal()] && (main.showType == ShowType.ALL || main.showType == ShowType.LEARNED && learned || main.showType == ShowType.UNLEARNED && !learned))
				main.vocabulary_filtered.add(this);
		
		return this;
	}
	
	public Vocabulary remove(ArrayList<Vocabulary> vocabulary)
	{
		for (Vocabulary v : sameReading)
		{
			v.sameReading.remove(this);
		}
		
		for (Vocabulary v : sameMeaning)
		{
			v.sameMeaning.remove(this);
		}
		
		vocabulary.remove(this);
		
		if (vocabulary == main.vocabulary)
			main.vocabulary_filtered.remove(this);
		
		return this;
	}
	
	public void reset()
	{
		category = 1;
		
		category_history[category_history.length - 1] = category;
		for (int i = 0; i < category_history.length - 1; ++i)
			category_history[i] = -1;
			
		timesChecked_kanji = timesCorrect_kanji = streak_kanji =  0;
		timesChecked_reading = timesCorrect_reading = streak_reading = 0;
		timesChecked_meaning = timesCorrect_meaning = streak_meaning = 0;
		lastChecked = 0;
		
		for (int i = 0; i < reading_used.length; ++i)
			reading_used[i] = 0;
			
		for (int i = 0; i < meaning_used.length; ++i)
			meaning_used[i] = 0;
			
		answered_correct = true;
		answered_kanji = answered_reading = answered_meaning = false;
		learned = false;
		
		streak_kanji = streak_kanji_best = streak_reading = streak_reading_best = streak_meaning = streak_meaning_best = 0;
	}
	
	public Answer getAnswer(String answer, QuestionType type, QuestionType question)
	{
		answer = StringHelper.trim(answer);
		answer = answer.replace("・", "");

		if (type == QuestionType.READING)
			answer = StringHelper.toHiragana(answer);

		for (int i = 0; i < meaning.length; ++i)
			if (meaning[i].equalsIgnoreCase(answer) || StringHelper.similiarMeaning(meaning[i], answer))
			{
				if (type == QuestionType.MEANING)
					return meaning[i].equalsIgnoreCase(answer) ? Answer.CORRECT : Answer.SIMILIAR;
				else
					return Answer.RETRY;
			}

		for (int i = 0; i < reading_trimed.length; ++i)
			if (answer.equalsIgnoreCase(reading_trimed[i]))
				if (type == QuestionType.READING)
					return Answer.CORRECT;
				else
					return Answer.RETRY;

		if (answer.equalsIgnoreCase(kanji))
			if (type == QuestionType.KANJI)
				return Answer.CORRECT;
			else
				return Answer.RETRY;
		else if (type == QuestionType.KANJI && StringHelper.similiarKanji(kanji, answer))
			return Answer.SIMILIAR;

		if (question == QuestionType.MEANING || question == QuestionType.READING)
			for (Vocabulary v : question == QuestionType.READING ? sameReading : sameMeaning)
			{
				if (answer.equalsIgnoreCase(v.kanji))
					if (type == QuestionType.KANJI)
						return Answer.CORRECT;
					else
						return Answer.RETRY;
				else if (type == QuestionType.KANJI && StringHelper.similiarKanji(v.kanji, answer))
					return Answer.SIMILIAR;

				for (String s : v.reading_trimed)
					if (answer.equalsIgnoreCase(s))
						if (type == QuestionType.READING)
							return Answer.CORRECT;
						else
							return Answer.RETRY;

				for (String s : v.meaning)
					if (s.equalsIgnoreCase(answer) || StringHelper.similiarMeaning(s, answer))
						if (type == QuestionType.MEANING)
							return s.equalsIgnoreCase(answer) ? Answer.CORRECT : Answer.SIMILIAR;
						else
							return Answer.RETRY;
			}
			
		return Answer.WRONG;
	}
	
	public Answer answer(String answer, QuestionType type, QuestionType question)
	{
		answer = StringHelper.trim(answer);
		answer = answer.replace("・", "");
		
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
		
		if (answer.equalsIgnoreCase(kanji))
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
			for (Vocabulary v : question == QuestionType.READING ? sameReading : sameMeaning)
			{
				if (answer.equalsIgnoreCase(v.kanji))
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
				else if (type == QuestionType.KANJI && StringHelper.similiarKanji(v.kanji, answer))
				{
					timesChecked_kanji++;
					streak_kanji = 0;
					answered_correct = false;
					return Answer.SIMILIAR;
				}
				
				for (String s : v.reading_trimed)
					if (answer.equalsIgnoreCase(s))
					{
						if (type == QuestionType.READING)
						{
							streak_reading++;
							streak_reading_best = Math.max(streak_reading_best, streak_reading);
							timesChecked_reading++;
							timesCorrect_reading++;
							answered_reading = true;
							return Answer.CORRECT;
						}
						else
							return Answer.RETRY;
					}

				for (String s : v.meaning)
					if (s.equalsIgnoreCase(answer) || StringHelper.similiarMeaning(s, answer))
					{
						if (type == QuestionType.MEANING)
						{
							streak_meaning++;
							streak_meaning_best = Math.max(streak_meaning_best, streak_meaning);
							timesChecked_meaning++;
							timesCorrect_meaning++;
							answered_meaning = true;
							return s.equalsIgnoreCase(answer) ? Answer.CORRECT : Answer.SIMILIAR;
						}
						else
							return Answer.RETRY;
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
		
		category = getLastSavePoint();
			
		return Answer.WRONG;
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
			
			if (!additionalInfo.isEmpty() && (showInfo || type == QuestionType.MEANING_INFO))
				sb.append((type == QuestionType.MEANING_INFO ? "\n" : " ") + "(" + additionalInfo + ")");
				
			return sb.toString();
		}
		else if (type == QuestionType.READING)
		{
			if (reading.length == 0)
				return "";
			
			StringBuilder sb = new StringBuilder();
			
			for (int i = 0; i < reading.length - 1; ++i)
			{
				sb.append(reading[i]);
				sb.append(" / ");
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
			
			if (sameMeaning.isEmpty())
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
					for (int i = 0; i < meaning_used.length; ++i)
					{
						if (f < 1 - meaning_used[i] / meaning_used_sum)
						{
							sb.append(meaning[i]);
							break;
						}

						f -= 1 - meaning_used[i] / meaning_used_sum;
					}
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
		else if (type == QuestionType.READING)
		{
			if (reading.length == 0)
				return "";
			
			StringBuilder sb = new StringBuilder();

			if (sameReading.isEmpty())
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

			return sb.toString();
		}
		else if (type == QuestionType.KANJI)
		{
			return kanji;
		}

		return "*error*";
	}
	
	public int searchResult;
	
	public int searched(String s)
	{
		searchResult = 0;
		
		if (kanji.contains(s))
		{
			if (kanji.startsWith(s))
			{
				if (kanji.equalsIgnoreCase(s))
					searchResult++;
					
				searchResult++ ;
			}
			
			searchResult++;
		}
		
		if (additionalInfo.toLowerCase().contains(s))
		{
			if (additionalInfo.toLowerCase().startsWith(s))
			{
				if (additionalInfo.equalsIgnoreCase(s))
					searchResult++;

				searchResult++ ;
			}

			searchResult++;
		}
			
		for (int i = 0; i < reading.length; ++i)
		{
			if (reading_trimed[i].contains(s) || reading[i].contains(s))
			{
				if (reading_trimed[i].startsWith(s) || reading_trimed[i].startsWith(s))
				{
					if (reading_trimed[i].equalsIgnoreCase(s) || reading_trimed[i].equalsIgnoreCase(s))
						searchResult++;

					searchResult++ ;
				}

				searchResult++;
			}
		}
		
		for (String s1 : meaning)
		{
			if (s1.toLowerCase().contains(s))
			{
				if (s1.toLowerCase().startsWith(s))
				{
					if (s1.equalsIgnoreCase(s))
						searchResult++;

					searchResult++;
				}
				
				searchResult++;
			}
		}
		
		return searchResult;
	}
	
	public void prepareSound(OnProcessSuccessListener listener)
	{
		if ("-".equals(soundFile))
		{
			return;
		}
		
		if (main.jishoHelper.isInternetAvailable())
		{
			if (soundFile == null || soundFile.isEmpty())
				main.jishoHelper.playSoundFile(this, listener);
			
			else
				listener.onProcessSuccess();
		}
	}
	
	public void playSound()
	{
		if ("-".equals(soundFile))
		{
			return;
		}
		
		if (soundFile == null || soundFile.isEmpty())
		{
			main.jishoHelper.playSoundFile(this, new OnProcessSuccessListener()
			{
				@Override
				public void onProcessSuccess(Object... args)
				{
					playSound();
				}
			});
			return;
		}
		
		try
		{
			if (!main.jishoHelper.isInternetAvailable())
			{
				Toast.makeText(main, "No internet connection", Toast.LENGTH_SHORT).show();
				return;
			}
			
			MediaPlayer player = new MediaPlayer();
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
						((AudioManager) main.getSystemService(Context.AUDIO_SERVICE)).requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
						
						p1.start();
					}
			});
			player.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
			{
					@Override
					public void onCompletion(MediaPlayer p1)
					{
						p1.stop();
						p1.release();
						
						((AudioManager) main.getSystemService(Context.AUDIO_SERVICE)).abandonAudioFocus(listener);
					}
			});
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void showStrokeOrder()
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
	
		showStrokeOrder(layout_kanji, null, false, false);
		
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
	
	public void showStrokeOrder(final LinearLayout add, final OnProcessSuccessListener listener, boolean accent, boolean horizontal)
	{
		if (!main.jishoHelper.offlineStrokeOrder() && !main.jishoHelper.isInternetAvailable())
		{
			Toast.makeText(main, "No internet connection", Toast.LENGTH_SHORT).show();
			return;
		}

		char[] kanji = new char[this.kanji.length()];
		this.kanji.getChars(0, this.kanji.length(), kanji, 0);
		String[] hex = new String[kanji.length];

		for (int i = 0; i < kanji.length; ++i)
		{
			hex[i] = Integer.toHexString(kanji[i]);
			while (hex[i].length() < 5)
			{
				hex[i] = "0" + hex[i];
			}
		}
		
		final int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, main.getResources().getDisplayMetrics());
		final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, !horizontal ? LinearLayout.LayoutParams.FILL_PARENT : size);
		params.gravity = Gravity.CENTER;

		main.jishoHelper.createStrokeOrderView(hex, new OnProcessSuccessListener()
			{

				@Override
				public void onProcessSuccess(Object... args)
				{
					if (listener != null)
					{
						listener.onProcessSuccess();
					}
					
					add.addView((View) args[0], params);
				}
			}, accent, horizontal);
	}
	
	public static final ArrayList<String> types = new ArrayList<>();
	public static final ArrayList<String> types_import = new ArrayList<>();
	public static final ArrayList<String> types_sort = new ArrayList<>();
	public static final ArrayList<String> types_view = new ArrayList<>();
	public static final ArrayList<String> types_show = new ArrayList<>();
	
	
	static
	{
		types.add("None");
		types.add("Other");
		types.add("Noun");
		types.add("I-adjective");
		types.add("Na-adjective");
		types.add("Ru-Verb");
		types.add("U-Verb");
		types.add("Adverb");
		types.add("Expression");
		types.add("Particle");
		types.add("Conjunction");
		
		types_import.add("Merge");
		types_import.add("Replace old");
		types_import.add("Replace old, but keep stats");
		types_import.add("Keep old");
		types_import.add("Ask every time");
		
		types_sort.add("Category");
		types_sort.add("Category (Reversed)");
		types_sort.add("Date added");
		types_sort.add("Date added (Reversed)");
		types_sort.add("Vocabulary type");
		types_sort.add("Next review");
		
		types_view.add("Large");
		types_view.add("Medium");
		types_view.add("Small");
		
		types_show.add("All");
		types_show.add("Learned vocabularies");
		types_show.add("Other vocabularies");
	}
	
	public String getType()
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
	
	public long getNextReview()
	{
		if (!learned)
			return 0;
		
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
			default:
				return 1000L * 60 * 60 * 24 * 356;
				
		}
	}
	
	public int getLastSavePoint()
	{
		if (!PreferenceManager.getDefaultSharedPreferences(main).getBoolean("savePoint", true) || (category <= 3))
		{
			if ((float)(timesCorrect_kanji + timesCorrect_reading + timesCorrect_meaning) / (timesChecked_kanji + timesChecked_reading + timesChecked_meaning) < 0.75F)
				return 0;

			return 1;
		}
		else if (category > 6)
			return 6;
			
		else if (category > 9)
			return 9;

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
	
	@Override
	public int compareTo(Vocabulary p1)
	{
		switch (main.sortType)
		{
			case CATEGORY_REVERSED:
				return p1.learned ? (this.learned ? (category < p1.category ? 1 : (category == p1.category ? 0 : -1)) : 1) : (this.learned ? -1 : 0);
			case TIME_ADDED:
				return (int)Math.signum(p1.added - added);
			case TIME_ADDED_REVERSED:
				return (int)Math.signum(added - p1.added);
			case TYPE:
				return (int)Math.signum(type.ordinal() - p1.type.ordinal());
			case NEXT_REVIEW:
				return p1.learned ? (this.learned ? (lastChecked + getNextReview() < p1.lastChecked + p1.getNextReview() ? -1 : (lastChecked + getNextReview() == p1.lastChecked + p1.getNextReview() ? 0 : 1)) : 1) : (this.learned ? -1 : 0);
			default:
				return p1.learned ? (this.learned ? (category < p1.category ? -1 : (category == p1.category ? 0 : 1)) : 1) : (this.learned ? -1 : 0);
		}
	}

	public enum Type_old
	{
		NONE,
		I_ADJECTIVE,
		NA_ADJECTIVE,
		NOUN,
		RU_VERB,
		U_VERB,
		ADVERB,
		EXPRESSION,
		PARTICLE,
		OTHER
	}
	
	public static int getOldType(int ordinal)
	{
		return VocabularyType.valueOf(Type_old.values()[ordinal].name()).ordinal();
	}
}
