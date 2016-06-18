package com.taraxippus.vocab.vocabulary;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.format.DateFormat;
import com.taraxippus.vocab.util.DialogHelper;
import com.taraxippus.vocab.util.SaveHelper;
import com.taraxippus.vocab.util.StringHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.text.BreakIterator;
import java.security.Key;

public class DBHelper extends SQLiteOpenHelper
{
	public final Context context;
	
	public DBHelper(Context context)
	{
		super(context, "Vocab.db", null, SaveHelper.VERSION);
		
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(
			"CREATE TABLE vocab ("
			+ "id integer primary key autoincrement not null,"
			+ "type int,"
			+ "kanji text,"
			+ "reading text,"
			+ "meaning text,"
			+ "additionalInfo text,"
			+ "notes text,"
			+ "category int,"
			+ "learned int,"
			+ "showInfo int,"
			
			+ "added int,"
			+ "lastChecked int,"
			+ "streak_kanji int,"
			+ "streak_reading int,"
			+ "streak_meaning int,"
			+ "streak_kanji_best int,"
			+ "streak_reading_best int,"
			+ "streak_meaning_best int,"
			+ "timesChecked_kanji int,"
			+ "timesChecked_reading int,"
			+ "timesChecked_meaning int,"
			+ "timesCorrect_kanji int,"
			+ "timesCorrect_reading int,"
			+ "timesCorrect_meaning int,"
			+ "reading_used text,"
			+ "meaning_used text,"
			+ "category_history text,"
			+ "answered int,"
			
			+ "soundFile text,"
			+ "imageFile text"
			+ ");"
		);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		if (oldVersion < 15)
			db.execSQL("ALTER TABLE vocab ADD COLUMN column int DEFAULT 0;");
	}

	public int getId(String kanji)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT id FROM vocab WHERE kanji = ?", new String[] {kanji});
		if (res.getCount() <= 0)
		{
			res.close();
			return -1;
		}

		res.moveToFirst();
		
