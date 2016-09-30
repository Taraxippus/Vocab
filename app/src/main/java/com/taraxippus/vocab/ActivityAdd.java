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
import java.util.Locale;
import com.taraxippus.vocab.vocabulary.Kanji;

public class ActivityAdd extends AppCompatActivity
{
	private DBHelper dbHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		dbHelper = new DBHelper(this);
		
		setContentView(R.layout.activity_add);
		
		final TextView text_kanji = (TextView) findViewById(R.id.text_kanji);
		final TextView text_reading = (TextView) findViewById(R.id.text_reading);
		final TextView text_meaning = (TextView) findViewById(R.id.text_meaning);
		final TextView text_additional_info = (TextView) findViewById(R.id.text_additional_info);
		final TextView text_notes = (TextView) findViewById(R.id.text_notes);
		final TextView text_image = (TextView) findViewById(R.id.text_image);
		final Button button_add = (Button) findViewById(R.id.button_add);
		final CheckBox checkbox_show_info = (CheckBox) findViewById(R.id.checkbox_show_info);
		final View progress_jisho = findViewById(R.id.progress_jisho);
		
		text_kanji.setTextLocale(Locale.JAPANESE);
		text_reading.setTextLocale(Locale.JAPANESE);
		text_notes.setTextLocale(Locale.JAPANESE);
		
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
			}
			else
			{
				if (StringHelper.isKanaOrKanji(text))
					text_kanji.setText(text);

				else
					text_meaning.setText(text);
			}
		}
		
		final Spinner spinner_type = (Spinner) findViewById(R.id.spinner_type);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Vocabulary.types);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_type.setAdapter(adapter);
		spinner_type.setSelection(0);
		
		final boolean edit;
		
		if (getIntent() != null && getIntent().hasExtra("id"))
		{
			edit = true;
			button_add.setText("Edit");

			Vocabulary vocab = dbHelper.getVocabulary(getIntent().getIntExtra("id", 0));
			
			setTitle("Edit " + vocab.kanji);
			
			text_kanji.setText(vocab.correctAnswer(QuestionType.KANJI));
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

			text_reading.setText(reading.toString());
			StringBuilder meaning = new StringBuilder();
			for (int i = 0; i < vocab.meaning.length - 1; ++i)
			{
				meaning.append(vocab.meaning[i]);
				meaning.append("; ");
			}

			meaning.append(vocab.meaning[vocab.meaning.length - 1]);

			text_meaning.setText(meaning.toString());
			text_additional_info.setText(vocab.additionalInfo);
			text_notes.setText(vocab.notes);
			text_image.setText(vocab.imageFile);
			spinner_type.setSelection(vocab.type.ordinal());
			checkbox_show_info.setChecked(vocab.showInfo);
		}
		else
			edit = false;

		findViewById(R.id.button_info).setOnClickListener(new View.OnClickListener()
		{
				@Override
				public void onClick(View p1)
				{
					if (progress_jisho.getVisibility() != View.VISIBLE)
					{
						if (text_kanji.getText().toString().isEmpty())
							DialogHelper.createDialog(ActivityAdd.this, (edit ? "Edit " : "Add ") + "Vocabulary", "Enter kanji to autofill the rest with information from jisho.org");
						
						else
							progress_jisho.setVisibility(View.VISIBLE);
					}
				}
		});
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
					String kanji = StringHelper.trim(text_kanji.getText().toString());
					String reading1 = text_reading.getText().toString();
					String meaning1 = text_meaning.getText().toString();
					String additionalInfo = StringHelper.trim(text_additional_info.getText().toString());
					String notes = StringHelper.trim(text_notes.getText().toString());
					String imageUrl = StringHelper.trim(text_image.getText().toString());

					if (kanji.isEmpty() || meaning1.isEmpty() || (!StringHelper.isKana(kanji)) && reading1.isEmpty())
						DialogHelper.createDialog(ActivityAdd.this, (edit ? "Edit " : "Add ") + (kanji.isEmpty() ? "Vocabulary" : kanji), "Please enter kanji, reading and meaning for the vocabulary! (You can leave out the reading if the kanji is written in kana only)");
						
					else
					{
						String[] reading = reading1.split(";|、");
						int i1 = 0;
						for (int i = 0; i < reading.length; ++i)
						{
							reading[i1] = StringHelper.trim(reading[i]);
							
							if (!reading[i1].isEmpty())
								i1++;
						}
							
						String[] reading_trimed = new String[i1];
						for (int i = 0; i < i1; ++i)
							reading_trimed[i] = StringHelper.toHiragana(reading[i].replace("・", ""));
						
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
						
						if (meaning_trimed.length == 0 || !StringHelper.isKana(kanji) && reading_trimed.length == 0)
						{
							DialogHelper.createDialog(ActivityAdd.this, (edit ? "Edit " : "Add ") + (kanji.isEmpty() ? "Vocabulary" : kanji), "Please enter kanji, reading and meaning for the vocabulary! (You can leave out the reading if the kanji is written in kana only)");
							
							return;
						}
							
						final Vocabulary vocab = new Vocabulary(getIntent() == null ? -1 : getIntent().getIntExtra("id", -1));
						
						vocab.type = VocabularyType.values()[((Spinner) findViewById(R.id.spinner_type)).getSelectedItemPosition()];
						vocab.kanji = kanji;
						vocab.reading = reading;
						vocab.reading_trimed = reading_trimed;
						vocab.meaning = meaning_trimed;
						vocab.additionalInfo = additionalInfo;
						vocab.notes = notes;
						vocab.reading_used = new int[reading.length];
						vocab.meaning_used = new int[meaning_trimed.length];
						vocab.added = System.currentTimeMillis();
						vocab.soundFile = "";
						
						vocab.category_history = new int[32];
						vocab.category_history[vocab.category_history.length - 1] = vocab.category;
						for (int i = 0; i < vocab.category_history.length - 1; ++i)
							vocab.category_history[i] = -1;
							
						vocab.showInfo = ((CheckBox) findViewById(R.id.checkbox_show_info)).isChecked();
						vocab.imageFile = imageUrl;
						final ArrayList<Integer> sameReading = new ArrayList<>(), sameMeaning = new ArrayList<>();
						dbHelper.findSynonyms(dbHelper.getReadableDatabase(), vocab.kanji, getIntent() == null ? -1 : getIntent().getIntExtra("id", -1), vocab.reading_trimed, vocab.meaning, sameReading, sameMeaning);
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
									intent.putExtra("id", vocab.id);
									startActivity(intent);
								}
								sendBroadcast(new Intent(ActivityAdd.this, NotificationHelper.class));
								finish();
							}
						};
						
						dbHelper.updateVocabulary(vocab, edit ? ImportType.REPLACE_KEEP_STATS : ImportType.ASK, showVocabulary);
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
