package com.taraxippus.vocab.vocabulary;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.format.DateFormat;
import com.taraxippus.vocab.dialog.DialogHelper;
import com.taraxippus.vocab.util.OnProcessSuccessListener;
import com.taraxippus.vocab.util.StringHelper;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;

public class DBHelper extends SQLiteOpenHelper
{
	public final Context context;
	public static final int VERSION = 18;
	
	public DBHelper(Context context)
	{
		super(context, "Vocab.db", null, VERSION);
		
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
			+ "reading_trimed text,"
			+ "meaning text,"
			+ "additionalInfo text,"
			+ "notes text,"
			+ "category int,"
			+ "learned int,"
			+ "showInfo int,"
			
			+ "added int,"
			+ "lastChecked int,"
			+ "nextReview int,"
			+ "sameReading text,"
			+ "sameMeaning text,"
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
			+ "quiz_preferences int,"
			+ "soundFile text,"
			+ "imageFile text"
			+ ");"
			
			+ "CREATE TABLE kanji ("
			+ "kanji integer primary key not null,"
			+ "reading_kun text,"
			+ "reading_on text,"
			+ "meaning text,"
			+ "notes text,"
			+ "strokes int,"
			+ "category int,"
			+ "learned int,"
			+ "added int,"
			+ "lastChecked int,"
			+ "nextReview int,"
			+ "vocabularies text,"
			+ "streak_kanji int,"
			+ "streak_reading_kun int,"
			+ "streak_reading_on int,"
			+ "streak_meaning int,"
			+ "streak_kanji_best int,"
			+ "streak_reading_kun_best int,"
			+ "streak_reading_on_best int,"
			+ "streak_meaning_best int,"
			+ "timesChecked_kanji int,"
			+ "timesChecked_reading text,"
			+ "timesChecked_reading_kun int,"
			+ "timesChecked_reading_on int,"
			+ "timesChecked_meaning int,"
			+ "timesCorrect_kanji int,"
			+ "timesCorrect_reading text,"
			+ "timesCorrect_reading_kun int,"
			+ "timesCorrect_reading_on int,"
			+ "timesCorrect_meaning int,"
			+ "meaning_used text,"
			+ "category_history text,"
			+ "answered int,"
			+ "imageFile text"
			+ ");"
		);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		AlertDialog alertDialog = new AlertDialog.Builder(context)
		.setTitle("SQL Database Upgrade")
		.setMessage("Updating database")
		.setPositiveButton("OK",
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.dismiss();
				}
			})
			.show();
		
		if (oldVersion < 15)
		{
			db.execSQL("ALTER TABLE vocab ADD COLUMN nextReview int; ALTER TABLE vocab ADD COLUMN sameReading text; ALTER TABLE vocab ADD COLUMN sameMeaning text;");
		
			Cursor res =  db.rawQuery("SELECT id, kanji, reading, meaning, category, lastChecked FROM vocab", null);
			if (res.getCount() <= 0)
			{
				res.close();
				return;
			}

			res.moveToFirst();
			
			final ContentValues contentValues = new ContentValues();
			final ArrayList<Integer> sameReading = new ArrayList<>(), sameMeaning = new ArrayList<>();
			
			do
			{
				contentValues.clear();
				
				findSynonyms(db, res.getString(res.getColumnIndex("kanji")), StringHelper.toStringArray(res.getString(res.getColumnIndex("reading")).replace("・", "")), StringHelper.toStringArray(res.getString(res.getColumnIndex("meaning"))), sameReading, sameMeaning);
				contentValues.put("nextReview", res.getLong(res.getColumnIndex("lastChecked")) + Vocabulary.getNextReview(res.getInt(res.getColumnIndex("category"))));
				contentValues.put("sameReading", StringHelper.toString(sameReading));
				contentValues.put("sameMeaning", StringHelper.toString(sameMeaning));
				
				db.update("vocab", contentValues, "id = ?", new String[] {"" + res.getInt(res.getColumnIndex("id"))});
			}
			while(res.moveToNext());
			
			res.close();
		}
		if (oldVersion < 16)
		{
			db.execSQL("ALTER TABLE vocab ADD COLUMN reading_trimed text; UPDATE vocab SET reading_trimed = replace(reading, '・', '' );");
		}
		if (oldVersion < 17)
		{
			db.execSQL("CREATE TABLE kanji ("
				+ "kanji integer primary key not null,"
				+ "reading_kun text,"
				+ "reading_on text,"
				+ "meaning text,"
				+ "notes text,"
				+ "strokes int,"
				+ "category int,"
				+ "learned int,"
				+ "added int,"
				+ "lastChecked int,"
				+ "nextReview int,"
				+ "vocabularies text,"
				+ "streak_kanji int,"
				+ "streak_reading_kun int,"
				+ "streak_reading_on int,"
				+ "streak_meaning int,"
				+ "streak_kanji_best int,"
				+ "streak_reading_kun_best int,"
				+ "streak_reading_on_best int,"
				+ "streak_meaning_best int,"
				+ "timesChecked_kanji int,"
				+ "timesChecked_reading text,"
				+ "timesChecked_reading_kun int,"
				+ "timesChecked_reading_on int,"
				+ "timesChecked_meaning int,"
				+ "timesCorrect_kanji int,"
				+ "timesCorrect_reading text,"
				+ "timesCorrect_reading_kun int,"
				+ "timesCorrect_reading_on int,"
				+ "timesCorrect_meaning int,"
				+ "meaning_used text,"
				+ "category_history text,"
				+ "answered int,"
				+ "imageFile text"
				+ ");"
			);	
		}
		if (oldVersion < 18)
		{
			db.execSQL("ALTER TABLE vocab ADD COLUMN quiz_preferences int;");
			
			Cursor res =  db.rawQuery("SELECT id, reading FROM vocab", null);
			if (res.getCount() <= 0)
			{
				res.close();
				return;
			}

			res.moveToFirst();

			final ContentValues contentValues = new ContentValues();
	
			do
			{
	
				contentValues.clear();
				contentValues.put("quiz_preferences", StringHelper.lengthOfArray(res.getString(1)) == 0 ? 0b111111 : 0b010101);
				db.update("vocab", contentValues, "id = ?", new String[] {"" + res.getInt(0)});
			}
			while(res.moveToNext());

			res.close();
		}
		alertDialog.setMessage("Updated vocabulary database from version " + oldVersion + " to " + newVersion + "!");
	}

	public int getId(String kanji)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT id FROM vocab WHERE kanji = ? LIMIT 1", new String[] {kanji});
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
	
	public int getCount(String where)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT id FROM vocab WHERE " + where, null);
		int count = res.getCount();
		res.close();
		return count;
	}
	
	public int getKanjiCount(String where)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT kanji FROM kanji WHERE " + where, null);
		int count = res.getCount();
		res.close();
		return count;
	}
	
	public String getString(int id, String column)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT " + column + " FROM vocab WHERE id = ? LIMIT 1", new String[] {"" + id});
		if (res.getCount() <= 0)
		{
			res.close();
			return null;
		}

		res.moveToFirst();

		String str = res.getString(0);
		res.close();
		return str;
	}
	
	public String[] getStringArray(int id, String column)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT " + column + " FROM vocab WHERE id = ? LIMIT 1", new String[] {"" + id});
		if (res.getCount() <= 0)
		{
			res.close();
			return null;
		}

		res.moveToFirst();

		String[] str = StringHelper.toStringArray(res.getString(0));
		res.close();
		return str;
	}
	
	public int getInt(int id, String column)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT " + column + " FROM vocab WHERE id = ? LIMIT 1", new String[] {"" + id});
		if (res.getCount() <= 0)
		{
			res.close();
			return -1;
		}

		res.moveToFirst();

		int i = res.getInt(0);
		res.close();
		return i;
	}
	
	public long getLong(int id, String column)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT " + column + " FROM vocab WHERE id = ? LIMIT 1", new String[] {"" + id});
		if (res.getCount() <= 0)
		{
			res.close();
			return -1;
		}

		res.moveToFirst();

		long l = res.getLong(0);
		res.close();
		return l;
	}
	
	private String getFilteredWhereStatement(ShowType showType, boolean[] show)
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
				return "type = -1";
		}
		
		return learnedStatement + "(" + showStatement + ")";
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
	
	public int[] getVocabularies(SortType sortType, ShowType showType, boolean[] show, boolean sortReversed, String searchQuery)
	{
		String searchStatement, orderStatement;
		
		switch (sortType)
		{
			case CATEGORY:
				orderStatement = "category";
				break;
			case TIME_ADDED:
				orderStatement = "added";
				break;
			case TYPE:
				orderStatement = "type";
				break;
			case NEXT_REVIEW:
				orderStatement = "learned DESC, nextReview";
				break;
			default:
				orderStatement = "category";
		}
			
		orderStatement = orderStatement + (sortReversed ? " DESC" : " ASC");
		
		String[] searchArgs = null;
		if (searchQuery != null && !searchQuery.isEmpty())
		{
			searchQuery = searchQuery.replace("・", "");
			searchStatement = " AND (kanji LIKE ? OR reading_trimed LIKE ? OR meaning LIKE ? OR additionalInfo LIKE ?)";
			orderStatement = "kanji = ? DESC, kanji LIKE ? DESC, reading_trimed LIKE ? DESC, reading_trimed LIKE ? DESC, meaning LIKE ? DESC, meaning LIKE ? DESC, additionalInfo = ?, additionalInfo LIKE ?, " + orderStatement;
			searchArgs = new String[] {"%" + searchQuery + "%", "%" + searchQuery + "%", "%" + searchQuery + "%", "%" + searchQuery + "%",
				searchQuery, searchQuery + "%",
				"%\\" + searchQuery + "\\%", "%\\" + searchQuery + "%",
				"%\\" + searchQuery + "\\%", "%\\" + searchQuery + "%",
				searchQuery, searchQuery + "%"};
		}
		else
			searchStatement = "";
		
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT id FROM vocab WHERE " + getFilteredWhereStatement(showType, show) + searchStatement + " ORDER BY " + orderStatement, searchArgs);
		if (res.getCount() <= 0)
		{
			res.close();
			return new int[0];
		}

		res.moveToFirst();

		final int[] vocabularies = new int[res.getCount()];
		int i = 0;
		
		do
		{
			vocabularies[i] = res.getInt(0);
			i++;
		}
		while (res.moveToNext());

		res.close();

		return vocabularies;
	}

	public int[] getNewVocabularies()
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT id FROM vocab WHERE learned = 1 AND lastChecked = 0", null);
		if (res.getCount() <= 0)
		{
			res.close();
			return new int[0];
		}

		res.moveToFirst();

		int[] vocabularies = new int[res.getCount()];
		int i = 0;
		do
		{
			vocabularies[i] = res.getInt(0);
			i++;
		}
		while (res.moveToNext());

		res.close();

		return vocabularies;
	}
	
	public Vocabulary getVocabulary(Cursor res)
	{
		final Vocabulary vocabulary = new Vocabulary(res.getInt(res.getColumnIndex("id")));
		vocabulary.type = VocabularyType.values()[res.getInt(res.getColumnIndex("type"))];
		vocabulary.kanji = res.getString(res.getColumnIndex("kanji"));
		vocabulary.reading = StringHelper.toStringArray(res.getString(res.getColumnIndex("reading")));
		vocabulary.reading_trimed = StringHelper.toStringArray(res.getString(res.getColumnIndex("reading_trimed")));
		vocabulary.meaning = StringHelper.toStringArray(res.getString(res.getColumnIndex("meaning")));
		vocabulary.additionalInfo = res.getString(res.getColumnIndex("additionalInfo"));
		vocabulary.notes = res.getString(res.getColumnIndex("notes"));

		vocabulary.category = res.getInt(res.getColumnIndex("category"));
		vocabulary.learned = res.getInt(res.getColumnIndex("learned")) == 1;
		vocabulary.showInfo = res.getInt(res.getColumnIndex("showInfo")) == 1;

		vocabulary.added = res.getLong(res.getColumnIndex("added"));
		vocabulary.lastChecked = res.getLong(res.getColumnIndex("lastChecked"));
		vocabulary.nextReview = res.getLong(res.getColumnIndex("nextReview"));
		
		vocabulary.sameReading = StringHelper.toIntArray(res.getString(res.getColumnIndex("sameReading")));
		vocabulary.sameMeaning = StringHelper.toIntArray(res.getString(res.getColumnIndex("sameMeaning")));
		
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

		vocabulary.reading_used = StringHelper.toIntArray(res.getString(res.getColumnIndex("reading_used")));
		vocabulary.meaning_used = StringHelper.toIntArray(res.getString(res.getColumnIndex("meaning_used")));
		vocabulary.category_history = StringHelper.toIntArray(res.getString(res.getColumnIndex("category_history")));

		if (vocabulary.reading_used.length != vocabulary.reading.length)
			vocabulary.reading_used = new int[vocabulary.reading.length];
		
		if (vocabulary.meaning_used.length != vocabulary.meaning.length)
			vocabulary.meaning_used = new int[vocabulary.meaning.length];
		
		int answered = res.getInt(res.getColumnIndex("answered"));
		vocabulary.answered_kanji = (answered & 0b1) != 0;
		vocabulary.answered_reading = (answered & 0b10) != 0;
		vocabulary.answered_meaning = (answered & 0b100) != 0;
		vocabulary.answered_correct = (answered & 0b1000) != 0;
	
		int quiz_preferences = res.getInt(res.getColumnIndex("quiz_preferences"));
		vocabulary.kanjiReview = ReviewType.values()[quiz_preferences & 0b11];
		vocabulary.readingReview = ReviewType.values()[(quiz_preferences & 0b1100) >> 2];
		vocabulary.meaningReview = ReviewType.values()[(quiz_preferences & 0b110000) >> 4];
		vocabulary.quickReview = (quiz_preferences & 0b1000000) != 0;
		
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
	
	public void updateVocabulary(final Vocabulary vocabulary)
	{
		updateVocabulary(vocabulary, ImportType.UPDATE, null);
	}
		
	public void updateVocabulary(final Vocabulary vocabulary, ImportType importType, final OnProcessSuccessListener listener)
	{
		final SQLiteDatabase db = this.getWritableDatabase();
		
		final ContentValues contentValues = new ContentValues();
		contentValues.put("type", vocabulary.type.ordinal());
		contentValues.put("kanji", vocabulary.kanji);
		contentValues.put("reading", StringHelper.toString(vocabulary.reading));
		contentValues.put("reading_trimed", StringHelper.toString(vocabulary.reading_trimed));
		contentValues.put("meaning", StringHelper.toString(vocabulary.meaning));
		contentValues.put("additionalInfo", vocabulary.additionalInfo);
		contentValues.put("notes", vocabulary.notes);
		contentValues.put("category", vocabulary.category);
		contentValues.put("learned", vocabulary.learned);
		contentValues.put("showInfo", vocabulary.showInfo);
		contentValues.put("added", vocabulary.added);
		contentValues.put("lastChecked", vocabulary.lastChecked);
		contentValues.put("nextReview", vocabulary.nextReview);
		contentValues.put("sameReading", StringHelper.toString(vocabulary.sameReading));
		contentValues.put("sameMeaning", StringHelper.toString(vocabulary.sameMeaning));
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
		contentValues.put("quiz_preferences", 
						  (vocabulary.kanjiReview.ordinal())
						  | (vocabulary.readingReview.ordinal() << 2)
						  | (vocabulary.meaningReview.ordinal() << 4)
						  | (vocabulary.quickReview ? (byte) 0b1000000 : 0));
	
		contentValues.put("soundFile", vocabulary.soundFile);
		contentValues.put("imageFile", vocabulary.imageFile);

		final int id = vocabulary.id == -1 ? getId(vocabulary.kanji) : vocabulary.id;
		
		if (exists(id))
		{
			int i, i1, index;
			Vocabulary old;
			switch (importType)
			{
				case KEEP:
					break;
					
				case UPDATE:
					db.update("vocab", contentValues, "id = ?", new String[] {"" + id});
					break;
					
				case REPLACE:
					Cursor res =  db.rawQuery("SELECT kanji, sameReading, sameMeaning FROM vocab WHERE id = " + id, null);
					res.moveToFirst();
					updateKanjiVocabularies(id, StringHelper.getKanji(res.getString(0)), false);
					updateSynonyms(id, StringHelper.toIntArray(res.getString(1)), StringHelper.toIntArray(res.getString(2)), false);
					res.close();
					
					db.update("vocab", contentValues, "id = ?", new String[] {"" + id});
					updateSynonyms(id, vocabulary.sameReading, vocabulary.sameMeaning, true);
					updateKanjiVocabularies(id, StringHelper.getKanji(vocabulary.kanji), true);
					break;
					
				case REPLACE_KEEP_STATS:
					old = getVocabulary(id);
					
					updateSynonyms(id, old.sameReading, old.sameMeaning, false);
					updateKanjiVocabularies(id, StringHelper.getKanji(old.kanji), false);
					
					if (!vocabulary.learned)
						contentValues.put("learned", old.learned);
					
					if (old.category > vocabulary.category)
					{
						contentValues.put("category", old.category);
						contentValues.put("category_history", StringHelper.toString(old.category_history));
					}

					if (old.reading.length == 0 && vocabulary.reading.length != 0)
					{
						old.timesChecked_reading = old.timesChecked_kanji;
						old.timesCorrect_reading = old.timesCorrect_kanji;
						old.timesChecked_kanji = old.timesCorrect_kanji = 0;
					}
					else if (vocabulary.reading.length == 0 && old.reading.length != 0)
						old.timesChecked_kanji = old.timesCorrect_kanji = 0;
						
					contentValues.put("timesChecked_kanji", vocabulary.timesChecked_kanji + old.timesChecked_kanji);
					contentValues.put("timesChecked_reading", vocabulary.timesChecked_reading + old.timesChecked_reading);
					contentValues.put("timesChecked_meaning", vocabulary.timesChecked_meaning + old.timesChecked_meaning);
					contentValues.put("timesCorrect_kanji", vocabulary.timesCorrect_kanji + old.timesCorrect_kanji);
					contentValues.put("timesCorrect_reading", vocabulary.timesCorrect_reading + old.timesCorrect_reading);
					contentValues.put("timesCorrect_meaning", vocabulary.timesCorrect_meaning + old.timesCorrect_meaning);
					contentValues.put("streak_kanji", vocabulary.streak_kanji + old.streak_kanji);
					contentValues.put("streak_reading", vocabulary.streak_reading + old.streak_reading);
					contentValues.put("streak_meaning", vocabulary.streak_meaning + old.streak_meaning);
					contentValues.put("streak_kanji_best", Math.max(Math.max(vocabulary.streak_kanji + old.streak_kanji, old.streak_kanji_best), vocabulary.streak_kanji_best));
					contentValues.put("streak_reading_best", Math.max(Math.max(vocabulary.streak_reading + old.streak_reading, old.streak_reading_best), vocabulary.streak_reading_best));
					contentValues.put("streak_meaning_best", Math.max(Math.max(vocabulary.streak_meaning + old.streak_meaning, old.streak_meaning_best), vocabulary.streak_meaning_best));

					if (old.added < vocabulary.added)
						contentValues.put("added", old.added);

					if (old.lastChecked > vocabulary.lastChecked)
						contentValues.put("lastChecked", old.lastChecked);
						
					contentValues.put("nextReview", contentValues.getAsLong("lastChecked") + Vocabulary.getNextReview(contentValues.getAsInteger("category")));
						
					for (i = 0; i < old.reading_trimed.length; ++i)
						for (i1 = 0; i1 < vocabulary.reading_trimed.length; ++i1)
							if (vocabulary.reading_trimed[i1].equalsIgnoreCase(old.reading_trimed[i]))
								vocabulary.reading_used[i1] += old.reading_used[i];

					for (i = 0; i < old.meaning.length; ++i)
						for (i1 = 0; i1 < vocabulary.meaning.length; ++i1)
							if (vocabulary.meaning[i1].equalsIgnoreCase(old.meaning[i]))
								vocabulary.meaning_used[i1] += old.meaning_used[i];
					
					contentValues.put("reading_used", StringHelper.toString(vocabulary.reading_used));
					contentValues.put("meaning_used", StringHelper.toString(vocabulary.meaning_used));
					  
					db.update("vocab", contentValues, "id = ?", new String[] {"" + id});
					updateSynonyms(id, vocabulary.sameReading, vocabulary.sameMeaning, true);
					updateKanjiVocabularies(id, StringHelper.getKanji(vocabulary.kanji), true);
					
					break;
					
				case MERGE:
					old = getVocabulary(id);
					
					final ArrayList<String> reading = new ArrayList<>();
					final ArrayList<String> meaning = new ArrayList<>();
					final ArrayList<Integer> reading_used = new ArrayList<>();
					final ArrayList<Integer> meaning_used = new ArrayList<>();
					final ArrayList<Integer> sameReading = new ArrayList<>();
					final ArrayList<Integer> sameMeaning = new ArrayList<>();
					
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

					for (i = 0; i < old.sameReading.length; ++i)
						sameReading.add(old.sameReading[i]);
						
					for (i = 0; i < vocabulary.sameReading.length; ++i)
						if (!sameReading.contains(vocabulary.sameReading[i]))
						{
							updateSynonyms(id, new int[] {vocabulary.sameReading[i]}, new int[0], true);
							sameReading.add(vocabulary.sameReading[i]);
						}
							
					for (i = 0; i < old.sameMeaning.length; ++i)
						sameMeaning.add(old.sameMeaning[i]);

					for (i = 0; i < vocabulary.sameMeaning.length; ++i)
						if (!sameMeaning.contains(vocabulary.sameMeaning[i]))
						{
							updateSynonyms(id, new int[0], new int[] {vocabulary.sameMeaning[i]}, true);
							sameMeaning.add(vocabulary.sameMeaning[i]);
						}
							
					if (old.type != VocabularyType.NONE)
						contentValues.put("type", old.type.ordinal());
						
					contentValues.put("reading", StringHelper.toString(reading));
					contentValues.put("reading_trimed", StringHelper.toHiragana(StringHelper.toString(reading).replace("・", "")));
					contentValues.put("meaning", StringHelper.toString(meaning));
					contentValues.put("sameReading", StringHelper.toString(sameReading));
					contentValues.put("sameMeaning", StringHelper.toString(sameMeaning));
					contentValues.put("additionalInfo", vocabulary.additionalInfo + (!vocabulary.additionalInfo.isEmpty() && !old.additionalInfo.isEmpty() && ! old.additionalInfo.equalsIgnoreCase(vocabulary.additionalInfo) ? ", " : "") + (old.additionalInfo.equalsIgnoreCase(vocabulary.additionalInfo) ? "" : old.additionalInfo));
					contentValues.put("notes", vocabulary.notes + (!vocabulary.notes.isEmpty() && !old.notes.isEmpty() && !old.notes.equalsIgnoreCase(vocabulary.notes) ? ", " : "") + (old.notes.equalsIgnoreCase(vocabulary.notes) ? "" : old.notes));
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
					contentValues.put("streak_kanji", vocabulary.streak_kanji + old.streak_kanji);
					contentValues.put("streak_reading", vocabulary.streak_reading + old.streak_reading);
					contentValues.put("streak_meaning", vocabulary.streak_meaning + old.streak_meaning);
					contentValues.put("streak_kanji_best", Math.max(Math.max(vocabulary.streak_kanji + old.streak_kanji, old.streak_kanji_best), vocabulary.streak_kanji_best));
					contentValues.put("streak_reading_best", Math.max(Math.max(vocabulary.streak_reading + old.streak_reading, old.streak_reading_best), vocabulary.streak_reading_best));
					contentValues.put("streak_meaning_best", Math.max(Math.max(vocabulary.streak_meaning + old.streak_meaning, old.streak_meaning_best), vocabulary.streak_meaning_best));
					contentValues.put("quiz_preferences", 
									  (0b010101)
									  | (vocabulary.quickReview && old.quickReview ? (byte) 0b1000000 : 0));
					
					if (old.added < vocabulary.added)
						contentValues.put("added", old.added);
					
					if (old.lastChecked > vocabulary.lastChecked)
						contentValues.put("lastChecked", old.lastChecked);
					
					contentValues.put("nextReview", contentValues.getAsLong("lastChecked") + Vocabulary.getNextReview(contentValues.getAsInteger("category")));
						
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
								Cursor res =  db.rawQuery("SELECT kanji, sameReading, sameMeaning FROM vocab WHERE id = " + id, null);
								res.moveToFirst();
								updateKanjiVocabularies(id, StringHelper.getKanji(res.getString(0)), false);
								updateSynonyms(id, StringHelper.toIntArray(res.getString(1)), StringHelper.toIntArray(res.getString(2)), false);
								res.close();

								db.update("vocab", contentValues, "id = ?", new String[] {"" + id});
								updateSynonyms(id, vocabulary.sameReading, vocabulary.sameMeaning, true);
								updateKanjiVocabularies(id, StringHelper.getKanji(vocabulary.kanji), true);
								
								if (listener != null)
									listener.onProcessSuccess(true);
									
								dialog.dismiss();
							}
						});
					alertDialog.setNegativeButton("Keep",
						new DialogInterface.OnClickListener() 
						{
							public void onClick(DialogInterface dialog, int which) 
							{
								if (listener != null)
									listener.onProcessSuccess(false);
									
								dialog.dismiss();
							}
						});
					alertDialog.setNeutralButton("Merge",
						new DialogInterface.OnClickListener() 
						{
							public void onClick(DialogInterface dialog, int which) 
							{
								vocabulary.id = id;
								updateVocabulary(vocabulary, ImportType.MERGE, listener);
								dialog.dismiss();
							}
						});
					alertDialog.show();
					return;
			}
		}
		else
		{
			vocabulary.id = (int) db.insert("vocab", null, contentValues);
			updateSynonyms(vocabulary.id, vocabulary.sameReading, vocabulary.sameMeaning, true);
			updateKanjiVocabularies(vocabulary.id, StringHelper.getKanji(vocabulary.kanji), true);
		}
			
		if (listener != null)
			listener.onProcessSuccess(true);
	}
	
	public void resetVocabulary(int... id)
	{
		int[] category_history = new int[32];
		category_history[category_history.length - 1] = 0;
		for (int i = 0; i < category_history.length - 1; ++i)
			category_history[i] = -1;
			
		ContentValues contentValues = new ContentValues();
		contentValues.put("category", 0);
		contentValues.put("learned", 0);
		contentValues.put("lastChecked", 0);
		contentValues.put("nextReview", Vocabulary.getNextReview(0));
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
		contentValues.put("reading_used", "");
		contentValues.put("meaning_used", "");
		contentValues.put("category_history", StringHelper.toString(category_history));
		contentValues.put("answered", (byte) 0b1000);
		contentValues.put("quiz_preferences", 0b010101);
						  
		SQLiteDatabase db = this.getWritableDatabase();
		StringBuilder list = new StringBuilder();
		for (int i = 0; i < id.length; ++i)
		{
			list.append(id[i]);

			if (i < id.length - 1)
				list.append(", ");
		}
		db.update("vocab", contentValues, "id IN (" + list + ")", null);
	}
	
	public void resetVocabularyCategory(long lastChecked, int id)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues contentValues = new ContentValues();
		contentValues.put("category", 1);
		contentValues.put("nextReview", lastChecked + Vocabulary.getNextReview(1));
		
		int[] category_history = new int[32];
		category_history[category_history.length - 1] = 1;
		for (int i = 0; i < category_history.length - 1; ++i)
			category_history[i] = -1;

		contentValues.put("category_history", StringHelper.toString(category_history));
		
		db.update("vocab", contentValues, "id = ?", new String[] {"" + id});
	}
	
	public void learnVocabulary(int id, boolean known)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues contentValues = new ContentValues();
		contentValues.put("learned", 1);
		contentValues.put("lastChecked", 1);
		contentValues.put("category", 1);
		contentValues.put("nextReview", 1 + Vocabulary.getNextReview(1));
		if (known)
			contentValues.put("quiz_preferences", 0b1111111);
						  
		int[] category_history = new int[32];
		category_history[category_history.length - 1] = 1;
		for (int i = 0; i < category_history.length - 1; ++i)
			category_history[i] = -1;
		
		contentValues.put("category_history", StringHelper.toString(category_history));
		
		db.update("vocab", contentValues, "id = ?", new String[] {"" + id});
	}
	
	public void updateVocabularyLearned(int id, boolean learned)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues contentValues = new ContentValues();
		contentValues.put("learned", learned ? 1 : 0);

		db.update("vocab", contentValues, "id = ?", new String[] {"" + id});
	}
	
	public void updateVocabularySoundFile(int id, String soundFile)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues contentValues = new ContentValues();
		contentValues.put("soundFile", soundFile);

		db.update("vocab", contentValues, "id = ?", new String[] {"" + id});
	}
	
	public void updateVocabulariesLearned(boolean learned)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues contentValues = new ContentValues();
		contentValues.put("learned", learned ? 1 : 0);

		db.update("vocab", contentValues, "learned = ?", new String[] {learned ? "0" : "1"});
	}
	
	public void updateVocabularyQuizPreferences(int id, int kanji, int reading, int meaning, boolean quick)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues contentValues = new ContentValues();
		contentValues.put("quiz_preferences", 
						  (kanji)
						  | (reading << 2)
						  | (meaning << 4)
						  | (quick ? (byte) 0b1000000 : 0));
		
		db.update("vocab", contentValues, "id = ? ", new String[] { "" + id });
	}
	
	public void deleteVocabulary(int id)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor res =  db.rawQuery("SELECT kanji, sameReading, sameMeaning FROM vocab WHERE id = " + id, null);
		if (res.getCount() <= 0)
		{
			res.close();
			return;
		}

		res.moveToFirst();
		updateKanjiVocabularies(id, StringHelper.getKanji(res.getString(0)), false);
		updateSynonyms(id, StringHelper.toIntArray(res.getString(1)), StringHelper.toIntArray(res.getString(2)), false);
		res.close();
		
		db.delete("vocab", "id = ?", new String[] {"" + id});
	}
	
	public void deleteVocabularies()
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DELETE FROM vocab");
		
		ContentValues contentValues = new ContentValues();
		contentValues.put("vocabularies", StringHelper.toString(new int[0]));
		db.update("kanji", contentValues, "", null);
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
			i++;
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
			
			DialogHelper.createDialog(context, "Save", "Saved vocabulary database as \"" + filename1 + ".db\" to documents");
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
	

	public void importVocabulary(String line, ImportType importType, VocabularyType type, boolean learned, OnProcessSuccessListener listener)
	{
		if (!line.contains("-"))
		{
			if (listener != null)
				listener.onProcessSuccess(false);
				
			return;
		}
			
		final Vocabulary v = new Vocabulary(-1);
		String rest;
		String[] reading, meaning;

		if (!line.contains("【") || !line.contains("】"))
		{
			v.kanji = StringHelper.trim(line.substring(0, line.indexOf("-")));

			if (!StringHelper.isKana(v.kanji))
			{
				if (listener != null)
					listener.onProcessSuccess(false);

				return;
			}

			reading = new String[0];
			
			if (line.contains("(") && line.contains(")"))
			{
				rest = line.substring(line.indexOf("-") + 1, line.indexOf("("));
				if (line.indexOf(")") != StringHelper.trim(line).length() - 1)
					rest = rest + line.substring(line.indexOf(")") + 1, line.length());
				meaning = rest.split(",|;");

				v.additionalInfo = StringHelper.trim(line.substring(line.indexOf("(") + 1, line.indexOf(")")));
			}
			else
			{
				meaning = line.substring(line.indexOf("-") + 1, line.length()).split(",|;");
				v.additionalInfo = "";
			}
		}
		else
		{
			reading = line.substring(line.indexOf("【") + 1, line.indexOf("】")).split("/|／");
			
			for (int i = 0; i < reading.length; ++i)
				reading[i] = StringHelper.trim(reading[i]);
			
			v.kanji = StringHelper.trim(line.substring(0, line.indexOf("【")));
			
			if (line.contains("(") && line.contains(")"))
			{		
				reading = line.substring(line.indexOf("【") + 1, line.indexOf("】")).split("/|／");

				rest = line.substring(line.indexOf("-") + 1, line.indexOf("("));
				if (line.indexOf(")") != StringHelper.trim(line).length() - 1)
					rest = rest + line.substring(line.indexOf(")") + 1, line.length());
				meaning = rest.split(",|;");

				v.additionalInfo = StringHelper.trim(line.substring(line.indexOf("(") + 1, line.indexOf(")")));
			}
			else
			{
				meaning = line.substring(line.indexOf("-") + 1, line.length()).split(",|;");
				v.additionalInfo = "";
			}
		}

		for (int i = 0; i < meaning.length; ++i)
			meaning[i] = StringHelper.trim(meaning[i]);
		
		String[] reading_trimed = new String[reading.length];
		for (int i = 0; i < reading.length; ++i)
			reading_trimed[i] = StringHelper.trim(StringHelper.toHiragana(reading[i].replace("・", "")));

		v.reading = reading;
		v.reading_trimed = reading_trimed;
		v.meaning = meaning;
		v.reading_used = new int[reading.length];
		v.meaning_used = new int[meaning.length];
		v.added = System.currentTimeMillis();
		v.nextReview = Vocabulary.getNextReview(0);
		v.soundFile = "";
		v.imageFile = "";
		v.notes = "";
		v.showInfo = true;
		v.category_history = new int[32];
		v.category_history[v.category_history.length - 1] = v.category;
		for (int i = 0; i < v.category_history.length - 1; ++i)
			v.category_history[i] = -1;
			
		v.type = type;
		v.learned = learned;
		final ArrayList<Integer> sameReading = new ArrayList<>(), sameMeaning = new ArrayList<>();
		findSynonyms(getReadableDatabase(), v.kanji, v.reading_trimed, v.meaning, sameReading, sameMeaning);
		v.setSynonyms(sameReading, sameMeaning);
		v.nextReview = v.lastChecked + Vocabulary.getNextReview(v.category);
		updateVocabulary(v, importType, listener);
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

	public void exportToFile()
	{
		File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
		dir.mkdirs();
		
		String filename1 = "Vocabularies " + new DateFormat().getDateFormat(context).format(new Date()).replace('/', '-');

		String filename = dir.getAbsolutePath() + "/" + filename1 + ".txt";
		int i = 1;
		while (new File(filename).exists())
		{
			filename = dir.getAbsolutePath() + "/" + filename1 + " " + i + ".txt";
			i++;
		}
		
		try
		{
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor res =  db.rawQuery("SELECT type, kanji, reading, meaning, additionalInfo FROM vocab ORDER BY type", null);
			if (res.getCount() <= 0)
			{
				res.close();
				return;
			}
			
			res.moveToFirst();
			
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename)));
			
			String[] reading, meaning;
			String additionalInfo;
			int type;
			int lastType = -1;
			
			do
			{
				type = res.getInt(res.getColumnIndex("type"));
				if (type != lastType)
				{
					if (lastType != -1)
						writer.write("\n");
					writer.write(Vocabulary.types.get(type));
					writer.write(":\n");
					lastType = type;
				}
				
				reading = StringHelper.toStringArray(res.getString(res.getColumnIndex("reading")));
				meaning = StringHelper.toStringArray(res.getString(res.getColumnIndex("meaning")));
				writer.write(res.getString(res.getColumnIndex("kanji")));
				additionalInfo = res.getString(res.getColumnIndex("additionalInfo"));
				
				if (reading.length > 0)
				{
					writer.write("【");
					for (i = 0; i < reading.length - 1; ++i)
					{
						writer.write(reading[i] + " / ");
					}
					writer.write(reading[reading.length -1] + "】- ");
				}
				else
					writer.write(" - ");

				for (i = 0; i < meaning.length - 1; ++i)
				{
					writer.write(meaning[i] + "; ");
				}
				writer.write(meaning[meaning.length -1]);
				if (!additionalInfo.isEmpty())
					writer.write(" (" + additionalInfo + ")");
				writer.write("\n");
			}
			while (res.moveToNext());

			DialogHelper.createDialog(context, "Export", "Exported " + res.getCount() + (res.getCount() == 1 ? " vocabulary" : " vocabularies") + " as \"" + filename1 + ".txt\" to documents!");
			
			res.close();
			writer.close();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
			DialogHelper.createDialog(context, "Export failed", "An error occured:\n" + e.getMessage());
		}
	}
	
	public void findSynonyms(SQLiteDatabase db, String kanji, String[] reading_trimed, String[] meaning, ArrayList<Integer> sameReading, ArrayList<Integer> sameMeaning)
	{
		findSynonyms(db, kanji, -1, reading_trimed, meaning, sameReading, sameMeaning);
	}
	
	public void findSynonyms(SQLiteDatabase db, String kanji, int id, String[] reading_trimed, String[] meaning, ArrayList<Integer> sameReading, ArrayList<Integer> sameMeaning)
	{
		sameReading.clear();
		sameMeaning.clear();
	
		if (reading_trimed.length > 0 && !reading_trimed[0].isEmpty())
		{
			StringBuilder reading_matches = new StringBuilder();
			for (int i = 0; i < reading_trimed.length; ++i)
			{
				reading_matches.append("reading_trimed LIKE '%\\").append(reading_trimed[i].replace("'", "''")).append("\\%'");

				if (i < reading_trimed.length - 1)
					reading_matches.append(" OR ");
			}

			Cursor res =  db.rawQuery("SELECT id FROM vocab WHERE kanji != ? AND id != ? AND (" + reading_matches + ")", new String[] {kanji, "" + id});
			if (res.getCount() > 0)
			{
				res.moveToFirst();

				do
				{
					sameReading.add(res.getInt(0));
				}
				while (res.moveToNext());
			}
			
			res.close();
		}
		
		StringBuilder meaning_matches = new StringBuilder();
		for (int i = 0; i < meaning.length; ++i)
		{
			meaning_matches.append("meaning LIKE '%\\").append(meaning[i].replace("'", "''")).append("\\%'");

			if (i < meaning.length - 1)
				meaning_matches.append(" OR ");
		}

		Cursor res = db.rawQuery("SELECT id FROM vocab WHERE kanji != ? AND id != ? AND (" + meaning_matches + ")", new String[] {kanji, "" + id});
		if (res.getCount() > 0)
		{
			res.moveToFirst();

			do
			{
				sameMeaning.add(res.getInt(0));
			}
			while (res.moveToNext());
		}
		res.close();
	}
	
	public void updateSynonyms(int id, int[] sameReading, int[] sameMeaning, boolean add)
	{
		SQLiteDatabase db = getWritableDatabase();
		if (sameReading.length > 0)
		{
			StringBuilder list = new StringBuilder();
			for (int i = 0; i < sameReading.length; ++i)
			{
				list.append(sameReading[i]);

				if (i < sameReading.length - 1)
					list.append(", ");
			}
			
			if (add)
				db.execSQL("UPDATE vocab SET sameReading = sameReading || '" + id + "\\' WHERE id IN (" + list + ");");
			else
				db.execSQL("UPDATE vocab SET sameReading = replace(sameReading, '\\" + id + "\\', '\\' ) WHERE id IN (" + list + ");");
		}
		if (sameMeaning.length > 0)
		{
			StringBuilder list = new StringBuilder();
			for (int i = 0; i < sameMeaning.length; ++i)
			{
				list.append(sameMeaning[i]);

				if (i < sameMeaning.length - 1)
					list.append(", ");
			}
			if (add)
				db.execSQL("UPDATE vocab SET sameMeaning = sameMeaning || '" + id + "\\' WHERE id IN (" + list + ");");
			else
				db.execSQL("UPDATE vocab SET sameMeaning = replace(sameMeaning, '\\" + id + "\\', '\\' ) WHERE id IN (" + list + ");");
		}
	}
	
	public void updateKanjiVocabularies(int id, char[] kanji, boolean add)
	{
		SQLiteDatabase db = getWritableDatabase();
		if (kanji.length > 0)
		{
			StringBuilder list = new StringBuilder();
			for (int i = 0; i < kanji.length; ++i)
			{
				list.append((int) kanji[i]);

				if (i < kanji.length - 1)
					list.append(", ");
			}
			if (add)
				db.execSQL("UPDATE kanji SET vocabularies = vocabularies || '" + id + "\\' WHERE kanji IN (" + list + ");");
			else
				db.execSQL("UPDATE kanji SET vocabularies = replace(vocabularies, '\\" + id + "\\', '\\' ) WHERE kanji IN (" + list + ");");
		}
	}
	
	public int[] findVocabulariesForKanji(SQLiteDatabase db, char kanji)
	{
		Cursor res = db.rawQuery("SELECT id FROM vocab WHERE kanji LIKE '%" + kanji + "%'", null);
		int[] vocabularies = new int[res.getCount()];
		if (res.getCount() > 0)
		{
			res.moveToFirst();

			int i = 0;
			do
			{
				vocabularies[i++] = res.getInt(0);
			}
			while (res.moveToNext());
		}
		
		res.close();
		
		return vocabularies;
	}
	
	public char[] getKanji(SortType sortType, ShowType showType, boolean sortReversed, String searchQuery)
	{
		String searchStatement, orderStatement;

		switch (sortType)
		{
			case CATEGORY:
				orderStatement = "category";
				break;
			case TIME_ADDED:
				orderStatement = "added";
				break;
			case TYPE:
				orderStatement = "strokes";
				break;
			case NEXT_REVIEW:
				orderStatement = "learned DESC, nextReview";
				break;
			default:
				orderStatement = "category";
		}

		orderStatement = orderStatement + (sortReversed ? " DESC" : " ASC");

		String[] searchArgs = null;
		if (searchQuery != null && !searchQuery.isEmpty())
		{
			searchStatement = "(kanji LIKE ? OR reading_kun LIKE ? OR reading_on LIKE ? OR meaning LIKE ? OR notes LIKE ?)";
			orderStatement = "kanji = ? DESC, reading_kun LIKE ? DESC, reading_kun LIKE ? DESC, reading_on LIKE ? DESC, reading_on LIKE ? DESC, meaning LIKE ? DESC, meaning LIKE ? DESC, " + orderStatement;
			searchArgs = new String[] {StringHelper.trim(searchQuery), "%" + searchQuery + "%", "%" + searchQuery + "%", "%" + searchQuery + "%", "%" + searchQuery + "%",
				StringHelper.trim(searchQuery),
				"%\\" + searchQuery + "\\%", "%\\" + searchQuery + "%",
				"%\\" + searchQuery + "\\%", "%\\" + searchQuery + "%",
				"%\\" + searchQuery + "\\%", "%\\" + searchQuery + "%" };
		}
		else
			searchStatement = "kanji != " + (int) '\n';

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT kanji FROM kanji WHERE " + (showType == ShowType.LEARNED ? "learned = 1 AND " : showType == showType.UNLEARNED ? "learned = 0 AND " : "") + searchStatement + " ORDER BY " + orderStatement, searchArgs);
		if (res.getCount() <= 0)
		{
			res.close();
			return new char[0];
		}

		res.moveToFirst();

		final char[] kanji = new char[res.getCount()];
		int i = 0;

		do
		{
			kanji[i] = (char) res.getInt(0);
			i++;
		}
		while (res.moveToNext());

		res.close();

		return kanji;
	}

	public char[] getNewKanji()
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT kanji FROM kanji WHERE learned = 1 AND lastChecked = 0", null);
		if (res.getCount() <= 0)
		{
			res.close();
			return new char[0];
		}

		res.moveToFirst();

		char[] kanji = new char[res.getCount()];
		int i = 0;
		do
		{
			kanji[i] = (char) res.getInt(0);
			i++;
		}
		while (res.moveToNext());

		res.close();

		return kanji;
	}
	
	public int getIntKanji(char id, String column)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT " + column + " FROM kanji WHERE kanji = " + (int) id +" LIMIT 1", null);
		if (res.getCount() <= 0)
		{
			res.close();
			return -1;
		}

		res.moveToFirst();

		int i = res.getInt(0);
		res.close();
		return i;
	}

	public long getLongKanji(char id, String column)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT " + column + " FROM kanji WHERE kanji = " + (int) id + " LIMIT 1", null);
		if (res.getCount() <= 0)
		{
			res.close();
			return -1;
		}

		res.moveToFirst();

		long l = res.getLong(0);
		res.close();
		return l;
	}
	
	public Kanji getKanji(char kanji)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT * FROM kanji WHERE kanji = " + (int) kanji, null);
		if (res.getCount() <= 0)
		{
			res.close();
			return null;
		}

		res.moveToFirst();
		Kanji result = getKanji(res);
		res.close();

		return result;
	}
	
	public Kanji getKanji(Cursor res)
	{
		final Kanji kanji = new Kanji((char) res.getInt(res.getColumnIndex("kanji")));
		kanji.reading_kun = StringHelper.toStringArray(res.getString(res.getColumnIndex("reading_kun")));
		kanji.reading_on = StringHelper.toStringArray(res.getString(res.getColumnIndex("reading_on")));
		kanji.meaning = StringHelper.toStringArray(res.getString(res.getColumnIndex("meaning")));
		kanji.notes = res.getString(res.getColumnIndex("notes"));
		kanji.strokes = res.getInt(res.getColumnIndex("strokes"));
		
		kanji.category = res.getInt(res.getColumnIndex("category"));
		kanji.learned = res.getInt(res.getColumnIndex("learned")) == 1;
		
		kanji.added = res.getLong(res.getColumnIndex("added"));
		kanji.lastChecked = res.getLong(res.getColumnIndex("lastChecked"));
		kanji.nextReview = res.getLong(res.getColumnIndex("nextReview"));

		kanji.vocabularies = StringHelper.toIntArray(res.getString(res.getColumnIndex("vocabularies")));
		
		kanji.streak_kanji = res.getInt(res.getColumnIndex("streak_kanji"));
		kanji.streak_reading_kun = res.getInt(res.getColumnIndex("streak_reading_kun"));
		kanji.streak_reading_on = res.getInt(res.getColumnIndex("streak_reading_on"));
		kanji.streak_meaning = res.getInt(res.getColumnIndex("streak_meaning"));

		kanji.streak_kanji_best = res.getInt(res.getColumnIndex("streak_kanji_best"));
		kanji.streak_reading_kun_best = res.getInt(res.getColumnIndex("streak_reading_kun_best"));
		kanji.streak_reading_on_best = res.getInt(res.getColumnIndex("streak_reading_on_best"));
		kanji.streak_meaning_best = res.getInt(res.getColumnIndex("streak_meaning_best"));

		kanji.timesChecked_kanji = res.getInt(res.getColumnIndex("timesChecked_kanji"));
		kanji.timesChecked_reading = StringHelper.toIntArray(res.getString(res.getColumnIndex("timesChecked_reading")));
		kanji.timesChecked_reading_kun = res.getInt(res.getColumnIndex("timesChecked_reading_kun"));
		kanji.timesChecked_reading_on = res.getInt(res.getColumnIndex("timesChecked_reading_on"));
		kanji.timesChecked_meaning = res.getInt(res.getColumnIndex("timesChecked_meaning"));

		kanji.timesCorrect_kanji = res.getInt(res.getColumnIndex("timesCorrect_kanji"));
		kanji.timesCorrect_reading = StringHelper.toIntArray(res.getString(res.getColumnIndex("timesCorrect_reading")));
		kanji.timesCorrect_reading_kun = res.getInt(res.getColumnIndex("timesCorrect_reading_kun"));
		kanji.timesCorrect_reading_on = res.getInt(res.getColumnIndex("timesCorrect_reading_on"));
		kanji.timesCorrect_meaning = res.getInt(res.getColumnIndex("timesCorrect_meaning"));

		kanji.meaning_used = StringHelper.toIntArray(res.getString(res.getColumnIndex("meaning_used")));
		kanji.category_history = StringHelper.toIntArray(res.getString(res.getColumnIndex("category_history")));

		if (kanji.timesChecked_reading.length != kanji.reading_kun.length + kanji.reading_on.length || kanji.timesCorrect_reading.length != kanji.timesChecked_reading.length)
		{
			kanji.timesChecked_reading = new int[kanji.reading_kun.length + kanji.reading_on.length];
			kanji.timesCorrect_reading = new int[kanji.timesChecked_reading.length];
		}
		
		if (kanji.meaning_used.length != kanji.meaning.length)
			kanji.meaning_used = new int[kanji.meaning.length];

		int answered = res.getInt(res.getColumnIndex("answered"));
		kanji.answered_kanji = (answered & 0b1) != 0;
		kanji.answered_reading_kun = (answered & 0b10) != 0;
		kanji.answered_reading_on = (answered & 0b10) != 0;
		kanji.answered_meaning = (answered & 0b1000) != 0;
		kanji.answered_correct = (answered & 0b10000) != 0;
		kanji.imageFile = res.getString(res.getColumnIndex("imageFile"));
		return kanji;
	}
	
	public boolean existsKanji(char kanji)
	{
		if (kanji == '\n')
			return false;

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT kanji FROM kanji WHERE kanji = ? LIMIT 1", new String[] {"" + (int) kanji});
		if (res.getCount() <= 0)
		{
			res.close();
			return false;
		}

		res.close();
		return true;
	}
	
	public void updateKanji(final Kanji kanji)
	{
		updateKanji(kanji, ImportType.UPDATE, null);
	}
	
	public void updateKanji(final Kanji kanji, ImportType importType, final OnProcessSuccessListener listener)
	{
		final SQLiteDatabase db = this.getWritableDatabase();

		final ContentValues contentValues = new ContentValues();
		contentValues.put("kanji", (int) kanji.kanji);
		contentValues.put("reading_kun", StringHelper.toString(kanji.reading_kun));
		contentValues.put("reading_on", StringHelper.toString(kanji.reading_on));
		contentValues.put("meaning", StringHelper.toString(kanji.meaning));
		contentValues.put("notes", kanji.notes);
		contentValues.put("strokes", kanji.strokes);
		contentValues.put("category", kanji.category);
		contentValues.put("learned", kanji.learned);
		contentValues.put("added", kanji.added);
		contentValues.put("lastChecked", kanji.lastChecked);
		contentValues.put("nextReview", kanji.nextReview);
		contentValues.put("vocabularies", StringHelper.toString(kanji.vocabularies));
		contentValues.put("streak_kanji", kanji.streak_kanji);
		contentValues.put("streak_reading_kun", kanji.streak_reading_kun);
		contentValues.put("streak_reading_on", kanji.streak_reading_on);
		contentValues.put("streak_meaning", kanji.streak_meaning);
		contentValues.put("streak_kanji_best", kanji.streak_kanji_best);
		contentValues.put("streak_reading_kun_best", kanji.streak_reading_kun_best);
		contentValues.put("streak_reading_on_best", kanji.streak_reading_on_best);
		contentValues.put("streak_meaning_best", kanji.streak_meaning_best);
		contentValues.put("timesChecked_kanji", kanji.timesChecked_kanji);
		contentValues.put("timesChecked_reading", StringHelper.toString(kanji.timesChecked_reading));
		contentValues.put("timesChecked_reading_kun", kanji.timesChecked_reading_kun);
		contentValues.put("timesChecked_reading_on", kanji.timesChecked_reading_on);
		contentValues.put("timesChecked_meaning", kanji.timesChecked_meaning);
		contentValues.put("timesCorrect_kanji", kanji.timesCorrect_kanji);
		contentValues.put("timesCorrect_reading", StringHelper.toString(kanji.timesCorrect_reading));
		contentValues.put("timesCorrect_reading_kun", kanji.timesCorrect_reading_kun);
		contentValues.put("timesCorrect_reading_on", kanji.timesCorrect_reading_on);
		contentValues.put("timesCorrect_meaning", kanji.timesCorrect_meaning);
		contentValues.put("meaning_used", StringHelper.toString(kanji.meaning_used));
		contentValues.put("category_history", StringHelper.toString(kanji.category_history));
		contentValues.put("answered", 
						  (kanji.answered_kanji ? (byte) 0b1 : 0)
						  | (kanji.answered_reading_kun ? (byte) 0b10 : 0)
						  | (kanji.answered_reading_on ? (byte) 0b100 : 0)
						  | (kanji.answered_meaning ? (byte) 0b1000 : 0)
						  | (kanji.answered_correct ? (byte) 0b10000 : 0));
		contentValues.put("imageFile", kanji.imageFile);

		if (existsKanji(kanji.kanji))
		{
			int i, i1, index;
			Kanji old;
			switch (importType)
			{
				case KEEP:
					break;

				case UPDATE:
				case REPLACE:
					db.update("kanji", contentValues, "kanji = ?", new String[] {"" + (int) kanji.kanji});
					break;

				case REPLACE_KEEP_STATS:
					old = getKanji(kanji.kanji);

					if (!kanji.learned)
						contentValues.put("learned", old.learned);

					if (old.category > kanji.category)
					{
						contentValues.put("category", old.category);
						contentValues.put("category_history", StringHelper.toString(old.category_history));
					}

					contentValues.put("timesChecked_kanji", kanji.timesChecked_kanji + old.timesChecked_kanji);
					contentValues.put("timesChecked_reading_kun", kanji.timesChecked_reading_kun + old.timesChecked_reading_kun);
					contentValues.put("timesChecked_reading_on", kanji.timesChecked_reading_on + old.timesChecked_reading_on);
					contentValues.put("timesChecked_meaning", kanji.timesChecked_meaning + old.timesChecked_meaning);
					contentValues.put("timesCorrect_kanji", kanji.timesCorrect_kanji + old.timesCorrect_kanji);
					contentValues.put("timesCorrect_reading_kun", kanji.timesCorrect_reading_kun + old.timesCorrect_reading_kun);
					contentValues.put("timesCorrect_reading_on", kanji.timesCorrect_reading_on + old.timesCorrect_reading_on);
					contentValues.put("timesCorrect_meaning", kanji.timesCorrect_meaning + old.timesCorrect_meaning);
					contentValues.put("streak_kanji", kanji.streak_kanji + old.streak_meaning);
					contentValues.put("streak_reading_kun", kanji.streak_reading_kun + old.streak_reading_on);
					contentValues.put("streak_reading_on", kanji.streak_reading_on + old.streak_reading_on);
					contentValues.put("streak_meaning", kanji.streak_meaning + old.streak_meaning);
					contentValues.put("streak_kanji_best", Math.max(Math.max(kanji.streak_kanji + old.streak_kanji, old.streak_kanji_best), kanji.streak_kanji_best));
					contentValues.put("streak_reading_kun_best", Math.max(Math.max(kanji.streak_reading_kun + old.streak_reading_kun, old.streak_reading_kun_best), kanji.streak_reading_kun_best));
					contentValues.put("streak_reading_on_best", Math.max(Math.max(kanji.streak_reading_on + old.streak_reading_on, old.streak_reading_on_best), kanji.streak_reading_on_best));
					contentValues.put("streak_meaning_best", Math.max(Math.max(kanji.streak_meaning + old.streak_meaning, old.streak_meaning_best), kanji.streak_meaning_best));

					if (old.added < kanji.added)
						contentValues.put("added", old.added);

					if (old.lastChecked > kanji.lastChecked)
						contentValues.put("lastChecked", old.lastChecked);

					contentValues.put("nextReview", contentValues.getAsLong("lastChecked") + Vocabulary.getNextReview(contentValues.getAsInteger("category")));

					for (i = 0; i < old.reading_kun.length; ++i)
					{
						for (i1 = 0; i1 < kanji.reading_kun.length; ++i1)
							if (kanji.reading_kun[i1].equalsIgnoreCase(old.reading_kun[i]))
							{
								kanji.timesChecked_reading[i1] += old.timesChecked_reading[i];
								kanji.timesCorrect_reading[i1] += old.timesCorrect_reading[i];
							}
					}
					
					for (i = 0; i < old.reading_on.length; ++i)
					{
						for (i1 = 0; i1 < kanji.reading_on.length; ++i1)
							if (kanji.reading_on[i1].equalsIgnoreCase(old.reading_on[i]))
							{
								kanji.timesChecked_reading[i1 + kanji.reading_kun.length] += old.timesChecked_reading[i + old.reading_kun.length];
								kanji.timesCorrect_reading[i1 + kanji.reading_kun.length] += old.timesCorrect_reading[i + old.reading_kun.length];
							}
					}
					
					contentValues.put("timesChecked_reading", StringHelper.toString(kanji.timesChecked_reading));
					contentValues.put("timesCorrect_reading", StringHelper.toString(kanji.timesCorrect_reading));
					
					for (i = 0; i < old.meaning.length; ++i)
						for (i1 = 0; i1 < kanji.meaning.length; ++i1)
							if (kanji.meaning[i1].equalsIgnoreCase(old.meaning[i]))
								kanji.meaning_used[i1] += old.meaning_used[i];
					
					contentValues.put("meaning_used", StringHelper.toString(kanji.meaning_used));

					db.update("kanji", contentValues, "kanji = ?", new String[] {"" + (int) kanji.kanji});
					break;

				case MERGE:
					old = getKanji(kanji.kanji);

					final ArrayList<String> reading_kun = new ArrayList<>();
					final ArrayList<String> reading_on = new ArrayList<>();
					final ArrayList<Integer> timesChecked_kun = new ArrayList<>();
					final ArrayList<Integer> timesChecked_on = new ArrayList<>();
					final ArrayList<Integer> timesCorrect_kun = new ArrayList<>();
					final ArrayList<Integer> timesCorrect_on = new ArrayList<>();
					final ArrayList<String> meaning = new ArrayList<>();
					final ArrayList<Integer> meaning_used = new ArrayList<>();
					
					for (i = 0; i < kanji.reading_kun.length; ++i)
					{
						reading_kun.add(kanji.reading_kun[i]);
						timesChecked_kun.add(kanji.timesChecked_reading[i]);
						timesCorrect_kun.add(kanji.timesCorrect_reading[i]);
					}

					for (i = 0; i < old.reading_kun.length; ++i)
					{
						index = reading_kun.indexOf(old.reading_kun[i]);

						if (index == -1)
						{
							reading_kun.add(old.reading_kun[i]);
							timesChecked_kun.add(old.timesChecked_reading[i]);
							timesCorrect_kun.add(old.timesCorrect_reading[i]);
						}
						else
						{
							timesChecked_kun.set(index, timesChecked_kun.get(index) + old.timesChecked_reading[i]);
							timesCorrect_kun.set(index, timesCorrect_kun.get(index) + old.timesCorrect_reading[i]);
						}
					}

					for (i = 0; i < kanji.reading_on.length; ++i)
					{
						reading_on.add(kanji.reading_on[i]);
						timesChecked_on.add(kanji.timesChecked_reading[i + kanji.reading_kun.length]);
						timesCorrect_on.add(kanji.timesCorrect_reading[i + kanji.reading_kun.length]);
					}

					for (i = 0; i < old.reading_on.length; ++i)
					{
						index = reading_on.indexOf(old.reading_on[i]);

						if (index == -1)
						{
							reading_on.add(old.reading_on[i]);
							timesChecked_on.add(old.timesChecked_reading[i + old.reading_kun.length]);
							timesCorrect_on.add(old.timesCorrect_reading[i + old.reading_kun.length]);
						}
						else
						{
							timesChecked_on.set(index, timesChecked_on.get(index) + old.timesChecked_reading[i + old.reading_kun.length]);
							timesCorrect_on.set(index, timesCorrect_on.get(index) + old.timesCorrect_reading[i + old.reading_kun.length]);
						}
					}
					
					for (i = 0; i < kanji.meaning.length; ++i)
					{
						meaning.add(kanji.meaning[i]);
						meaning_used.add(kanji.meaning_used[i]);
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

					int[] timesChecked = new int[timesChecked_kun.size() + timesChecked_on.size()];
					int[] timesCorrect = new int[timesCorrect_kun.size() + timesCorrect_on.size()];
					
					for (i = 0; i < timesChecked_kun.size(); ++i)
					{
						timesChecked[i] = timesChecked_kun.get(i);
						timesCorrect[i] = timesCorrect_kun.get(i);
					}
					
					for (i = 0; i < timesChecked_on.size(); ++i)
					{
						timesChecked[i + timesChecked_kun.size()] = timesChecked_on.get(i);
						timesCorrect[i + timesChecked_kun.size()] = timesCorrect_on.get(i);
					}
					
					contentValues.put("timesChecked_reading", StringHelper.toString(timesChecked));
					contentValues.put("timesCorrect_reading", StringHelper.toString(timesCorrect));
					contentValues.put("reading_kun", StringHelper.toString(reading_kun));
					contentValues.put("reading_on", StringHelper.toString(reading_on));
					contentValues.put("meaning", StringHelper.toString(meaning));
					contentValues.put("notes", kanji.notes + (!kanji.notes.isEmpty() && !old.notes.isEmpty() && ! old.notes.equalsIgnoreCase(kanji.notes) ? ", " : "") + (old.notes.equalsIgnoreCase(kanji.notes) ? "" : old.notes));
					contentValues.put("meaning_used", StringHelper.toString(meaning_used));

					if (!kanji.learned)
						contentValues.put("learned", old.learned);

					if (old.category > kanji.category)
					{
						contentValues.put("category", old.category);
						contentValues.put("category_history", StringHelper.toString(old.category_history));
					}

					contentValues.put("timesChecked_kanji", kanji.timesChecked_kanji + old.timesChecked_kanji);
					contentValues.put("timesChecked_reading_kun", kanji.timesChecked_reading_kun + old.timesChecked_reading_kun);
					contentValues.put("timesChecked_reading_on", kanji.timesChecked_reading_on + old.timesChecked_reading_on);
					contentValues.put("timesChecked_meaning", kanji.timesChecked_meaning + old.timesChecked_meaning);
					contentValues.put("timesCorrect_kanji", kanji.timesCorrect_kanji + old.timesCorrect_kanji);
					contentValues.put("timesCorrect_reading_kun", kanji.timesCorrect_reading_kun + old.timesCorrect_reading_kun);
					contentValues.put("timesCorrect_reading_on", kanji.timesCorrect_reading_on + old.timesCorrect_reading_on);
					contentValues.put("timesCorrect_meaning", kanji.timesCorrect_meaning + old.timesCorrect_meaning);
					contentValues.put("streak_kanji", kanji.streak_kanji + old.streak_meaning);
					contentValues.put("streak_reading_kun", kanji.streak_reading_kun + old.streak_reading_on);
					contentValues.put("streak_reading_on", kanji.streak_reading_on + old.streak_reading_on);
					contentValues.put("streak_meaning", kanji.streak_meaning + old.streak_meaning);
					contentValues.put("streak_kanji_best", Math.max(Math.max(kanji.streak_kanji + old.streak_kanji, old.streak_kanji_best), kanji.streak_kanji_best));
					contentValues.put("streak_reading_kun_best", Math.max(Math.max(kanji.streak_reading_kun + old.streak_reading_kun, old.streak_reading_kun_best), kanji.streak_reading_kun_best));
					contentValues.put("streak_reading_on_best", Math.max(Math.max(kanji.streak_reading_on + old.streak_reading_on, old.streak_reading_on_best), kanji.streak_reading_on_best));
					contentValues.put("streak_meaning_best", Math.max(Math.max(kanji.streak_meaning + old.streak_meaning, old.streak_meaning_best), kanji.streak_meaning_best));

					if (old.added < kanji.added)
						contentValues.put("added", old.added);

					if (old.lastChecked > kanji.lastChecked)
						contentValues.put("lastChecked", old.lastChecked);

					contentValues.put("nextReview", contentValues.getAsLong("lastChecked") + Vocabulary.getNextReview(contentValues.getAsInteger("category")));
					
					if (kanji.imageFile.isEmpty())
						contentValues.put("imageFile", old.imageFile);

					contentValues.put("answered", (byte) 0b10000);

					db.update("kanji", contentValues, "kanji = ?", new String[] {"" + (int) kanji.kanji});

					break;

				case ASK:
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
					alertDialog.setMessage("Conflict");
					alertDialog.setMessage("The kanji 「" + kanji.kanji + "」is already in your database! What do you want to do?");
					alertDialog.setPositiveButton("Replace",
						new DialogInterface.OnClickListener() 
						{
							public void onClick(DialogInterface dialog, int which) 
							{
								db.update("kanji", contentValues, "kanji = ?", new String[] {"" + (int) kanji.kanji});
								
								if (listener != null)
									listener.onProcessSuccess(true);

								dialog.dismiss();
							}
						});
					alertDialog.setNegativeButton("Keep",
						new DialogInterface.OnClickListener() 
						{
							public void onClick(DialogInterface dialog, int which) 
							{
								if (listener != null)
									listener.onProcessSuccess(false);

								dialog.dismiss();
							}
						});
					alertDialog.setNeutralButton("Merge",
						new DialogInterface.OnClickListener() 
						{
							public void onClick(DialogInterface dialog, int which) 
							{
								updateKanji(kanji, ImportType.MERGE, listener);
								dialog.dismiss();
							}
						});
					alertDialog.show();
					return;
			}
		}
		else
			db.insert("kanji", null, contentValues);

		if (listener != null)
			listener.onProcessSuccess(true);
	}
	
	public void deleteKanji(char kanji)
	{
		this.getWritableDatabase().delete("kanji", "kanji = " + (int) kanji, null);
	}
	
	public void updateKanjiLearned(char kanji, boolean learned)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues contentValues = new ContentValues();
		contentValues.put("learned", learned ? 1 : 0);

		db.update("kanji", contentValues, "kanji = " + (int) kanji, null);
	}
	
	public void resetKanji(char... id)
	{
		int[] category_history = new int[32];
		category_history[category_history.length - 1] = 0;
		for (int i = 0; i < category_history.length - 1; ++i)
			category_history[i] = -1;

		ContentValues contentValues = new ContentValues();
		contentValues.put("category", 0);
		contentValues.put("learned", 0);
		contentValues.put("lastChecked", 0);
		contentValues.put("nextReview", Vocabulary.getNextReview(0));
		contentValues.put("streak_kanji", 0);
		contentValues.put("streak_reading_kun", 0);
		contentValues.put("streak_reading_on", 0);
		contentValues.put("streak_meaning", 0);
		contentValues.put("streak_kanji_best", 0);
		contentValues.put("streak_reading_kun_best", 0);
		contentValues.put("streak_reading_on_best", 0);
		contentValues.put("streak_meaning_best", 0);
		contentValues.put("timesChecked_kanji", 0);
		contentValues.put("timesChecked_reading_kun", 0);
		contentValues.put("timesChecked_reading_on", 0);
		contentValues.put("timesChecked_meaning", 0);
		contentValues.put("timesCorrect_kanji", 0);
		contentValues.put("timesCorrect_reading_kun", 0);
		contentValues.put("timesCorrect_reading_on", 0);
		contentValues.put("timesCorrect_meaning", 0);
		contentValues.put("timesChecked_reading", "");
		contentValues.put("timesCorrect_reading", "");
		contentValues.put("meaning_used", "");
		contentValues.put("category_history", StringHelper.toString(category_history));
		contentValues.put("answered", (byte) 0b10000);

		SQLiteDatabase db = this.getWritableDatabase();
		StringBuilder list = new StringBuilder();
		for (int i = 0; i < id.length; ++i)
		{
			list.append((int) id[i]);

			if (i < id.length - 1)
				list.append(", ");
		}
		db.update("kanji", contentValues, "kanji IN (" + list + ")", null);
	}

	public void resetKanjiCategory(long lastChecked, char id)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues contentValues = new ContentValues();
		contentValues.put("category", 1);
		contentValues.put("nextReview", lastChecked + Vocabulary.getNextReview(1));

		int[] category_history = new int[32];
		category_history[category_history.length - 1] = 1;
		for (int i = 0; i < category_history.length - 1; ++i)
			category_history[i] = -1;

		contentValues.put("category_history", StringHelper.toString(category_history));

		db.update("kanji", contentValues, "kanji = " + (int) id, null);
	}
}
