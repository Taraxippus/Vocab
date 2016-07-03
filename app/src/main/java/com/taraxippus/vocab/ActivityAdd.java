package com.taraxippus.vocab;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.dialog.DialogHelper;
import com.taraxippus.vocab.dialog.ImportDialog;
import com.taraxippus.vocab.util.NotificationHelper;
import com.taraxippus.vocab.util.OnProcessSuccessListener;
import com.taraxippus.vocab.util.StringHelper;
import com.taraxippus.vocab.vocabulary.DBHelper;
import com.taraxippus.vocab.vocabulary.ImportType;
import com.taraxippus.vocab.vocabulary.QuestionType;
import com.taraxippus.vocab.vocabulary.Vocabulary;
import com.taraxippus.vocab.vocabulary.VocabularyType;
import java.util.ArrayList;

public class ActivityAdd extends AppCompatActivity
{
	private DBHelper dbHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		dbHelper = new DBHelper(this);
		
		setContentView(R.layout.activity_add);

		if (getIntent() != null && getIntent().getAction() == Intent.ACTION_SEND)
		{
			String text = getIntent().getStringExtra(Intent.EXTRA_TEXT);
			
			String[] lines = text.split("\n");
			boolean flag = false;
			for (int i = 0; i < lines.length; ++i)
				if (dbHelper.isVocabulary(lines[i]))
					flag = true;
				
			
			if (flag)
			{
				Bundle bundle = new Bundle();
				bundle.putString("text", text);

				ImportDialog dialog = new ImportDialog();
				dialog.setArguments(bundle);
				dialog.show(getFragmentManager(), "import");
				return;
			}
			else
			{
				if (StringHelper.isKanaOrKanji(text))
					((TextView) findViewById(R.id.text_kanji)).setText(text);

				else
					((TextView) findViewById(R.id.text_meaning)).setText(text);
			}
		}
		
