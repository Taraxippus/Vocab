package com.taraxippus.vocab.util;

import android.content.*;
import android.os.*;
import android.support.v7.app.*;
import com.taraxippus.vocab.*;
import com.taraxippus.vocab.vocabulary.*;
import java.io.*;
import java.nio.*;

public class SaveHandler
{
	public static final byte VERSION = 14;
	public static final String FILE = "vocabulary.vocab";
	
	public final MainActivity main;
	public boolean save = true;
	
	public SaveHandler(MainActivity main)
	{
		this.main = main;
	}
	
	public void importVocabulary(String line, ImportType importType, VocabularyType type, boolean learned)
	{
		if (!line.contains("-"))
			return;

		Vocabulary v;
		String rest;
		String[] reading, meaning;

		if (!line.contains("【") || !line.contains("】"))
		{
			String kanji = line.substring(0, line.indexOf("-"));

			if (!StringHelper.isKana(kanji))
				return;

			if (line.contains("(") && line.contains(")"))
			{
				rest = line.substring(line.indexOf("-") + 1, line.indexOf("("));
				if (line.indexOf(")") != StringHelper.trim(line).length() - 1)
					rest = rest + line.substring(line.indexOf(")") + 1, line.length());
				meaning = rest.split(",|;");

				for (int i = 0; i < meaning.length; ++i)
					meaning[i] = StringHelper.trim(meaning[i]);

				v = new Vocabulary(main, type, StringHelper.trim(kanji), new String[0], meaning, StringHelper.trim(line.substring(line.indexOf("(") + 1, line.indexOf(")"))), "");
			}
			else
			{
				meaning = line.substring(line.indexOf("-") + 1, line.length()).split(",|;");
				for (int i = 0; i < meaning.length; ++i)
					meaning[i] = StringHelper.trim(meaning[i]);

				v = new Vocabulary(main, type, StringHelper.trim(kanji), new String[0], meaning, "", "");
			}
		}
		else
		{
			if (line.contains("(") && line.contains(")"))
			{		
				reading = line.substring(line.indexOf("【") + 1, line.indexOf("】")).split("/|／");

				for (int i = 0; i < reading.length; ++i)
					reading[i] = StringHelper.trim(reading[i]);

				rest = line.substring(line.indexOf("-") + 1, line.indexOf("("));
				if (line.indexOf(")") != StringHelper.trim(line).length() - 1)
					rest = rest + line.substring(line.indexOf(")") + 1, line.length());
				meaning = rest.split(",|;");

				for (int i = 0; i < meaning.length; ++i)
					meaning[i] = StringHelper.trim(meaning[i]);

				v = new Vocabulary(main, type, StringHelper.trim(line.substring(0, line.indexOf("【"))), reading, meaning, StringHelper.trim(line.substring(line.indexOf("(") + 1, line.indexOf(")"))), "");
			}
			else
			{
				reading = line.substring(line.indexOf("【") + 1, line.indexOf("】")).split("/|／");

				for (int i = 0; i < reading.length; ++i)
					reading[i] = StringHelper.trim(reading[i]);

				meaning = line.substring(line.indexOf("-") + 1, line.length()).split(",|;");
				for (int i = 0; i < meaning.length; ++i)
					meaning[i] = StringHelper.trim(meaning[i]);

				v = new Vocabulary(main, type, StringHelper.trim(line.substring(0, line.indexOf("【"))), reading, meaning, "", "");
			}
		}

		v.learned = learned;
		v.add(main.vocabulary, importType, main);
	}
	
	public boolean isVocabulary(String s)
	{
		if (!s.contains("-"))
			return false;
		
		if (!s.contains("【") || !s.contains("】"))
		{
			if (!StringHelper.isKana(s.substring(0, s.indexOf("-"))))
				return false;
		}
			
		return true;
	}

