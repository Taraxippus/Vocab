package com.taraxippus.vocab.save;

import android.content.*;
import android.os.*;
import android.support.v7.app.*;
import com.taraxippus.vocab.*;
import com.taraxippus.vocab.vocabulary.*;
import java.io.*;
import java.nio.*;

public class SaveHandler
{
	public static final byte FORMAT_VERSION = 7;
	
	public final MainActivity main;
	public boolean save = true;
	
	public SaveHandler(MainActivity main)
	{
		this.main = main;
	}
	
	public void importVocabulary(String line, Vocabulary.ImportType importType, Vocabulary.Type type, boolean learned)
	{
		if (!line.contains("-"))
			return;

		Vocabulary v;
		String rest;
		String[] reading, meaning;

		if (!line.contains("【") || !line.contains("】") )
		{
			String kanji = line.substring(0, line.indexOf("-"));

			if (!Vocabulary.isKana(kanji))
				return;

			if (line.contains("(") && line.contains(")"))
			{
				rest = line.substring(line.indexOf("-") + 1, line.indexOf("("));
				if (line.indexOf(")") != Vocabulary.trim(line).length() - 1)
					rest = rest + line.substring(line.indexOf(")") + 1, line.length());
				meaning = rest.split(",|;");

				for (int i = 0; i < meaning.length; ++i)
					meaning[i] = Vocabulary.trim(meaning[i]);

				v = new Vocabulary(main, type, Vocabulary.trim(kanji), new String[0], meaning, Vocabulary.trim(line.substring(line.indexOf("(") + 1, line.indexOf(")"))));
			}
			else
			{
				meaning = line.substring(line.indexOf("-") + 1, line.length()).split(",|;");
				for (int i = 0; i < meaning.length; ++i)
					meaning[i] = Vocabulary.trim(meaning[i]);

				v = new Vocabulary(main, type, Vocabulary.trim(kanji), new String[0], meaning, "");
			}
		}
		else
		{
			if (line.contains("(") && line.contains(")"))
			{		
				reading = line.substring(line.indexOf("【") + 1, line.indexOf("】")).split("/|／");

				for (int i = 0; i < reading.length; ++i)
					reading[i] = Vocabulary.trim(reading[i]);

				rest = line.substring(line.indexOf("-") + 1, line.indexOf("("));
				if (line.indexOf(")") != Vocabulary.trim(line).length() - 1)
					rest = rest + line.substring(line.indexOf(")") + 1, line.length());
				meaning = rest.split(",|;");

				for (int i = 0; i < meaning.length; ++i)
					meaning[i] = Vocabulary.trim(meaning[i]);

				v = new Vocabulary(main, type, Vocabulary.trim(line.substring(0, line.indexOf("【"))), reading, meaning, Vocabulary.trim(line.substring(line.indexOf("(") + 1, line.indexOf(")"))));
			}
			else
			{
				reading = line.substring(line.indexOf("【") + 1, line.indexOf("】")).split("/|／");

				for (int i = 0; i < reading.length; ++i)
					reading[i] = Vocabulary.trim(reading[i]);

				meaning = line.substring(line.indexOf("-") + 1, line.length()).split(",|;");
				for (int i = 0; i < meaning.length; ++i)
					meaning[i] = Vocabulary.trim(meaning[i]);

				v = new Vocabulary(main, type, Vocabulary.trim(line.substring(0, line.indexOf("【"))), reading, meaning, "");
			}
		}

		v.learned = learned;
		v.add(main.vocabulary, importType, main);
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
				outputStream = main.openFileOutput("vocabulary.vocab", Context.MODE_PRIVATE);
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
				bytes += v.getBytes();
			}

			ByteBuffer buffer = ByteBuffer.allocate(bytes + 1 + main.show.length + 4 * (Integer.SIZE / 8));

			buffer.put(FORMAT_VERSION);

			buffer.putInt(main.sortType.ordinal());
			buffer.putInt(main.viewType.ordinal());
			buffer.putInt(main.showType.ordinal());
			buffer.putInt(main.show.length);
			for (boolean b : main.show)
				buffer.put(b ? (byte)1 : 0);

			for (Vocabulary v : main.vocabulary)
			{
				v.save(buffer);
			}

			outputStream.write(buffer.array());
			outputStream.flush();
			outputStream.close();
		} 
		catch (Exception e)
		{
			e.printStackTrace();

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

				inputStream = main.openFileInput("vocabulary.vocab");

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

			int version = buffer.get();

			if (version >= 4)
			{
				main.sortType = Vocabulary.SortType.values()[buffer.getInt()];
				main.viewType = Vocabulary.ViewType.values()[buffer.getInt()];
				if (version >= 5)
					main.showType = Vocabulary.ShowType.values()[buffer.getInt()];

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
				v = Vocabulary.load(main, buffer, version);

				if (v != null)
					v.add(main.vocabulary, Vocabulary.ImportType.REPLACE, main);
			}

			inputStream.close();
		} 
		catch (Exception e)
		{
			e.printStackTrace();

			save = false;

			AlertDialog alertDialog = new AlertDialog.Builder(main).create();
			alertDialog.setTitle("Load");
			alertDialog.setMessage("Something went wrong while trying to load :(\nSaving will be disabled to prevent overriding data\n\n" + e.toString());
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

		main.updateFilter();
		main.updateNotification();

		if (main.tap == MainActivity.Tap.QUIZ)
		{
			main.updateQuiz();

			if (main.vocabulary.get(main.vocabulary_selected).equals(selected))
			{
				main.quiz.vocabulary = main.vocabulary.get(main.vocabulary_selected);
			}
			else
			{
				main.quiz.answer = Answer.CORRECT;
				main.quiz.next();

			}

			main.setTap(main.quiz);
		}

		return true;
	}
}
