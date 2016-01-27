package com.taraxippus.vocab.vocabulary;

import android.app.*;
import android.content.*;
import com.taraxippus.vocab.*;
import java.nio.*;
import java.util.*;

public class Vocabulary implements Comparable<Vocabulary>
{
	public static final Random random = new Random();
	
	public final MainActivity main;
	
	public final String kanji;
	public final String[] reading;
	public final String[] reading_trimed;
	public final String[] meaning;
	public final String additionalInfo;
	
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
	
	public final ArrayList<Vocabulary> sameMeaning = new ArrayList<>();
	public final ArrayList<Vocabulary> sameReading = new ArrayList<>();
	
	public boolean learned;
	public boolean showInfo = true;
	
	public boolean answered_kanji;
	public boolean answered_reading;
	public boolean answered_meaning;
	public boolean answered_correct = true;
	
	public final Type type;
	
	public int category = 1;
	
	public long lastChecked;
	public long added;
	
	public Vocabulary(MainActivity main, Type type, String kanji, String[] reading, String[] meaning, String additionalInfo)
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
		
		this.added = System.currentTimeMillis();
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
				
				Type type = old.type == Type.NONE ? this.type : old.type;
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
				
				Vocabulary merged = new Vocabulary(main, type, old.kanji, reading.toArray(new String[reading.size()]), meaning.toArray(new String[meaning.size()]), additionalInfo + (!additionalInfo.isEmpty() && !old.additionalInfo.isEmpty() && ! old.additionalInfo.equalsIgnoreCase(additionalInfo) ? ", " : "") + (old.additionalInfo.equalsIgnoreCase(additionalInfo) ? "" : old.additionalInfo));
				
				for (int i = 0; i < merged.reading_used.length; ++i)
					merged.reading_used[i] = reading_used.get(i);
			
				for (int i = 0; i < merged.meaning_used.length; ++i)
					merged.meaning_used[i] = meaning_used.get(i);
				
				merged.learned = learned || old.learned;
				merged.category = Math.max(category, old.category);
					
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
				
				added = Math.min(added, old.added);
				lastChecked = Math.max(lastChecked, old.lastChecked);
				
				if (category <= 1 && (float)(timesCorrect_kanji + timesCorrect_reading + timesCorrect_meaning) / (timesChecked_kanji + timesChecked_reading + timesChecked_meaning) < 0.75F)
				{
					category = 0;
				}
				
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
			if (main.show[type.ordinal()] && (main.showType == Vocabulary.ShowType.ALL || main.showType == Vocabulary.ShowType.LEARNED && learned || main.showType == Vocabulary.ShowType.UNLEARNED && !learned))
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
	