		Spinner spinner_type = (Spinner) findViewById(R.id.spinner_type);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Vocabulary.types);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_type.setAdapter(adapter);
		spinner_type.setSelection(0);
		
		final boolean edit;
		
		if (getIntent() != null && getIntent().hasExtra("id"))
		{
			edit = true;
			((Button) findViewById(R.id.button_add)).setText("Edit");

			Vocabulary vocab = dbHelper.getVocabulary(getIntent().getIntExtra("id", 0));
			
			setTitle("Edit " + vocab.kanji);
			
			((TextView) findViewById(R.id.text_kanji)).setText(vocab.correctAnswer(QuestionType.KANJI));
			StringBuilder reading = new StringBuilder();
			if (vocab.reading.length != 0)
			{
				for (int i = 0; i < vocab.reading.length - 1; ++i)
				{
					reading.append(vocab.reading[i]);
					reading.append("; ");
				}

				reading.append(vocab.reading[vocab.reading.length - 1]);
			}

			((TextView) findViewById(R.id.text_reading)).setText(reading.toString());
			StringBuilder meaning = new StringBuilder();
			for (int i = 0; i < vocab.meaning.length - 1; ++i)
			{
				meaning.append(vocab.meaning[i]);
				meaning.append("; ");
			}

			meaning.append(vocab.meaning[vocab.meaning.length - 1]);

			((TextView) findViewById(R.id.text_meaning)).setText(meaning.toString());
			((TextView) findViewById(R.id.text_additional_info)).setText(vocab.additionalInfo);
			((TextView) findViewById(R.id.text_notes)).setText(vocab.notes);
			((TextView) findViewById(R.id.text_image)).setText(vocab.imageFile);

			spinner_type.setSelection(vocab.type.ordinal());

			((CheckBox) findViewById(R.id.checkbox_show_info)).setChecked(vocab.showInfo);
		}
		else
			edit = false;

		findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener()
		{
				@Override
				public void onClick(View p1)
				{
					finish();
				}
		});
		findViewById(R.id.button_add).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					String kanji = StringHelper.trim(((TextView)findViewById(R.id.text_kanji)).getText().toString());
					String reading1 = StringHelper.toHiragana(StringHelper.trim(((TextView)findViewById(R.id.text_reading)).getText().toString()));
					String meaning1 = StringHelper.trim(((TextView)findViewById(R.id.text_meaning)).getText().toString());
					String additionalInfo = StringHelper.trim(((TextView)findViewById(R.id.text_additional_info)).getText().toString());
					String notes = StringHelper.trim(((TextView)findViewById(R.id.text_notes)).getText().toString());
					String imageUrl = StringHelper.trim(((TextView)findViewById(R.id.text_image)).getText().toString());

					if (kanji.isEmpty() || meaning1.isEmpty() || (!StringHelper.isKana(kanji)) && reading1.isEmpty())
						DialogHelper.createDialog(ActivityAdd.this, (edit ? "Edit " : "Add ") + (kanji.isEmpty() ? "Vocabulary" : kanji), "Please enter kanji, reading and meaning for the vocabulary! (You can leave out the reading if the kanji is written in kana only)");
						
					else
					{
						String[] reading = reading1.split(";");
						for (int i = 0; i < reading.length; ++i)
							reading[i] = StringHelper.trim(reading[i]);

						String[] reading_trimed = new String[reading.length];
						for (int i = 0; i < reading.length; ++i)
							reading_trimed[i] = reading[i].replace("・", "");
						
						String[] meaning = meaning1.split(";");
						for (int i = 0; i < meaning.length; ++i)
							meaning[i] = StringHelper.trim(meaning[i]);

						final Vocabulary vocab = new Vocabulary();
						
						vocab.type = VocabularyType.values()[((Spinner) findViewById(R.id.spinner_type)).getSelectedItemPosition()];
						vocab.kanji = kanji;
						vocab.reading = reading;
						vocab.reading_trimed = reading_trimed;
						vocab.meaning = meaning;
						vocab.additionalInfo = additionalInfo;
						vocab.notes = notes;
						vocab.reading_used = new int[reading.length];
						vocab.meaning_used = new int[meaning.length];
						vocab.added = System.currentTimeMillis();
						vocab.soundFile = "";
						
						vocab.category_history = new int[32];
						vocab.category_history[vocab.category_history.length - 1] = vocab.category;
						for (int i = 0; i < vocab.category_history.length - 1; ++i)
							vocab.category_history[i] = -1;
							
						vocab.showInfo = ((CheckBox) findViewById(R.id.checkbox_show_info)).isChecked();
						vocab.imageFile = imageUrl;
						final ArrayList<Integer> sameReading = new ArrayList<>(), sameMeaning = new ArrayList<>();
						dbHelper.findSynonyms(dbHelper.getReadableDatabase(), vocab.kanji, vocab.reading, vocab.meaning, sameReading, sameMeaning);
						vocab.setSynonyms(sameReading, sameMeaning);
						vocab.nextReview = vocab.lastChecked + Vocabulary.getNextReview(vocab.category);
						
						final OnProcessSuccessListener showVocabulary = new OnProcessSuccessListener()
						{
							@Override
							public void onProcessSuccess(Object[] args)
							{
								if (getIntent() == null || getIntent().getAction() != Intent.ACTION_SEND)
								{
									Intent intent = new Intent(ActivityAdd.this, ActivityDetail.class);
									intent.putExtra("id", dbHelper.getId(vocab.kanji));
									startActivity(intent);
								}
								sendBroadcast(new Intent(ActivityAdd.this, NotificationHelper.class));
								finish();
							}
						};
						
						dbHelper.updateVocabulary(vocab, getIntent() == null ? -1 : getIntent().getIntExtra("id", -1), edit ? ImportType.REPLACE_KEEP_STATS : ImportType.ASK, showVocabulary);
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
