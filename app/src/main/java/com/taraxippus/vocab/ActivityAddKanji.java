package com.taraxippus.vocab;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.taraxippus.vocab.ActivityAdd;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.dialog.DialogHelper;
import com.taraxippus.vocab.util.JishoHelper;
import com.taraxippus.vocab.util.NotificationHelper;
import com.taraxippus.vocab.util.OnProcessSuccessListener;
import com.taraxippus.vocab.util.StringHelper;
import com.taraxippus.vocab.vocabulary.DBHelper;
import com.taraxippus.vocab.vocabulary.ImportType;
import com.taraxippus.vocab.vocabulary.Kanji;
import com.taraxippus.vocab.vocabulary.QuestionType;
import com.taraxippus.vocab.vocabulary.Vocabulary;
import java.util.Locale;

public class ActivityAddKanji extends AppCompatActivity
{
	private DBHelper dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		dbHelper = new DBHelper(this);
		final boolean edit;
		
		setContentView(R.layout.activity_add_kanji);
		final TextView text_kanji = (TextView) findViewById(R.id.text_kanji);
		final TextView text_reading_kun = (TextView) findViewById(R.id.text_reading_kun);
		final TextView text_reading_on = (TextView) findViewById(R.id.text_reading_on);
		final TextView text_meaning = (TextView) findViewById(R.id.text_meaning);
		final TextView text_strokes = (TextView) findViewById(R.id.text_strokes);
		final TextView text_notes = (TextView) findViewById(R.id.text_notes);
		final TextView text_image = (TextView) findViewById(R.id.text_image);
		final Button button_add = (Button) findViewById(R.id.button_add);
		final TextView text_number = (TextView) findViewById(R.id.text_number);
		final View progress_jisho = findViewById(R.id.progress_jisho);
		
		text_kanji.setTextLocale(Locale.JAPANESE);
		text_reading_on.setTextLocale(Locale.JAPANESE);
		text_reading_kun.setTextLocale(Locale.JAPANESE);
		text_notes.setTextLocale(Locale.JAPANESE);

		final int kanjiNumber = dbHelper.getKanjiCount("kanji = kanji") + 1;
		
		if (getIntent() != null && getIntent().hasExtra("kanji"))
		{
			char id = getIntent().getCharExtra("kanji", '\n');
			text_kanji.setText("" + id);
			
			if (dbHelper.existsKanji(id))
			{
				edit = true;
				button_add.setText("Edit");
				
				Kanji kanji = dbHelper.getKanji(id);

				setTitle("Edit " + kanji.kanji);

				StringBuilder tmp = new StringBuilder();
				if (kanji.reading_kun.length != 0)
				{
					for (int i = 0; i < kanji.reading_kun.length - 1; ++i)
					{
						tmp.append(kanji.reading_kun[i]);
						tmp.append("、");
					}

					tmp.append(kanji.reading_kun[kanji.reading_kun.length - 1]);
				}
				text_reading_kun.setText(tmp.toString());
				
				tmp.setLength(0);
				if (kanji.reading_on.length != 0)
				{
					for (int i = 0; i < kanji.reading_on.length - 1; ++i)
					{
						tmp.append(kanji.reading_on[i]);
						tmp.append("、");
					}

					tmp.append(kanji.reading_on[kanji.reading_on.length - 1]);
				}
				text_reading_on.setText(tmp.toString());
				
				tmp.setLength(0);
				for (int i = 0; i < kanji.meaning.length - 1; ++i)
				{
					tmp.append(kanji.meaning[i]);
					tmp.append("; ");
				}

				tmp.append(kanji.meaning[kanji.meaning.length - 1]);

				text_meaning.setText(tmp.toString());
				text_notes.setText(kanji.notes);
				text_strokes.setText("" + kanji.strokes);
				text_image.setText(kanji.imageFile);
			}
			else
				edit = false;
		}
		else
			edit = false;