	public Answer answer(String answer, QuestionType type, QuestionType question)
	{
		answer = trim(answer);
		answer = answer.replace("・", "");
		
		if (type == QuestionType.READING)
			answer = toHiragana(answer);
			
		for (int i = 0; i < meaning.length; ++i)
			if (meaning[i].equalsIgnoreCase(answer) || similiarMeaning(meaning[i], answer))
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
		else if (type == QuestionType.KANJI && similiarKanji(kanji, answer))
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
				else if (type == QuestionType.KANJI && similiarKanji(v.kanji, answer))
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
					if (s.equalsIgnoreCase(answer) || similiarMeaning(s, answer))
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
				sb.append(meaning[random.nextInt(meaning.length)]);
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
			
			if (!additionalInfo.isEmpty() && (showInfo || type ==QuestionType.MEANING_INFO))
				sb.append(" (" + additionalInfo + ")");

			return sb.toString();
		}
		else if (type == QuestionType.READING)
		{
			if (reading.length == 0)
				return "";
			
			StringBuilder sb = new StringBuilder();

			if (sameReading.isEmpty())
			{
				sb.append(reading[random.nextInt(reading.length)]);
			}
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
	
	public boolean searched(String s)
	{
		s = s.toLowerCase();
		
		if (kanji.toLowerCase().contains(s) || additionalInfo.toLowerCase().contains(s))
			return true;
		
		for (int i = 0; i < reading.length; ++i)
		{
			if (reading_trimed[i].toLowerCase().contains(s) || reading[i].toLowerCase().contains(s))
				return true;
		}
		
		for (String s1 : meaning)
		{
			if (s1.toLowerCase().contains(s))
				return true;
		}
		
		return false;
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
				return "U-verb with " + toRomaji(kanji.charAt(kanji.length() - 1)) + " ending";
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
	
	public String toRomaji(char hiragana)
	{
		switch (hiragana)
		{
			case 'す':
				return "su";
			case 'く':
				return "ku";
			case 'ぐ':
				return "gu";
			case 'む':
				return "mu";
			case 'ぶ':
				return "bu";
			case 'ぬ':
				return "nu";
			case 'る':
				return "ru";
			case 'う':
				return "u";
			case 'つ':
				return "tsu";
			default:
				return "" + hiragana;
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
				return 1000L * 60 * 60 * 6;
			case 4:
				return 1000L * 60 * 60 * 6;
			case 5:
				return 1000L * 60 * 60 * 24;
			case 6:
				return 1000L * 60 * 60 * 24 * 3;
			case 7:
				return 1000L * 60 * 60 * 24 * 7;
				
			default:
				return 1000L * 60 * 60 * 24 * 30;
				
		}
	}
	
	public int getLastSavePoint()
	{
		if (category > 6)
		{
			return 6;
		}
		else if (category > 3)
		{
			return 3;
		}
		else
		{
			if ((float)(timesCorrect_kanji + timesCorrect_reading + timesCorrect_meaning) / (timesChecked_kanji + timesChecked_reading + timesChecked_meaning) < 0.75F)
				return 0;
			
			return 1;
		}
	}
	
	public int getBytes()
	{
		int bytes = 0;
		
		for (String s : meaning)
		{
			bytes += s.getBytes().length;
		}
		for (String s : reading)
		{
			bytes += s.getBytes().length;
		}
		
		return 1 
		+ kanji.getBytes().length
		+ additionalInfo.getBytes().length
		+ (Integer.SIZE/ 8) * (2 + 1 + 12 + 1 + reading_used.length + meaning_used.length + reading.length + 1 + meaning.length + 1)
		+ (Long.SIZE / 8) * 2
		+ bytes;
	}
	
	public void save(ByteBuffer buffer)
	{
		byte[] bytes;
		
		buffer.putInt(type.ordinal());
		
		bytes = kanji.getBytes();
		buffer.putInt(bytes.length);
		buffer.put(bytes);
		
		buffer.putInt(reading.length);
		
		for (String s : reading)
		{
			bytes = s.getBytes();
			buffer.putInt(bytes.length);
			buffer.put(bytes);
		}
		
		buffer.putInt(meaning.length);
		
		for (String s : meaning)
		{
			bytes = s.getBytes();
			buffer.putInt(bytes.length);
			buffer.put(bytes);
		}
		
		bytes = additionalInfo.getBytes();
		buffer.putInt(bytes.length);
		buffer.put(bytes);
		
		buffer.putInt(streak_kanji);
		buffer.putInt(streak_reading);
		buffer.putInt(streak_meaning);
		
		buffer.putInt(streak_kanji_best);
		buffer.putInt(streak_reading_best);
		buffer.putInt(streak_meaning_best);
		
		buffer.putInt(timesChecked_kanji);
		buffer.putInt(timesChecked_reading);
		buffer.putInt(timesChecked_meaning);
		
		buffer.putInt(timesCorrect_kanji);
		buffer.putInt(timesCorrect_reading);
		buffer.putInt(timesCorrect_meaning);
		
		for (int i : reading_used)
			buffer.putInt(i);
		
		for (int i : meaning_used)
			buffer.putInt(i);
		
		buffer.putLong(lastChecked);
		buffer.putLong(added);
	
		buffer.put((byte)(
			(learned ? (byte) 0b1 : 0)
			| (answered_kanji ? (byte) 0b10 : 0)
			| (answered_reading ? (byte) 0b100 : 0)
			| (answered_meaning ? (byte) 0b1000 : 0)
			| (answered_correct ? (byte) 0b10000 : 0)
			| (showInfo ? (byte) 0b100000 : 0)
		));
		
		buffer.putInt(category);
	}
	
	public static Vocabulary load(MainActivity main, ByteBuffer buffer, int version)
	{
		if (version >= 1)
		{
			byte[] bytes;

			Type type;
			
			if (version >= 7) 
			{
				type = Type.values()[buffer.getInt()];
			}
			else
			{
				type = Type.values()[Vocabulary.getOldType(buffer.getInt())];
			}
			
			bytes = new byte[buffer.getInt()];
			buffer.get(bytes);
			String kanji = new String(bytes);

			String[] reading = new String[buffer.getInt()];
			for (int i = 0; i < reading.length; ++i)
			{
				bytes = new byte[buffer.getInt()];
				buffer.get(bytes);
				reading[i] = new String(bytes);
			}

			String[] meaning = new String[buffer.getInt()];
			for (int i = 0; i < meaning.length; ++i)
			{
				bytes = new byte[buffer.getInt()];
				buffer.get(bytes);
				meaning[i] = new String(bytes);
			}

			bytes = new byte[buffer.getInt()];
			buffer.get(bytes);
			String additionalInfo = new String(bytes);

			Vocabulary v = new Vocabulary(main, type, kanji, reading, meaning, additionalInfo);

			v.streak_kanji = buffer.getInt();
			v.streak_reading = buffer.getInt();
			v.streak_meaning = buffer.getInt();

			if (version >= 3)
			{
				v.streak_kanji_best = buffer.getInt();
				v.streak_reading_best = buffer.getInt();
				v.streak_meaning_best = buffer.getInt();
			}
			
			v.timesChecked_kanji = buffer.getInt();
			v.timesChecked_reading = buffer.getInt();
			v.timesChecked_meaning = buffer.getInt();

			v.timesCorrect_kanji = buffer.getInt();
			v.timesCorrect_reading = buffer.getInt();
			v.timesCorrect_meaning = buffer.getInt();
			
			for (int i = 0; i < reading.length; ++i)
				if (i < v.reading.length)
					v.reading_used[i] = buffer.getInt();
				else
					buffer.getInt();

			for (int i = 0; i < meaning.length; ++i)
				if (i < v.meaning.length)
					v.meaning_used[i] = buffer.getInt();
				else
					buffer.getInt();
			
			v.lastChecked = buffer.getLong();
			
			if (version >= 2)
				v.added = buffer.getLong();

			byte flag = buffer.get();
			v.learned = (flag & 0b1) != 0;
			v.answered_kanji = (flag & 0b10) != 0;
			v.answered_reading = (flag & 0b100) != 0;
			v.answered_meaning = (flag & 0b1000) != 0;
			v.answered_correct = (flag & 0b10000) != 0;

			if (version >= 6)
				v.showInfo =  (flag & 0b100000) != 0;
			
			v.category = buffer.getInt();

			return v;
		}
	
		return null;
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
				return (int)(p1.added - added);
			case TIME_ADDED_REVERSED:
				return (int)(added - p1.added);
			case TYPE:
				return type.ordinal() - p1.type.ordinal();
			case NEXT_REVIEW:
				return p1.learned ? (this.learned ? (lastChecked + getNextReview() < p1.lastChecked + p1.getNextReview() ? -1 : (lastChecked + getNextReview() == p1.lastChecked + p1.getNextReview() ? 0 : 1)) : 1) : (this.learned ? -1 : 0);
			default:
				return p1.learned ? (this.learned ? (category < p1.category ? -1 : (category == p1.category ? 0 : 1)) : 1) : (this.learned ? -1 : 0);
		}
	}
	
	public static final String empty = "";
	
	public static String trim(String oldString) 
	{
		return com.google.common.base.CharMatcher.WHITESPACE.trimFrom(oldString);
	}
	
	public static boolean isKana(String s)
	{
		String trimed = trim(s);
		
		char c;
		
		for (int i = 0; i < trimed.length(); ++i)
		{
			c = trimed.charAt(i);
			
			if (c != '・' && Character.UnicodeBlock.of(c) != Character.UnicodeBlock.HIRAGANA && Character.UnicodeBlock.of(c) != Character.UnicodeBlock.KATAKANA)
			{
				return false;
			}
		}
		
		return !s.isEmpty();
	}
	
	public static boolean isKanji (char c)
	{
		return Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS;
	}
	
	public static String toHiragana(String s)
	{
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < s.length(); ++i)
			builder.append(toHiragana(s.charAt(i)));
		
		return builder.toString();
	}
	
