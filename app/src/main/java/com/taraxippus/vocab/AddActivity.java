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
import com.taraxippus.vocab.util.DialogHelper;
import com.taraxippus.vocab.util.StringHelper;
import com.taraxippus.vocab.vocabulary.QuestionType;
import com.taraxippus.vocab.vocabulary.Vocabulary;
import com.taraxippus.vocab.vocabulary.VocabularyType;
import com.taraxippus.vocab.vocabulary.DBHelper;
import com.taraxippus.vocab.vocabulary.ImportType;

public class AddActivity extends AppCompatActivity
{
	DBHelper dbHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		dbHelper = new DBHelper(this);
		
		setContentView(R.layout.activity_add);

		if (getIntent() != null && getIntent().getAction() == Intent.ACTION_SEND)
		{
			String text = getIntent().getStringExtra(Intent.EXTRA_TEXT);
			
			if (StringHelper.isKanaOrKanji(text))
				((TextView) findViewById(R.id.text_kanji)).setText(text);
			
			else
				((TextView) findViewById(R.id.text_meaning)).setText(text);
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
						DialogHelper.createDialog(AddActivity.this, (edit ? "Edit " : "Add ") + (kanji.isEmpty() ? "Vocabulary" : kanji), "Please enter kanji, reading and meaning for the vocabulary! (You can leave out the reading if the kanji is written in kana only)");
						
					else
					{
						String[] reading = reading1.split(";");
						for (int i = 0; i < reading.length; ++i)
							reading[i] = StringHelper.trim(reading[i]);

						String[] meaning = meaning1.split(";");
						for (int i = 0; i < meaning.length; ++i)
							meaning[i] = StringHelper.trim(meaning[i]);

						VocabularyType type = VocabularyType.values()[((Spinner) findViewById(R.id.spinner_type)).getSelectedItemPosition()];

						Vocabulary vocab = vocab = new Vocabulary(type, kanji, reading, meaning, additionalInfo, notes);
						vocab.showInfo = ((CheckBox) findViewById(R.id.checkbox_show_info)).isChecked();
						vocab.imageFile = imageUrl;
						
						if (edit)
							dbHelper.updateVocabulary(vocab, getIntent().getIntExtra("id", -1), ImportType.REPLACE_KEEP_STATS);
						else
							dbHelper.updateVocabulary(vocab, -1, ImportType.ASK);
							
						if (getIntent() == null || getIntent().getAction() != Intent.ACTION_SEND)
						{
							Intent intent = new Intent(AddActivity.this, DetailActivity.class);
							intent.putExtra("id", dbHelper.getId(vocab.kanji));
							startActivity(intent);
						}
							
						finish();
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