		text_kanji.setOnFocusChangeListener(new View.OnFocusChangeListener()
			{
				@Override
				public void onFocusChange(View p1, boolean focus)
				{
					if (!focus)
					{
						if (dbHelper.existsKanji(StringHelper.trim(text_kanji.getText().toString()).charAt(0)))
							text_number.setText("Alreasy exists");
						else
							text_number.setText("Nr. " + kanjiNumber);
					}
				}
			});
		
		View button_info = findViewById(R.id.button_info);
		button_info.setVisibility(edit ? View.GONE : View.VISIBLE);
		text_number.setVisibility(edit ? View.GONE : View.VISIBLE);
		text_number.setText("Nr. " + kanjiNumber);
		View.OnClickListener listener = new View.OnClickListener()
		{
			@Override
			public void onClick(View p1)
			{
				if (progress_jisho.getVisibility() != View.VISIBLE)
				{
					String kanji1 = StringHelper.trim(text_kanji.getText().toString());

					if (kanji1.isEmpty())
						DialogHelper.createDialog(ActivityAddKanji.this, "Add Kanji", "Enter kanji to autofill the rest with information from jisho.org");

					else
					{
						progress_jisho.setVisibility(View.VISIBLE);

						final Kanji kanji = new Kanji(kanji1.charAt(0));
						JishoHelper.importKanji(ActivityAddKanji.this, kanji, new OnProcessSuccessListener()
							{
								@Override
								public void onProcessSuccess(Object... args)
								{
									if (isDestroyed())
										return;

									progress_jisho.setVisibility(View.INVISIBLE);

									if (args[0] == null)
									{
										Toast.makeText(ActivityAddKanji.this, "An error occured", 0).show();
										return;
									}

									text_kanji.setText("" + kanji.kanji);
									
									StringBuilder tmp = new StringBuilder();
									if (kanji.reading_kun.length != 0)
									{
										for (int i = 0; i < kanji.reading_kun.length - 1; ++i)
										{
											tmp.append(kanji.reading_kun[i]);
											tmp.append("、");
										}

										tmp.append(kanji.reading_kun[kanji.reading_kun.length - 1]);
									}
									text_reading_kun.setText(tmp.toString());

									tmp.setLength(0);
									if (kanji.reading_on.length != 0)
									{
										for (int i = 0; i < kanji.reading_on.length - 1; ++i)
										{
											tmp.append(kanji.reading_on[i]);
											tmp.append("、");
										}

										tmp.append(kanji.reading_on[kanji.reading_on.length - 1]);
									}
									text_reading_on.setText(tmp.toString());

									tmp.setLength(0);
									for (int i = 0; i < kanji.meaning.length - 1; ++i)
									{
										tmp.append(kanji.meaning[i]);
										tmp.append("; ");
									}

									tmp.append(kanji.meaning[kanji.meaning.length - 1]);

									text_meaning.setText(tmp.toString());
									text_notes.setText(kanji.notes);
									text_strokes.setText("" + kanji.strokes);
								}
							});
					}
				}
			}
		};
		button_info.setOnClickListener(listener);
		
		findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					finish();
				}
			});
		button_add.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					char id = text_kanji.getText().length() == 0 ? '\n' : text_kanji.getText().charAt(0);
					String reading_kun1 = text_reading_kun.getText().toString();
					String reading_on1 = text_reading_on.getText().toString();
					String meaning1 = text_meaning.getText().toString();
					String notes = StringHelper.trim(text_notes.getText().toString());
					String imageUrl = StringHelper.trim(text_image.getText().toString());
					int strokes = text_strokes.getText().length() == 0 ? 0 : Integer.parseInt(text_strokes.getText().toString());
					
					if (meaning1.length() == 0 || !StringHelper.isKanji(id) || id == '々' || reading_kun1.length() == 0 && reading_on1.length() == 0)
						DialogHelper.createDialog(ActivityAddKanji.this, (edit ? "Edit " : "Add ") + (id == '\n' ? "Kanji" : id), "Please enter a kanji, at least one reading in kana and a meaning! (You can leave out either kunyomi or onyomi)");
					
					else
					{
						String[] reading_kun = reading_kun1.split(";|、");
						int i1 = 0;
						for (int i = 0; i < reading_kun.length; ++i)
						{
							reading_kun[i1] = StringHelper.toHiragana(StringHelper.trim(reading_kun[i]));

							if (!reading_kun[i1].isEmpty())
								i1++;
						}
						
						String[] reading_kun_trimed = new String[i1];
						for (int i = 0; i < i1; ++i)
							reading_kun_trimed[i] = reading_kun[i];
							
						String[] reading_on = reading_on1.split(";|、");
						i1 = 0;
						for (int i = 0; i < reading_on.length; ++i)
						{
							reading_on[i1] = StringHelper.toKatakana(StringHelper.trim(reading_on[i]));

							if (!reading_on[i1].isEmpty() || !StringHelper.isKana(reading_on[i1]))
								i1++;
						}
						
						String[] reading_on_trimed = new String[i1];
						for (int i = 0; i < i1; ++i)
							reading_on_trimed[i] = reading_on[i];
						
						String[] meaning = meaning1.split(";");
						i1 = 0;
						for (int i = 0; i < meaning.length; ++i)
						{
							meaning[i1] = StringHelper.trim(meaning[i]);

							if (!meaning[i1].isEmpty())
								i1++;
						}

						String[] meaning_trimed = new String[i1];
						for (int i = 0; i < i1; ++i)
							meaning_trimed[i] = meaning[i];

						if (meaning_trimed.length == 0 || !StringHelper.isKanji(id) || id == '々' || reading_kun_trimed.length == 0 && reading_on_trimed.length == 0)
						{
							DialogHelper.createDialog(ActivityAddKanji.this, (edit ? "Edit " : "Add ") + (id == '\n' ? "Kanji" : id), "Please enter a kanji, at least one reading in kana and a meaning! (You can leave out either kunyomi or onyomi)");

							return;
						}

						final Kanji kanji = new Kanji(id);

						kanji.strokes = strokes;
						kanji.reading_kun = reading_kun_trimed;
						kanji.reading_on = reading_on_trimed;
						kanji.meaning = meaning_trimed;
						kanji.notes = notes;
						kanji.meaning_used = new int[meaning_trimed.length];
						kanji.added = System.currentTimeMillis();
						kanji.timesChecked_reading = new int[kanji.reading_kun.length + kanji.reading_on.length];
						kanji.timesCorrect_reading = new int[kanji.reading_kun.length + kanji.reading_on.length];
						
						kanji.category_history = new int[32];
						kanji.category_history[kanji.category_history.length - 1] = kanji.category;
						for (int i = 0; i < kanji.category_history.length - 1; ++i)
							kanji.category_history[i] = -1;

						kanji.imageFile = imageUrl;
						kanji.vocabularies = dbHelper.findVocabulariesForKanji(dbHelper.getReadableDatabase(), kanji.kanji);
						kanji.nextReview = kanji.lastChecked + Vocabulary.getNextReview(kanji.category);

						final OnProcessSuccessListener showKanji = new OnProcessSuccessListener()
						{
							@Override
							public void onProcessSuccess(Object[] args)
							{
								Intent intent = new Intent(ActivityAddKanji.this, ActivityDetailKanji.class);
								intent.putExtra("kanji", kanji.kanji);
								startActivity(intent);
								
								sendBroadcast(new Intent(ActivityAddKanji.this, NotificationHelper.class));
								finish();
							}
						};

						dbHelper.updateKanji(kanji, edit ? ImportType.REPLACE_KEEP_STATS : ImportType.ASK, showKanji);
						
						if (edit && getIntent().getCharExtra("kanji", '\n') != kanji.kanji)
							DialogHelper.createDialog(ActivityAddKanji.this, "Changed kanji character", "Because you changed the kanji, the old version was kept. You can delete it manually.");
					}
				}
			});
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		dbHelper.close();
	}
}
