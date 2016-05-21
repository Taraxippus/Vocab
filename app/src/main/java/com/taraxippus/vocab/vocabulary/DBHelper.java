package com.taraxippus.vocab.vocabulary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.taraxippus.vocab.util.SaveHandler;
import com.taraxippus.vocab.util.StringHelper;

public class DBHelper extends SQLiteOpenHelper
{
	public DBHelper(Context context)
	{
		super(context, "Vocab.db" , null, SaveHandler.VERSION);
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
			+ "imageFile text,"
			+ ");"
		);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		if (oldVersion < 15)
			db.execSQL("ALTER TABLE vocab ADD COLUMN column int DEFAULT 0;");
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
		
		Vocabulary vocabulary = new Vocabulary(null, 
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
		
		res.close();
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
		Cursor res =  db.rawQuery("SELECT * FROM vocab WHERE kanji = ?", new String[] {kanji});
		if (res.getCount() <= 0)
		{
			res.close();
			return false;
		}

		res.close();
		return true;
	}

	public void updateVocabulary(Vocabulary vocabulary)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues contentValues = new ContentValues();
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

		if (exists(vocabulary.kanji))
			db.update("vocab", contentValues, "kanji = ?", new String[] {vocabulary.kanji});
		
		else
			db.insert("vocab", null, contentValues);
		
	}
}