	public static char toHiragana(char c) 
	{
		if (c == '・')
		{
			return c;
		}
		if (('\u30a1' <= c) && (c <= '\u30fe')) 
		{
			return (char) (c - 0x60);
		}
		else if (('\uff66' <= c) && (c <= '\uff9d'))
		{
			return (char) (c - 0xcf25);
		}
		return c;
	}
	
	public static boolean similiarMeaning(String s1, String s2)
	{
		if (s1.length() < 2 || Math.abs(s1.length() - s2.length()) > 1)
			return false;
		
		int match = 0;
		
		if (s1.length() > s2.length())
		{
			for (int i = 0; i < s2.length(); ++i)
			{
				if (s1.charAt(i) == s2.charAt(i))
				{
					match++;
				}
				else
					break;
			}
			
			for (int i = 0; i < s2.length(); ++i)
			{
				if (s1.charAt(s1.length() - i - 1) == s2.charAt(s2.length() - 1 - i))
				{
					match++;
				}
				else
					break;
			}
			
			return match >= s2.length();
		}
		else
		{
			for (int i = 0; i < s1.length(); ++i)
			{
				if (s2.charAt(i) == s1.charAt(i))
				{
					match++;
				}
				else
					break;
			}

			for (int i = 0; i < s1.length(); ++i)
			{
				if (s2.charAt(s2.length() - i - 1) == s1.charAt(s1.length() - 1 - i))
				{
					match++;
				}
				else
					break;
			}
			
			return match >= s1.length();
		}
	}
	