		int id = res.getInt(0);
		res.close();
		return id;
	}
	
	public String getKanji(int id)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT kanji FROM vocab WHERE id = ?", new String[] {"" + id});
		if (res.getCount() <= 0)
		{
			res.close();
			return null;
		}

		res.moveToFirst();

		String kanji = res.getString(0);
		res.close();
		return kanji;
	}
	
	public Vocabulary getVocabulary(int id)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT * FROM vocab WHERE id = " + id, null);
		if (res.getCount() <= 0)
		{
			res.close();
			return null;
		}

		res.moveToFirst();
		Vocabulary vocabulary = getVocabulary(res);
		res.close();
		
		return vocabulary;
	}
	
	public ArrayList<Integer> getVocabularies(SortType sortType, ShowType showType, boolean[] show)
	{
		final ArrayList<Integer> vocabularies = new ArrayList<>();
		
		String learnedStatement, showStatement, orderStatement;
		
		switch (showType)
		{
			case LEARNED:
				learnedStatement = "learned = 1 AND ";
				break;
			case UNLEARNED:
				learnedStatement = "learned = 0 AND ";
				break;
			default:
				learnedStatement = "";
		}
		
		if (show == null || show.length == 0)
			showStatement = "type > -1";
		
		else
		{
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < show.length; ++i)
				if (show[i])
				{
					if (sb.length() != 0)
						sb.append(" OR ");

					sb.append("type = " + i);
				}

			showStatement = sb.toString();

			if (showStatement.isEmpty())
				return vocabularies;
		}
		
		switch (sortType)
		{
			case CATEGORY:
				orderStatement = "category ASC";
				break;
			case CATEGORY_REVERSED:
				orderStatement = "category DESC";
				break;
			case TIME_ADDED:
				orderStatement = "added ASC";
				break;
			case TIME_ADDED_REVERSED:
				orderStatement = "added DESC";
				break;
			case TYPE:
				orderStatement = "type";
				break;
			case NEXT_REVIEW:
			default:
				orderStatement = "category";
		}
			
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT id FROM vocab WHERE " + learnedStatement + "(" + showStatement + ") ORDER BY " + orderStatement, null);
		if (res.getCount() <= 0)
		{
			res.close();
			return null;
		}

		res.moveToFirst();

		do
		{
			vocabularies.add(res.getInt(0));
		}
		while (res.moveToNext());

		res.close();

		return vocabularies;
	}

	public ArrayList<Vocabulary> getQuizVocabularies()
	{
		final ArrayList<Vocabulary> vocabularies = new ArrayList<>();
		
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT * FROM vocab WHERE learned = 1", null);
		if (res.getCount() <= 0)
		{
			res.close();
			return null;
		}

		res.moveToFirst();
		
		do
		{
			if (res.getLong(res.getColumnIndex("lastChecked")) + Vocabulary.getNextReview(res.getInt(res.getColumnIndex("category"))) < System.currentTimeMillis())
				vocabularies.add(getVocabulary(res));
		}
		while (res.moveToNext());
		
		res.close();
		
		return vocabularies;
	}
	
	public Vocabulary getVocabulary(Cursor res)
	{
		Vocabulary vocabulary = new Vocabulary(
			VocabularyType.values()[res.getInt(res.getColumnIndex("type"))],
			res.getString(res.getColumnIndex("kanji")),
			StringHelper.toStringArray(res.getString(res.getColumnIndex("reading"))), 
			StringHelper.toStringArray(res.getString(res.getColumnIndex("meaning"))),
			res.getString(res.getColumnIndex("additionalInfo")),
			res.getString(res.getColumnIndex("notes")));

		vocabulary.category = res.getInt(res.getColumnIndex("category"));
		vocabulary.learned = res.getInt(res.getColumnIndex("learned")) == 1;
		vocabulary.showInfo = res.getInt(res.getColumnIndex("showInfo")) == 1;

		vocabulary.added = res.getLong(res.getColumnIndex("added"));
		vocabulary.lastChecked = res.getLong(res.getColumnIndex("lastChecked"));

		vocabulary.streak_kanji = res.getInt(res.getColumnIndex("streak_kanji"));
		vocabulary.streak_reading = res.getInt(res.getColumnIndex("streak_reading"));
		vocabulary.streak_meaning = res.getInt(res.getColumnIndex("streak_meaning"));

		vocabulary.streak_kanji_best = res.getInt(res.getColumnIndex("streak_kanji_best"));
		vocabulary.streak_reading_best = res.getInt(res.getColumnIndex("streak_reading_best"));
		vocabulary.streak_meaning_best = res.getInt(res.getColumnIndex("streak_meaning_best"));

		vocabulary.timesChecked_kanji = res.getInt(res.getColumnIndex("timesChecked_kanji"));
		vocabulary.timesChecked_reading = res.getInt(res.getColumnIndex("timesChecked_reading"));
		vocabulary.timesChecked_meaning = res.getInt(res.getColumnIndex("timesChecked_meaning"));

		vocabulary.timesCorrect_kanji = res.getInt(res.getColumnIndex("timesCorrect_kanji"));
		vocabulary.timesCorrect_reading = res.getInt(res.getColumnIndex("timesCorrect_reading"));
		vocabulary.timesCorrect_meaning = res.getInt(res.getColumnIndex("timesCorrect_meaning"));

		System.arraycopy(StringHelper.toIntArray(res.getString(res.getColumnIndex("reading_used"))), 0, vocabulary.reading_used, 0, vocabulary.reading_used.length);
		System.arraycopy(StringHelper.toIntArray(res.getString(res.getColumnIndex("meaning_used"))), 0, vocabulary.meaning_used, 0, vocabulary.meaning_used.length);
		System.arraycopy(StringHelper.toIntArray(res.getString(res.getColumnIndex("category_history"))), 0, vocabulary.category_history, 0, vocabulary.category_history.length);

		int answered = res.getInt(res.getColumnIndex("answered"));
		vocabulary.answered_kanji = (answered & 0b1) != 0;
		vocabulary.answered_reading = (answered & 0b10) != 0;
		vocabulary.answered_meaning = (answered & 0b100) != 0;
		vocabulary.answered_correct = (answered & 0b1000) != 0;

		vocabulary.soundFile = res.getString(res.getColumnIndex("soundFile"));
		vocabulary.imageFile = res.getString(res.getColumnIndex("imageFile"));
		return vocabulary;
	}
	
	public void delete()
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS vocab");
		onCreate(db);
	}

	public boolean exists(String kanji)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT id FROM vocab WHERE kanji = ?", new String[] {kanji});
		if (res.getCount() <= 0)
		{
			res.close();
			return false;
		}

		res.close();
		return true;
	}
	
	public boolean exists(int id)
	{
		if (id == -1)
			return false;
		
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT id FROM vocab WHERE id = ?", new String[] {"" + id});
		if (res.getCount() <= 0)
		{
			res.close();
			return false;
		}

		res.close();
		return true;
	}

	public void updateVocabulary(final Vocabulary vocabulary, final int id1, ImportType importType)
	{
		final SQLiteDatabase db = this.getWritableDatabase();
		
		final ContentValues contentValues = new ContentValues();
		contentValues.put("type", vocabulary.type.ordinal());
		contentValues.put("kanji", vocabulary.kanji);
		contentValues.put("reading", StringHelper.toString(vocabulary.reading));
		contentValues.put("meaning", StringHelper.toString(vocabulary.meaning));
		contentValues.put("additionalInfo", vocabulary.additionalInfo);
		contentValues.put("notes", vocabulary.notes);
		contentValues.put("category", vocabulary.category);
		contentValues.put("learned", vocabulary.learned);
		contentValues.put("showInfo", vocabulary.showInfo);
		contentValues.put("added", vocabulary.added);
		contentValues.put("lastChecked", vocabulary.lastChecked);
		contentValues.put("streak_kanji", vocabulary.streak_kanji);
		contentValues.put("streak_reading", vocabulary.streak_reading);
		contentValues.put("streak_meaning", vocabulary.streak_meaning);
		contentValues.put("streak_kanji_best", vocabulary.streak_kanji_best);
		contentValues.put("streak_reading_best", vocabulary.streak_reading_best);
		contentValues.put("streak_meaning_best", vocabulary.streak_meaning_best);
		contentValues.put("timesChecked_kanji", vocabulary.timesChecked_kanji);
		contentValues.put("timesChecked_reading", vocabulary.timesChecked_reading);
		contentValues.put("timesChecked_meaning", vocabulary.timesChecked_meaning);
		contentValues.put("timesCorrect_kanji", vocabulary.timesCorrect_kanji);
		contentValues.put("timesCorrect_reading", vocabulary.timesCorrect_reading);
		contentValues.put("timesCorrect_meaning", vocabulary.timesCorrect_meaning);
		contentValues.put("reading_used", StringHelper.toString(vocabulary.reading_used));
		contentValues.put("meaning_used", StringHelper.toString(vocabulary.meaning_used));
		contentValues.put("category_history", StringHelper.toString(vocabulary.category_history));
		contentValues.put("answered", 
						  (vocabulary.answered_kanji ? (byte) 0b1 : 0)
						  | (vocabulary.answered_reading ? (byte) 0b10 : 0)
						  | (vocabulary.answered_meaning ? (byte) 0b100 : 0)
						  | (vocabulary.answered_correct ? (byte) 0b1000 : 0));
		contentValues.put("soundFile", vocabulary.soundFile);
		contentValues.put("imageFile", vocabulary.imageFile);

		final int id = id1 == -1 ? getId(vocabulary.kanji) : id1;
		
		if (exists(id))
		{
			int i, i1, index;
			Vocabulary old;
			switch (importType)
			{
				case KEEP:
					break;
					
				case REPLACE:
					db.update("vocab", contentValues, "id = ?", new String[] {"" + id});
					break;
					
				case REPLACE_KEEP_STATS:
					old = getVocabulary(id);
					
					if (!vocabulary.learned)
						contentValues.put("learned", old.learned);
					
					if (old.category > vocabulary.category)
					{
						contentValues.put("category", old.category);
						contentValues.put("category_history", StringHelper.toString(old.category_history));
					}

					contentValues.put("timesChecked_kanji", vocabulary.timesChecked_kanji + old.timesChecked_kanji);
					contentValues.put("timesChecked_reading", vocabulary.timesChecked_reading + old.timesChecked_reading);
					contentValues.put("timesChecked_meaning", vocabulary.timesChecked_meaning + old.timesChecked_meaning);
					contentValues.put("timesCorrect_kanji", vocabulary.timesCorrect_kanji + old.timesCorrect_kanji);
					contentValues.put("timesCorrect_reading", vocabulary.timesCorrect_reading + old.timesCorrect_reading);
					contentValues.put("timesCorrect_meaning", vocabulary.timesCorrect_meaning + old.timesCorrect_meaning);
					contentValues.put("streak_kanji", vocabulary.streak_kanji + old.streak_meaning);
					contentValues.put("streak_reading", vocabulary.streak_reading + old.streak_meaning);
					contentValues.put("streak_meaning", vocabulary.streak_meaning + old.streak_meaning);
					contentValues.put("streak_kanji_best", Math.max(Math.max(vocabulary.streak_kanji + old.streak_kanji, old.streak_kanji_best), vocabulary.streak_kanji_best));
					contentValues.put("streak_reading_best", Math.max(Math.max(vocabulary.streak_kanji + old.streak_kanji, old.streak_kanji_best), vocabulary.streak_kanji_best));
					contentValues.put("streak_meaning_best", Math.max(Math.max(vocabulary.streak_kanji + old.streak_kanji, old.streak_kanji_best), vocabulary.streak_kanji_best));

					if (old.added < vocabulary.added)
						contentValues.put("added", old.added);

					if (old.lastChecked > vocabulary.lastChecked)
						contentValues.put("lastChecked", old.lastChecked);
						
					for (i = 0; i < old.reading_trimed.length; ++i)
						for (i1 = 0; i1 < vocabulary.reading_trimed.length; ++i1)
							if (vocabulary.reading_trimed[i1].equalsIgnoreCase(old.reading_trimed[i]))
								vocabulary.reading_used[i1] = old.reading_used[i];

					for (i = 0; i < old.meaning.length; ++i)
						for (i1 = 0; i1 < vocabulary.meaning.length; ++i1)
							if (vocabulary.meaning[i1].equalsIgnoreCase(old.meaning[i]))
								vocabulary.meaning_used[i1] = old.meaning_used[i];
					
					db.update("vocab", contentValues, "id = ?", new String[] {"" + id});
					break;
					
				case MERGE:
					old = getVocabulary(id);
					
					ArrayList<String> reading = new ArrayList<>();
					ArrayList<String> meaning = new ArrayList<>();
					ArrayList<Integer> reading_used = new ArrayList<>();
					ArrayList<Integer> meaning_used = new ArrayList<>();

					for (i = 0; i < vocabulary.reading.length; ++i)
					{
						reading.add(vocabulary.reading[i]);
						reading_used.add(vocabulary.reading_used[i]);
					}

					for (i = 0; i < old.reading.length; ++i)
					{
						index = reading.indexOf(old.reading[i]);

						if (index == -1)
						{
							reading.add(old.reading[i]);
							reading_used.add(old.reading_used[i]);
						}
						else
							reading_used.set(index, reading_used.get(index) + old.reading_used[i]);
					}

					for (i = 0; i < vocabulary.meaning.length; ++i)
					{
						meaning.add(vocabulary.meaning[i]);
						meaning_used.add(vocabulary.meaning_used[i]);
					}

					for (i = 0; i < old.meaning.length; ++i)
					{
						index = meaning.indexOf(old.meaning[i]);

						if (index == -1)
						{
							meaning.add(old.meaning[i]);
							meaning_used.add(old.meaning_used[i]);
						}
						else
							meaning_used.set(index, meaning_used.get(index) + old.meaning_used[i]);
					}

					if (old.type != VocabularyType.NONE)
						contentValues.put("type", old.type.ordinal());
						
					contentValues.put("reading", StringHelper.toString(reading));
					contentValues.put("meaning", StringHelper.toString(meaning));
					contentValues.put("additionalInfo", vocabulary.additionalInfo + (!vocabulary.additionalInfo.isEmpty() && !old.additionalInfo.isEmpty() && ! old.additionalInfo.equalsIgnoreCase(vocabulary.additionalInfo) ? ", " : "") + (old.additionalInfo.equalsIgnoreCase(vocabulary.additionalInfo) ? "" : old.additionalInfo));
					contentValues.put("notes", vocabulary.notes + (!vocabulary.notes.isEmpty() && !old.notes.isEmpty() && ! old.notes.equalsIgnoreCase(vocabulary.notes) ? ", " : "") + (old.notes.equalsIgnoreCase(vocabulary.notes) ? "" : old.notes));
					contentValues.put("reading_used", StringHelper.toString(reading_used));
					contentValues.put("meaning_used", StringHelper.toString(meaning_used));
					
					if (!vocabulary.learned)
						contentValues.put("learned", old.learned);
						
					if (old.category > vocabulary.category)
					{
						contentValues.put("category", old.category);
						contentValues.put("category_history", StringHelper.toString(old.category_history));
					}
					
					contentValues.put("timesChecked_kanji", vocabulary.timesChecked_kanji + old.timesChecked_kanji);
					contentValues.put("timesChecked_reading", vocabulary.timesChecked_reading + old.timesChecked_reading);
					contentValues.put("timesChecked_meaning", vocabulary.timesChecked_meaning + old.timesChecked_meaning);
					contentValues.put("timesCorrect_kanji", vocabulary.timesCorrect_kanji + old.timesCorrect_kanji);
					contentValues.put("timesCorrect_reading", vocabulary.timesCorrect_reading + old.timesCorrect_reading);
					contentValues.put("timesCorrect_meaning", vocabulary.timesCorrect_meaning + old.timesCorrect_meaning);
					contentValues.put("streak_kanji", vocabulary.streak_kanji + old.streak_meaning);
					contentValues.put("streak_reading", vocabulary.streak_reading + old.streak_meaning);
					contentValues.put("streak_meaning", vocabulary.streak_meaning + old.streak_meaning);
					contentValues.put("streak_kanji_best", Math.max(Math.max(vocabulary.streak_kanji + old.streak_kanji, old.streak_kanji_best), vocabulary.streak_kanji_best));
					contentValues.put("streak_reading_best", Math.max(Math.max(vocabulary.streak_kanji + old.streak_kanji, old.streak_kanji_best), vocabulary.streak_kanji_best));
					contentValues.put("streak_meaning_best", Math.max(Math.max(vocabulary.streak_kanji + old.streak_kanji, old.streak_kanji_best), vocabulary.streak_kanji_best));
					
					if (vocabulary.learned != old.learned)
						contentValues.put("learned", false);
					
					if (old.added < vocabulary.added)
						contentValues.put("added", old.added);
					
					if (old.lastChecked > vocabulary.lastChecked)
						contentValues.put("lastChecked", old.lastChecked);
					
					if (vocabulary.imageFile.isEmpty())
						contentValues.put("imageFile", old.imageFile);
					
					contentValues.put("answered", (byte) 0b1000);
					
					db.update("vocab", contentValues, "id = ?", new String[] {"" + id});
					break;
					
				case ASK:
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
					alertDialog.setMessage("Conflict");
					alertDialog.setMessage("There already exists an vocabulary with the kanji \"" + vocabulary.kanji + "\"! What do you want to do?");
					alertDialog.setPositiveButton("Replace",
						new DialogInterface.OnClickListener() 
						{
							public void onClick(DialogInterface dialog, int which) 
							{
								db.update("vocab", contentValues, "id = ?", new String[] {"" + id});
								
								dialog.dismiss();
							}
						});
					alertDialog.setNegativeButton("Keep",
						new DialogInterface.OnClickListener() 
						{
							public void onClick(DialogInterface dialog, int which) 
							{
								dialog.dismiss();
							}
						});
					alertDialog.setNeutralButton("Merge",
						new DialogInterface.OnClickListener() 
						{
							public void onClick(DialogInterface dialog, int which) 
							{
								updateVocabulary(vocabulary, id, ImportType.MERGE);
								dialog.dismiss();
							}
						});
					break;
			}
		}
		else
			db.insert("vocab", null, contentValues);
	}
	
	public void resetVocabulary(int id)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT reading_used, meaning_used FROM vocab WHERE id = " + id, null);
		if (res.getCount() <= 0)
		{
			res.close();
			return;
		}

		res.moveToFirst();
		
		int[] reading_used = StringHelper.toIntArray(res.getString(res.getColumnIndex("reading_used")));
		for (int i = 0; i < reading_used.length; ++i)
			reading_used[i] = 0;

		int[] meaning_used = StringHelper.toIntArray(res.getString(res.getColumnIndex("meaning_used")));
		for (int i = 0; i < meaning_used.length; ++i)
			meaning_used[i] = 0;
		
		res.close();
		
		int[] category_history = new int[32];
		category_history[category_history.length - 1] = 1;
		for (int i = 0; i < category_history.length - 1; ++i)
			category_history[i] = -1;
			
		ContentValues contentValues = new ContentValues();
		contentValues.put("category", 1);
		contentValues.put("learned", 0);
		contentValues.put("lastChecked", 0);
		contentValues.put("streak_kanji", 0);
		contentValues.put("streak_reading", 0);
		contentValues.put("streak_meaning", 0);
		contentValues.put("streak_kanji_best", 0);
		contentValues.put("streak_reading_best", 0);
		contentValues.put("streak_meaning_best", 0);
		contentValues.put("timesChecked_kanji", 0);
		contentValues.put("timesChecked_reading", 0);
		contentValues.put("timesChecked_meaning", 0);
		contentValues.put("timesCorrect_kanji", 0);
		contentValues.put("timesCorrect_reading", 0);
		contentValues.put("timesCorrect_meaning", 0);
		contentValues.put("reading_used", StringHelper.toString(reading_used));
		contentValues.put("meaning_used", StringHelper.toString(meaning_used));
		contentValues.put("category_history", StringHelper.toString(category_history));
		contentValues.put("answered", (byte) 0b1000);
		
		db = this.getWritableDatabase();
		db.update("vocab", contentValues, "id = ?", new String[] {"" + id});
	}
	
	public void resetVocabularyCategory(int id)
	{
		if (!exists(id))
			return;
			
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues contentValues = new ContentValues();
		contentValues.put("category", 1);
		
		db.update("vocab", contentValues, "id = ?", new String[] {"" + id});
	}
	
	public void updateVocabularyLearned(int id, boolean learned)
	{
		if (!exists(id))
			return;

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues contentValues = new ContentValues();
		contentValues.put("learned", learned ? 1 : 0);

		db.update("vocab", contentValues, "id = ?", new String[] {"" + id});
	}
	
	public void updateVocabulariesLearned(boolean learned)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues contentValues = new ContentValues();
		contentValues.put("learned", learned ? 1 : 0);

		db.update("vocab", contentValues, "learned = ?", new String[] {learned ? "0" : "1"});
	}
	
	public void deleteVocabulary(int id)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete("vocab", "id = ?", new String[] {"" + id});
	}
	
	public void deleteVocabularies()
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DELETE FROM vocab");
	}
	
	public void deleteVocabularies(ShowType showType, boolean[] show)
	{
		String learnedStatement, showStatement;

		switch (showType)
		{
			case LEARNED:
				learnedStatement = "learned = 1 AND ";
				break;
			case UNLEARNED:
				learnedStatement = "learned = 0 AND ";
				break;
			default:
				learnedStatement = "";
		}

		if (show == null || show.length == 0)
			showStatement = "type > -1";

		else
		{
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < show.length; ++i)
				if (show[i])
				{
					if (sb.length() != 0)
						sb.append(" OR ");

					sb.append("type = " + i);
				}

			showStatement = sb.toString();

			if (showStatement.isEmpty())
				return;
		}
		
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete("vocab", learnedStatement + "(" + showStatement + ")", null);
	}
	
	public void saveToFile()
	{
		close();
		
		File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
		dir.mkdirs();
		String filename1 = "Vocab " + new DateFormat().getDateFormat(context).format(new Date()).replace('/', '-');
		
		String filename = dir.getAbsolutePath() + "/" + filename1 + ".db";
		int i = 1;
		while (new File(filename).exists())
		{
			filename = dir.getAbsolutePath() + "/" + filename1 + " " + i + ".db";
		}
		
		try
		{
			FileInputStream fis = new FileInputStream(context.getDatabasePath(getDatabaseName()));
			OutputStream output = new FileOutputStream(filename);
			
			byte[] buffer = new byte[1024];
			int length;
			while ((length = fis.read(buffer)) > 0)
			{
				output.write(buffer, 0, length);
			}
			
			output.flush();
			output.close();
			fis.close();
			
			DialogHelper.createDialog(context, "Save", "Saved vocabulary database as \"" + filename1 + ".db\" in documents");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			DialogHelper.createDialog(context, "Saving failed", "An error occured:\n" + e.getMessage());
		}
	}
	
	public void loadFromFile(FileInputStream fis)
	{
		close();

		try
		{
			OutputStream output = new FileOutputStream(context.getDatabasePath(getDatabaseName()));

			byte[] buffer = new byte[1024];
			int length;
			while ((length = fis.read(buffer)) > 0)
			{
				output.write(buffer, 0, length);
			}

			output.flush();
			output.close();
			fis.close();

			getWritableDatabase().close();
			
			DialogHelper.createDialog(context, "Load", "Loaded vocabularies");
		}
		catch (Exception e)
		{
			e.printStackTrace();

			DialogHelper.createDialog(context, "Loading failed", "An error occured:\n" + e.getMessage());
		}
	}
}