	public void exportVocabulary(String filename)
	{
		File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
		file.mkdirs();

		try
		{
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath() + "/" + filename)));

			int i;
			for (Vocabulary v : main.vocabulary)
			{
				writer.write(v.kanji);
				if (v.reading.length > 0)
				{
					writer.write("【");
					for (i = 0; i < v.reading.length - 1; ++i)
					{
						writer.write(v.reading[i] + " / ");
					}
					writer.write(v.reading[v.reading.length -1] + "】- ");
				}
				
				for (i = 0; i < v.meaning.length - 1; ++i)
				{
					writer.write(v.meaning[i] + "; ");
				}
				writer.write(v.meaning[v.meaning.length -1]);
				if (!v.additionalInfo.isEmpty())
					writer.write(" (" + v.additionalInfo + ")");
				writer.write("\n");
			}

			writer.close();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void save()
	{
		save(null);
	}

	public void save(String filename)
	{
		if (!save)
			return;

		FileOutputStream outputStream;

		try 
		{
			if (filename == null)
			{
				outputStream = main.openFileOutput(FILE, Context.MODE_PRIVATE);
			}
			else
			{
				File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
				file.mkdirs();

				outputStream = new FileOutputStream(file.getAbsolutePath() + "/" + filename);
			}

			int bytes = 0;

			for (Vocabulary v : main.vocabulary)
			{
				bytes += getBytes(v);
			}

			ByteBuffer buffer = ByteBuffer.allocate(bytes + 1 + main.show.length + 4 * (Integer.SIZE / 8));

			buffer.put(VERSION);

			buffer.putInt(main.sortType.ordinal());
			buffer.putInt(main.viewType.ordinal());
			buffer.putInt(main.showType.ordinal());
			buffer.putInt(main.show.length);
			for (boolean b : main.show)
				buffer.put(b ? (byte)1 : 0);

			for (Vocabulary v : main.vocabulary)
			{
				saveVocabulary(v, buffer);
			}

			outputStream.write(buffer.array());
			outputStream.flush();
			outputStream.close();
		} 
		catch (final Exception e)
		{
			e.printStackTrace();

			main.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						AlertDialog alertDialog = new AlertDialog.Builder(main).create();
						alertDialog.setTitle("Save");
						alertDialog.setMessage("Something went wrong while trying to save :(\n\n" + e.toString());
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
				});
		}
		
	}

	public void load()
	{
		load(null);
	}

	public boolean load(FileInputStream stream)
	{
		Vocabulary selected = null;

		if (main.tap == MainActivity.Tap.QUIZ && main.vocabulary_selected > 0 && main.vocabulary_selected < main.vocabulary.size())
			selected = main.vocabulary.get(main.vocabulary_selected);

		main.vocabulary.clear();
		main.vocabulary_learned.clear();

		FileInputStream inputStream;

		try 
		{
			byte[] bytes;

			if (stream == null)
			{
				if (!save)
				{
					return false;
				}

				main.getFilesDir().mkdirs();
				
				inputStream = main.openFileInput(FILE);

				bytes = new byte[(int)new File(main.getFilesDir().getAbsolutePath() + "/vocabulary.vocab").length()];
				inputStream.read(bytes);
			}
			else
			{
				inputStream = stream;

				ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
				int bufferSize = 1024;
				byte[] buffer = new byte[bufferSize];

				int len = 0;
				while ((len = inputStream.read(buffer)) != -1)
				{
					byteBuffer.write(buffer, 0, len);
				}
				bytes = byteBuffer.toByteArray();
			}


			ByteBuffer buffer = ByteBuffer.wrap(bytes);

			if (buffer.capacity() == 0)
			{
				main.dialogHelper.createDialog("Corrupt save file", "The save file is corrupt. :(");
			}
			else
			{
				int version = buffer.get();

				if (version >= 4)
				{
					main.sortType = SortType.values()[buffer.getInt()];
					main.viewType = ViewType.values()[buffer.getInt()];
					if (version >= 5)
						main.showType = ShowType.values()[buffer.getInt()];

					int typeCount = buffer.getInt();
					for (int i = 0; i < typeCount; ++i)
						if (i < main.show.length)
							if (version >= 7)
								main.show[i] = buffer.get() == 1;
							else
								main.show[Vocabulary.getOldType(i)] = buffer.get() == 1;
						else
							buffer.get();

				}

				Vocabulary v;

				while (buffer.hasRemaining())
				{
					v = loadVocabulary(buffer, version);

					if (v != null)
						v.add(main.vocabulary, ImportType.REPLACE, main);
				}
			}
		
			inputStream.close();
		} 
		catch (FileNotFoundException e)
		{
			main.dialogHelper.createDialog("Welcome!", "You can add new vocabularies from the \"Add new vocabulary\" tap in the navigation drawer. You can add them to your learned vocabularies by using the menus in the home tab. Learned vocabularies will appear in the quiz. A handwriting keyboard is recommended to enter kanji.");
		}
		catch (Exception e)
		{
			e.printStackTrace();

			save = false;

			main.dialogHelper.createDialog("Load", "Something went wrong while trying to load :(\nSaving will be disabled to prevent overriding data\n\n" + e.toString());
		}

		main.updateFilter();
		main.updateNotification();

		if (main.tap == MainActivity.Tap.QUIZ)
		{
			main.updateQuiz();

			if (main.vocabulary_selected > 0 && main.vocabulary_selected < main.vocabulary.size() && main.vocabulary.get(main.vocabulary_selected).equals(selected))
			{
				main.quiz.vocabulary = main.vocabulary.get(main.vocabulary_selected);
			}
			else
			{
				main.quiz.answer = Answer.SKIP;
				main.quiz.next();
			}

			main.setTap(main.quiz);
		}

		return true;
	}
	
	public int getBytes(Vocabulary v)
	{
		int bytes = 0;

		for (String s : v.meaning)
		{
			bytes += s.getBytes().length;
		}
		for (String s : v.reading)
		{
			bytes += s.getBytes().length;
		}

		return 1 
			+ v.kanji.getBytes().length
			+ v.additionalInfo.getBytes().length
			+ v.notes.getBytes().length
			+ v.soundFile.getBytes().length
			+ v.imageFile.getBytes().length
			+ (Integer.SIZE / 8) * (3 + 2 + 12 + 2 + v.reading_used.length + v.meaning_used.length + v.reading.length + 1 + v.meaning.length + 1)
			+ (Long.SIZE / 8) * 2
			+ (Integer.SIZE / 8) * 32
			+ bytes;
	}

	public void saveVocabulary(Vocabulary v, ByteBuffer buffer)
	{
		byte[] bytes;

		buffer.putInt(v.type.ordinal());

		bytes = v.kanji.getBytes();
		buffer.putInt(bytes.length);
		buffer.put(bytes);

		buffer.putInt(v.reading.length);

		for (String s : v.reading)
		{
			bytes = s.getBytes();
			buffer.putInt(bytes.length);
			buffer.put(bytes);
		}

		buffer.putInt(v.meaning.length);

		for (String s : v.meaning)
		{
			bytes = s.getBytes();
			buffer.putInt(bytes.length);
			buffer.put(bytes);
		}

		bytes = v.additionalInfo.getBytes();
		buffer.putInt(bytes.length);
		buffer.put(bytes);
		
		bytes = v.notes.getBytes();
		buffer.putInt(bytes.length);
		buffer.put(bytes);

		buffer.putInt(v.streak_kanji);
		buffer.putInt(v.streak_reading);
		buffer.putInt(v.streak_meaning);

		buffer.putInt(v.streak_kanji_best);
		buffer.putInt(v.streak_reading_best);
		buffer.putInt(v.streak_meaning_best);

		buffer.putInt(v.timesChecked_kanji);
		buffer.putInt(v.timesChecked_reading);
		buffer.putInt(v.timesChecked_meaning);

		buffer.putInt(v.timesCorrect_kanji);
		buffer.putInt(v.timesCorrect_reading);
		buffer.putInt(v.timesCorrect_meaning);

		for (int i : v.reading_used)
			buffer.putInt(i);

		for (int i : v.meaning_used)
			buffer.putInt(i);

		buffer.putLong(v.lastChecked);
		buffer.putLong(v.added);

		buffer.put((byte)(
				   (v.learned ? (byte) 0b1 : 0)
				   | (v.answered_kanji ? (byte) 0b10 : 0)
				   | (v.answered_reading ? (byte) 0b100 : 0)
				   | (v.answered_meaning ? (byte) 0b1000 : 0)
				   | (v.answered_correct ? (byte) 0b10000 : 0)
				   | (v.showInfo ? (byte) 0b100000 : 0)
				   ));

		buffer.putInt(v.category);
		
		for (int i = 0; i < v.category_history.length; ++i)
			buffer.putInt(v.category_history[i]);
		
		bytes = v.soundFile.getBytes();
		buffer.putInt(bytes.length);
		buffer.put(bytes);
		
		bytes = v.imageFile.getBytes();
		buffer.putInt(bytes.length);
		buffer.put(bytes);
	}

	public Vocabulary loadVocabulary(ByteBuffer buffer, int version)
	{
		if (version >= 1)
		{
			byte[] bytes;

			VocabularyType type;

			if (version >= 7) 
			{
				type = VocabularyType.values()[buffer.getInt()];
			}
			else
			{
				type = VocabularyType.values()[Vocabulary.getOldType(buffer.getInt())];
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

			String notes = "";
			
			if (version >= 8)
			{
				bytes = new byte[buffer.getInt()];
				buffer.get(bytes);
				notes = new String(bytes);
			}
			
			Vocabulary v = new Vocabulary(main, type, kanji, reading, meaning, additionalInfo, notes);

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

			if (version >= 14)
				for (int i = 0; i < v.category_history.length; ++i)
					v.category_history[i] = buffer.getInt();
			else if (version == 13)
				for (int i = 0; i < v.category_history.length; ++i)
					v.category_history[v.category_history.length - 1 - i] = buffer.getInt();
			else
				v.category_history[v.category_history.length - 1] = v.category;
			
			if (version >= 9)
			{
				bytes = new byte[buffer.getInt()];
				buffer.get(bytes);
				v.soundFile = new String(bytes);
				
				if (version == 10 || version >= 12)
				{
					bytes = new byte[buffer.getInt()];
					buffer.get(bytes);

					if (version >= 12)
						v.imageFile = new String(bytes);
				}
			}
			
			return v;
		}

		return null;
	}
}
