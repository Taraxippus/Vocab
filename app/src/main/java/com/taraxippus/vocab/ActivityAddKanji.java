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
		final TextView text_notes = (TextView) findViewById(R.id.text_notes);
		final Button button_add = (Button) findViewById(R.id.button_add);
		
		text_kanji.setTextLocale(Locale.JAPANESE);
		text_notes.setTextLocale(Locale.JAPANESE);

		if (getIntent() != null && getIntent().hasExtra("kanji"))
		{
			text_kanji.setText("" + getIntent().getCharExtra("kanji", '漢'));
		}
		
		if (getIntent() != null && getIntent().hasExtra("id"))
		{
			edit = true;
			button_add.setText("Edit");
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
		button_add.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View p1)
				{
					
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
