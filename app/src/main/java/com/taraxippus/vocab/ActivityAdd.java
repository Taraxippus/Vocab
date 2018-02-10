package com.taraxippus.vocab;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.dialog.DialogHelper;
import com.taraxippus.vocab.dialog.DialogImport;
import com.taraxippus.vocab.util.JishoHelper;
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
import android.support.design.widget.TextInputEditText;
import android.view.KeyEvent;
import android.view.View.MeasureSpec;
import com.taraxippus.vocab.vocabulary.ReviewType;

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
		final TextView text_number = (TextView) findViewById(R.id.text_number);
		
		final Button button_add = (Button) findViewById(R.id.button_add);
		final CheckBox checkbox_show_info = (CheckBox) findViewById(R.id.checkbox_show_info);
		final View progress_jisho = findViewById(R.id.progress_jisho);
		
		text_kanji.setTextLocale(Locale.JAPANESE);
		text_reading.setTextLocale(Locale.JAPANESE);
		text_notes.setTextLocale(Locale.JAPANESE);
		
		boolean autoImport = false;
		final int vocabularyNumber = dbHelper.getCount("id = id") + 1;
		
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

				DialogImport dialog = new DialogImport();
				dialog.setArguments(bundle);
				dialog.show(getFragmentManager(), "import");
			}
			else
			{
				if (StringHelper.isKanaOrKanji(text))
				{
					text_kanji.setText(text);
					autoImport = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("autoImport", true);
				}
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
			
		text_kanji.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
				@Override
				public void onFocusChange(View p1, boolean focus)
				{
					if (!focus)
					{
						if (dbHelper.exists(dbHelper.getId(StringHelper.trim(text_kanji.getText().toString()))))
							text_number.setText("Alreasy exists");
						else
							text_number.setText("Nr. " + vocabularyNumber);
					}
				}
		});
	
		View button_info = findViewById(R.id.button_info);
		button_info.setVisibility(edit ? View.GONE : View.VISIBLE);
		text_number.setVisibility(edit ? View.GONE : View.VISIBLE);
		text_number.setText("Nr. " + vocabularyNumber);
		View.OnClickListener listener = new View.OnClickListener()
		{
				@Override
				public void onClick(View p1)
				{
					if (progress_jisho.getVisibility() != View.VISIBLE)
					{
						String kanji = StringHelper.trim(text_kanji.getText().toString());
						
						if (kanji.isEmpty())
							DialogHelper.createDialog(ActivityAdd.this, "Add Vocabulary", "Enter kanji to autofill the rest with information from jisho.org");
						
						else
						{
							progress_jisho.setVisibility(View.VISIBLE);
							
							final Vocabulary vocab = new Vocabulary(-1);
							vocab.kanji = kanji;
							JishoHelper.importVocabulary(ActivityAdd.this, vocab, new OnProcessSuccessListener()
							{
									@Override
									public void onProcessSuccess(Object... args)
									{
										if (isDestroyed())
											return;
											
										progress_jisho.setVisibility(View.INVISIBLE);
										
										if (args[0] == null)
										{
											Toast.makeText(ActivityAdd.this, "An error occured", 0).show();
											return;
										}
											
										text_kanji.setText(vocab.correctAnswer(QuestionType.KANJI));
										text_additional_info.setText(vocab.additionalInfo);

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
										for (int i = 0; i < vocab.meaning.length; ++i)
										{
											if (vocab.meaning.length <= 2 && vocab.meaning[i].contains("("))
											{
												int index0 = vocab.meaning[i].indexOf("(");
												int index1 = vocab.meaning[i].indexOf(")");
												if (index1 >= vocab.meaning[i].length() - 2)
												{
													meaning.append(vocab.meaning[i].substring(0, index0 - 1).replace(" ...", "")).append(vocab.meaning[i].substring(index1 + 1).replace(" ...", ""));
													if (text_additional_info.getText().length() == 0)
														text_additional_info.setText(vocab.meaning[i].substring(index0 + 1,index1));
													else
														text_additional_info.setText(vocab.meaning[i].substring(index0 + 1, index1) + "; " + text_additional_info.getText());
												}
												else
													meaning.append(vocab.meaning[i].substring(index1 + 2).replace(" ...", ""));
											}
											else
												meaning.append(vocab.meaning[i]);
												
											if (i != vocab.meaning.length - 1)
												meaning.append("; ");
										}

										if (vocab.meaning.length > 2)
										{
											final ArrayList<Integer> selectedItems = new ArrayList<>();
											AlertDialog.Builder builder = new AlertDialog.Builder(ActivityAdd.this);
											builder.setTitle("Choose Meanings")
												.setMultiChoiceItems(vocab.meaning, null,
												new DialogInterface.OnMultiChoiceClickListener()
												{
													@Override
													public void onClick(DialogInterface dialog, int which, boolean isChecked)
													{
														if (isChecked)
															selectedItems.add(which);

														else if (selectedItems.contains(which))
															selectedItems.remove(Integer.valueOf(which));

													}
												})
												.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() 
												{
													@Override
													public void onClick(DialogInterface dialog, int id) 
													{
														dialog.dismiss();
														StringBuilder sb = new StringBuilder();
														for (int i : selectedItems)
														{
															if (sb.length() != 0)
																sb.append("; ");

															if (vocab.meaning[i].contains("("))
															{
																int index0 = vocab.meaning[i].indexOf("(");
																int index1 = vocab.meaning[i].indexOf(")");
																if (index1 >= vocab.meaning[i].length() - 2)
																{
																	sb.append(vocab.meaning[i].substring(0, index0 - 1).replace(" ...", "")).append(vocab.meaning[i].substring(index1 + 1).replace(" ...", ""));
																	if (text_additional_info.getText().length() == 0)
																		text_additional_info.setText(vocab.meaning[i].substring(index0 + 1,index1));
																	else
																		text_additional_info.setText(vocab.meaning[i].substring(index0 + 1, index1) + "; " + text_additional_info.getText());
																}
																else
																	sb.append(vocab.meaning[i].substring(index1 + 2).replace(" ...", ""));
															}
															else
																sb.append(vocab.meaning[i]);
														}
														text_meaning.setText(sb.toString());
													}
												})
												.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
												{
													@Override
													public void onClick(DialogInterface dialog, int id)
													{
														dialog.cancel();
													}
												});
											builder.show();
										}
									
										text_meaning.setText(meaning.toString());
										text_notes.setText(vocab.notes);
										spinner_type.setSelection(vocab.type.ordinal());
									}
							});
						}
					}
				}
		};
		button_info.setOnClickListener(listener);
		if (autoImport)
			listener.onClick(button_info);
			
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
						DialogHelper.createDialog(ActivityAdd.this, (edit ? "Edit " : "Add ") + (kanji.isEmpty() ? "Vocabulary" : kanji), "Please enter kanji, reading in kana and meaning for the vocabulary! (You can leave out the reading if the kanji is written in kana only)");
						
					else
					{
						String[] reading = reading1.split(";|、");
						int i1 = 0;
						for (int i = 0; i < reading.length; ++i)
						{
							reading[i1] = StringHelper.trim(reading[i]);
							
							if (!reading[i1].isEmpty() || !StringHelper.isKana(reading[i1]))
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
							DialogHelper.createDialog(ActivityAdd.this, (edit ? "Edit " : "Add ") + (kanji.isEmpty() ? "Vocabulary" : kanji), "Please enter kanji, reading in kana and meaning for the vocabulary! (You can leave out the reading if the kanji is written in kana only)");
							
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
						if (reading_trimed.length == 0)
							vocab.kanjiReview = vocab.readingReview = vocab.meaningReview = ReviewType.MIXED;
						
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

	@Override
	public void onBackPressed() {}
}