	public boolean similiarKanji(String s1, String s2)
	{
		if (isKana(s1))
		{
			return false;
		}
		
		int kanjiCount = 0;
		for (int i = 0; i < s2.length(); ++i)
		{
			if (isKanji(s2.charAt(i)))
			{
				if (!s1.contains("" + s2.charAt(i)))
					return false;
					
				kanjiCount++;
			}
					
		}
		
		int kanjiCount2 = 0;
		for (int i = 0; i < s1.length(); ++i)
		{
			if (isKanji(s1.charAt(i)))
			{
				if (!s2.contains("" + s1.charAt(i)))
					return false;

				kanjiCount2++;
			}

		}
		
		return kanjiCount > 0 && kanjiCount == kanjiCount2;
	}
	
	public enum Type
	{
		NONE,
		OTHER,
		NOUN,
		I_ADJECTIVE,
		NA_ADJECTIVE,
		RU_VERB,
		U_VERB,
		ADVERB,
		EXPRESSION,
		PARTICLE,
		CONJUNCTION
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
		return Type.valueOf(Type_old.values()[ordinal].name()).ordinal();
	}
	
	public enum ImportType
	{
		MERGE,
		REPLACE,
		REPLACE_KEEEP_STATS,
		KEEP,
		ASK
	}
	
	public enum SortType
	{
		CATEGORY,
		CATEGORY_REVERSED,
		TIME_ADDED,
		TIME_ADDED_REVERSED,
		TYPE,
		NEXT_REVIEW,
	}
	
	public enum ViewType
	{
		LARGE,
		MEDIUM,
		SMALL
	}
	
	public enum ShowType
	{
		ALL,
		LEARNED,
		UNLEARNED
	}
}
