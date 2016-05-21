package com.taraxippus.vocab;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.fragment.AddFragment;
import com.taraxippus.vocab.fragment.DetailFragment;
import com.taraxippus.vocab.fragment.EditFragment;
import com.taraxippus.vocab.fragment.GrammarQuizFragment;
import com.taraxippus.vocab.fragment.HomeFragment;
import com.taraxippus.vocab.fragment.QuizFragment;
import com.taraxippus.vocab.util.AlarmReceiver;
import com.taraxippus.vocab.util.DialogHelper;
import com.taraxippus.vocab.util.JishoHelper;
import com.taraxippus.vocab.util.SaveHandler;
import com.taraxippus.vocab.util.ViewVocabularyDialog;
import com.taraxippus.vocab.vocabulary.Answer;
import com.taraxippus.vocab.vocabulary.ImportType;
import com.taraxippus.vocab.vocabulary.QuestionType;
import com.taraxippus.vocab.vocabulary.ShowType;
import com.taraxippus.vocab.vocabulary.SortType;
import com.taraxippus.vocab.vocabulary.ViewType;
import com.taraxippus.vocab.vocabulary.Vocabulary;
import com.taraxippus.vocab.vocabulary.VocabularyType;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import com.taraxippus.vocab.vocabulary.DBHelper;

public class MainActivity extends ActionBarActivity implements View.OnClickListener
{

	public static final String[] item_names = new String[] {"Home", "Add new vocabulary", "Quiz", "Grammar quiz", "Settings"};
	public static final int[] item_icons = new int[] {R.drawable.home, R.drawable.add, R.drawable.quiz, R.drawable.quiz_grammar, R.drawable.settings};
	
	public final SaveHandler saveHandler = new SaveHandler(this);
	public final DialogHelper dialogHelper = new DialogHelper(this);
	public final JishoHelper jishoHelper = new JishoHelper(this);
	
	public HomeFragment home;
	public AddFragment add;
	public QuizFragment quiz;
	public GrammarQuizFragment quiz_grammar;    

    RecyclerView recyclerView;                             
    DrawerLayout drawerLayout;                                        
	
	public ViewVocabularyDialog viewVocabularyDialog;
	
	public int vocabulary_selected;
	public DBHelper dbHelper;
	
	public final ArrayList<Vocabulary> vocabulary = new ArrayList<Vocabulary>();
	public final ArrayList<Vocabulary> vocabulary_filtered = new ArrayList<Vocabulary>();
	public final ArrayList<Vocabulary> vocabulary_learned = new ArrayList<Vocabulary>();
	public final ArrayList<Vocabulary> vocabulary_learned_new = new ArrayList<Vocabulary>();
	
	public static SortType sortType = SortType.CATEGORY;
	public ViewType viewType = ViewType.MEDIUM;
	public ShowType showType = ShowType.ALL;
	public final boolean[] show = new boolean[VocabularyType.values().length];
	public String queryText;
	public final Comparator<Vocabulary> searchComparator;

	public Fragment currentFragment;
	public Tap tap;
	public int selectedTap = 0;
	
	public MainActivity()
	{
		super();
		
		searchComparator = new Comparator<Vocabulary>()
		{
			@Override
			public int compare(Vocabulary v1, Vocabulary v2)
			{
				return (int) Math.signum(v2.searchResult - v1.searchResult);
			}
		};
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		updateFilter();
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
		setSupportActionBar(toolbar);
		
        recyclerView = (RecyclerView) findViewById(R.id.RecyclerView);                        
		
        NavigationAdapter adapter = new NavigationAdapter(this);     
        recyclerView.setAdapter(adapter);                            

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);               
        recyclerView.setLayoutManager(layoutManager);            

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);   

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name)
		{
            @Override
            public void onDrawerOpened(View drawerView) 
			{
                super.onDrawerOpened(drawerView);
				invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) 
			{
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
		}; 
        drawerLayout.setDrawerListener(drawerToggle); 
        drawerToggle.syncState();      
		
		dbHelper = new DBHelper(this);
		
		home = new HomeFragment();
		add = new AddFragment();
		quiz = new QuizFragment();
		quiz_grammar = new GrammarQuizFragment();
		
		home.setTransitions(this);
		add.setTransitions(this);
		quiz.setTransitions(this);
		quiz_grammar.setTransitions(this);
		
		if (getIntent() == null)
		{
			changeFragment(home, null);
			setTitle(item_names[0]);
		}
		else 
			onNewIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEND))
		{
			String s = intent.getDataString();
			
			if (s != null && !s.isEmpty())
			{
				boolean flag = true;
				
				String[] s1 = s.split("\n");
				
				for (String s2 : s1)
				{
					if (saveHandler.isVocabulary(s2))
					{
						saveHandler.importVocabulary(s2, ImportType.ASK, VocabularyType.NONE, false);
						flag = false;
					}
				}
				
				if (flag)
				{
					changeFragment(add, null);
					setTitle(item_names[1]);
				}
			}
			else
			{
				changeFragment(add, null);
				setTitle(item_names[1]);
			}
		}
		else if (intent.getBooleanExtra("review", false))
		{
			((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(R.string.notification_id);

			this.updateQuiz();
			
			Fragment fragment;
			
			if (!vocabulary_learned.isEmpty())
			{
				fragment = quiz;
			}
			else
			{
				fragment = home;
				
				dialogHelper.createDialog("Quiz", "Learn new vocabularies or come back later!");
			}
			
			changeFragment(fragment, null);
			setTap(quiz);
		}
		else
		{
			changeFragment(home, null);
			setTitle(item_names[0]);
		}
	}
	
	public final ArrayList<Integer> selectedVocabulary_backStack = new ArrayList<>();
	
	@Override
	public void onBackPressed()
	{
		if (drawerLayout.isDrawerOpen(recyclerView))
		{
			drawerLayout.closeDrawer(recyclerView);
		} 
		else if (getFragmentManager().getBackStackEntryCount() > 0) 
		{

			if (tap == Tap.DETAIL && selectedVocabulary_backStack.size() > 0)
			{
				this.vocabulary_selected = selectedVocabulary_backStack.remove(selectedVocabulary_backStack.size() - 1);
			}
			
			getFragmentManager().popBackStack();
		} 
		else 
		{
			super.onBackPressed();
		}
	}
	
	public void onVocabularyClicked(View v)
	{
		vocabulary_selected = vocabulary.indexOf(vocabulary_filtered.get(home.recyclerView.getChildAdapterPosition((LinearLayout) v.getParent()) - 1));
		
		Fragment fragment = getDetailFragment();
		
		View v1 = v.findViewById(R.id.kanji_text);
		View v2 = v.findViewById(R.id.reading_text);
		View v3 = v.findViewById(R.id.meaning_text);
		
		FragmentTransaction ft = getFragmentManager().beginTransaction()
			.replace(R.id.content_frame, fragment)
			.addToBackStack("detail")
			.addSharedElement(v, v.getTransitionName())
			.addSharedElement(v1, v1.getTransitionName())
			.addSharedElement(v2, v2.getTransitionName())
			.addSharedElement(v3, v3.getTransitionName())
			;
		ft.commit();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.item_search));
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener()
			{
				@Override
				public boolean onQueryTextSubmit(String p1)
				{
					return false;
				}

				@Override
				public boolean onQueryTextChange(String p1)
				{
					queryText = p1;
					updateFilter();
					
					return true;
				}
		});
		
		return true;
	}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) 
	{
		menu.setGroupVisible(R.id.group_detail, tap == Tap.DETAIL);
		menu.setGroupVisible(R.id.group_home, tap == Tap.HOME);
		menu.setGroupVisible(R.id.group_quiz, tap == Tap.QUIZ);
		
		menu.findItem(R.id.item_learn_add).setVisible(tap == Tap.DETAIL && !vocabulary.get(vocabulary_selected).learned);
		menu.findItem(R.id.item_learn_remove).setVisible(tap == Tap.DETAIL && vocabulary.get(vocabulary_selected).learned);
		
        return super.onPrepareOptionsMenu(menu);
    }

	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) 
	{
        int id = item.getItemId();

		if (id == R.id.item_sort)
		{
			showFilterMenu();
			
			return true;
		}
        else if (id == R.id.item_add) 
		{
			changeFragment(add, null);
            return true;
        }
        else if (id == R.id.item_import_file) 
		{
			Intent i = new Intent(Intent.ACTION_GET_CONTENT);
			i.setType("text/*");

			startActivityForResult(i, 0);
            return true;
        }
		else if (id == R.id.item_import_clipboard) 
		{
			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			
			if (clipboard.hasPrimaryClip() && clipboard.getText() != null)
			{
				final String[] text = clipboard.getText().toString().split("\n");
				
				AlertDialog alertDialog = new AlertDialog.Builder(this).create();
				alertDialog.setTitle("Import settings");
				View v = getLayoutInflater().inflate(R.layout.import_dialog, null);

				final Spinner spinner = (Spinner) v.findViewById(R.id.import_spinner);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Vocabulary.types_import);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(adapter);
				final Spinner spinner1 = (Spinner) v.findViewById(R.id.type_spinner);
				adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Vocabulary.types);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner1.setAdapter(adapter);
				final CheckBox learned = (CheckBox)v.findViewById(R.id.learned_checkbox);

				alertDialog.setView(v);		
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Import",
					new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							dialog.dismiss();
							
							ImportType importType = ImportType.values()[spinner.getSelectedItemPosition()];
							VocabularyType type = VocabularyType.values()[spinner1.getSelectedItemPosition()];
							
							int size = vocabulary.size();

							for (String line : text)
							{
								saveHandler.importVocabulary(line, importType, type, learned.isChecked());
							}

							updateFilter();
							saveHandler.save();
							updateNotification();

							dialogHelper.createDialog("Import", "Imported " + (vocabulary.size() - size) + ((vocabulary.size() - size) == 1 ? " new vocabulary!" : " new vocabularies!"));
						}
					});
				alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
					new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							dialog.dismiss();
						}
					});
				alertDialog.show();
			}
			else
				dialogHelper.createDialog("Import", "Clipboard is empty or doesn't contain text!");
				
			
			return true;
		}
		else if (id == R.id.item_export)
		{
			String filename = "vocabularies_" + new SimpleDateFormat("yyyy_MM_dd_HH:mm").format(new Date()) + ".txt";
			
			saveHandler.exportVocabulary(filename);

			dialogHelper.createDialog("Export", "Exported " + vocabulary.size() + (vocabulary.size() == 1 ? " vocabulary" : " vocabularies") + " to file \"" + filename + "\" in documents!");
		}
		else if (id == R.id.item_load) 
		{		
			AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
			alertDialog.setTitle("Load");
			alertDialog.setMessage("Do you really want to load vocabularies from a file? Your current vocabularies will be lost!");
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Load",
				new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						Intent i = new Intent(Intent.ACTION_GET_CONTENT);
						i.setType("*/*");

						startActivityForResult(i, 1);

						dialog.dismiss();
					}
				});
			alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
				new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						dialog.dismiss();
					}
				});
		
			alertDialog.show();
				
            return true;
        }
		else if (id == R.id.item_save) 
		{
			String filename = "vocabularies_" + new SimpleDateFormat("yyyy_MM_dd_HH:mm").format(new Date()) + ".vocab";
			
			saveHandler.save(filename);
			dialogHelper.createDialog("Save", "Saved " + vocabulary.size() + (vocabulary.size() == 1 ? " vocabulary" : " vocabularies") + " to file \"" + filename + "\" in documents!");
			
            return true;
        }
		else if (id == R.id.item_clear)
		{
			dialogHelper.createDialog("Clear", "Do you really want to delete every vocabulary? This cannot be undone!",
			"Delete filtered", 
				new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						vocabulary.removeAll(vocabulary_filtered);
						saveHandler.save();
						onVocabularyChanged();

						dialog.dismiss();
					}
				}, 
			"Delete", new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						vocabulary.clear();
						saveHandler.save();
						onVocabularyChanged();
						
						dialog.dismiss();
					}
				});
		}
		else if (id == R.id.item_clear_reset)
		{
			dialogHelper.createDialog("Reset", "Do you really want to reset every vocabulary? Your stats and progress will be lost. This cannot be undone!",
				"Reset filtered", 
				new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						for (Vocabulary v : vocabulary_filtered)
							v.reset();
					
						saveHandler.save();
						onVocabularyChanged();

						dialog.dismiss();
					}
				}, 
				"Reset", new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						for (Vocabulary v : vocabulary)
							v.reset();
						
						saveHandler.save();
						onVocabularyChanged();

						dialog.dismiss();
					}
				});
		}
		else if (id == R.id.item_learn_add_next)
		{
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("Learn next vocabularies");
			View v = getLayoutInflater().inflate(R.layout.learn_dialog, null);
			
			final Runnable updatePreview;
			final Spinner spinner = (Spinner) v.findViewById(R.id.learn_spinner);
			final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new String[] {"Oldest", "Filtered"});
			final EditText edit_count = (EditText) v.findViewById(R.id.edit_text_count);
			final Button plus = (Button) v.findViewById(R.id.button_plus);
			final Button minus = (Button) v.findViewById(R.id.button_minus);
			final TextView preview = (TextView) v.findViewById(R.id.text_preview);
			final ScrollView scroll_preview = (ScrollView) v.findViewById(R.id.scroll_preview);
			final View divider_scroll_top = v.findViewById(R.id.divider_scroll_top);
			final View divider_scroll_bottom = v.findViewById(R.id.divider_scroll_bottom);
			
			scroll_preview.setOnScrollChangeListener(
			new View.OnScrollChangeListener()
			{
					@Override
					public void onScrollChange(View p1, int p2, int p3, int p4, int p5)
					{
						if (scroll_preview.canScrollVertically(1)) 
							divider_scroll_bottom.setVisibility(View.VISIBLE);
						else
							divider_scroll_bottom.setVisibility(View.INVISIBLE);

						if (scroll_preview.canScrollVertically(-1)) 
							divider_scroll_top.setVisibility(View.VISIBLE);
						else
							divider_scroll_top.setVisibility(View.INVISIBLE);
						
					}
			});
			
			updatePreview = new Runnable()
			{
				@Override
				public void run()
				{
					StringBuilder sb = new StringBuilder();

					ArrayList<Vocabulary> vocabulary1 = spinner.getSelectedItemPosition() == 0 ? vocabulary : vocabulary_filtered;

					int count = 0;
					int count1 = Integer.parseInt(edit_count.getText().toString());
					for (int i = 0; i < vocabulary1.size() && count < count1; ++i)
					{
						if (!vocabulary1.get(i).learned)
						{
							if (count > 0)
								sb.append("\n");
							sb.append(vocabulary1.get(i).kanji);
							++count;
						}
					}

					preview.setText(sb.toString());
					
					if (scroll_preview.canScrollVertically(1)) 
						divider_scroll_bottom.setVisibility(View.VISIBLE);
					else
						divider_scroll_bottom.setVisibility(View.INVISIBLE);

					if (scroll_preview.canScrollVertically(-1)) 
						divider_scroll_top.setVisibility(View.VISIBLE);
					else
						divider_scroll_top.setVisibility(View.INVISIBLE);
				}
			};
			
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);
			spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener()
			{
					@Override
					public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4)
					{
						updatePreview.run();
					}

					@Override
					public void onNothingSelected(AdapterView<?> p1)
					{
						
					}
			});
			
			edit_count.setText("" + Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("learnAddCount", "10")));
			edit_count.setOnEditorActionListener(new EditText.OnEditorActionListener()
			{
					@Override
					public boolean onEditorAction(TextView p1, int p2, KeyEvent p3)
					{
						updatePreview.run();
						return true;
					}
			});
			
			plus.setOnClickListener(new View.OnClickListener()
			{
					@Override
					public void onClick(View p1)
					{
						edit_count.setText("" + (Integer.parseInt(edit_count.getText().toString()) + 1));
						
						updatePreview.run();
					}
			});
			minus.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View p1)
					{
						int count = Integer.parseInt(edit_count.getText().toString());
						
						if (count > 1)
							edit_count.setText("" + (count - 1));

						updatePreview.run();
					}
				});
			
			updatePreview.run();
			
			alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new AlertDialog.OnClickListener()
			{
					@Override
					public void onClick(DialogInterface p1, int p2)
					{
						p1.cancel();
					}
			});
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Learn", new AlertDialog.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface p1, int p2)
					{
						int count = 0;
						int count1 = Integer.parseInt(edit_count.getText().toString());
						
						ArrayList<Vocabulary> vocabulary1 = spinner.getSelectedItemPosition() == 0 ? vocabulary : vocabulary_filtered;
						for (int i = 0; i < vocabulary1.size() && count < count1; ++i)
						{
							if (!vocabulary1.get(i).learned)
							{
								vocabulary1.get(i).learned = true;
								++count;
							}
						}

						PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString("learnAddCount", "" + count1).commit();
						
						invalidateOptionsMenu();
						onVocabularyChanged();
					}
				});
			
			alertDialog.setView(v);
			alertDialog.show();
		}
		else if (id == R.id.item_learn_add_all)
		{
			for (Vocabulary v : vocabulary)
				v.learned = true;

			invalidateOptionsMenu();
			onVocabularyChanged();
		}
		else if (id == R.id.item_learn_remove_all)
		{
			for (Vocabulary v : vocabulary)
				v.learned = false;

			invalidateOptionsMenu();
			onVocabularyChanged();
		}
		else if (id == R.id.item_open_jisho) 
		{
			jishoHelper.search(vocabulary.get(vocabulary_selected).kanji);
			
            return true;
        }
		else if (id == R.id.item_open_jisho_kanji) 
		{
			jishoHelper.search(vocabulary.get(vocabulary_selected).kanji + "%23kanji");

            return true;
        }
        else if (id == R.id.item_edit) 
		{
			changeFragment(getEditFragment(), "edit");
		
            return true;
        }
		else if (id == R.id.item_reset) 
		{
			dialogHelper.createDialog("Reset", "Do you really want to reset this vocabulary? All progress and statistics will be lost!", "Reset", new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						vocabulary.get(vocabulary_selected).reset();

						updateFilter();
						refreshDetailView();

						dialog.dismiss();
					}
				});
			

            return true;
        }
		else if (id == R.id.item_reset_category) 
		{
			dialogHelper.createDialog("Reset", "Do you really want to reset the category of this vocabulary? You'll have to review it again.", "Reset", new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						vocabulary.get(vocabulary_selected).lastChecked = 0;
						vocabulary.get(vocabulary_selected).category = 1;;
						
						updateFilter();
						refreshDetailView();

						dialog.dismiss();
					}
				});


            return true;
        }
		else if (id == R.id.item_delete) 
		{
			dialogHelper.createDialog("Delete", "Do you really want to delete this vocabulary?", "Delete", new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						vocabulary.get(vocabulary_selected).remove(vocabulary);

						updateFilter();

						onBackPressed();

						dialog.dismiss();
					}
				});
			
            return true;
        }
		else if (id == R.id.item_show) 
		{
			changeFragment(getDetailFragment(), "detail");

            return true;
        }
		else if (id == R.id.item_learn_add)
		{
			vocabulary.get(vocabulary_selected).learned = true;
			invalidateOptionsMenu();
			
			onVocabularyChanged();
		}
		else if (id == R.id.item_learn_remove)
		{
			vocabulary.get(vocabulary_selected).learned = false;
			invalidateOptionsMenu();
			
			onVocabularyChanged();
		}
		else if (id == R.id.item_cheat)
		{
			Vocabulary v = vocabulary.get(vocabulary_selected);
			v.answer(v.kanji, QuestionType.KANJI, QuestionType.MEANING);
			if (v.reading.length > 0)
				v.answer(v.reading[0], QuestionType.READING, QuestionType.KANJI);
			v.answer(v.meaning[0], QuestionType.MEANING, QuestionType.KANJI);
			
			v.category++;
			v.lastChecked = System.currentTimeMillis();

			v.answered_kanji = false;
			v.answered_meaning = false;
			v.answered_reading = false;
			v.answered_correct = true;
			
			System.arraycopy(v.category_history, 1, v.category_history, 0, v.category_history.length - 1);
			v.category_history[v.category_history.length - 1] = v.category;
			
			onVocabularyChanged();
			refreshDetailView();
			saveHandler.save();
		}
		
        return super.onOptionsItemSelected(item);
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) 
	{
		if (resultCode == RESULT_OK)
		{
			if (requestCode == 0)
			{
				AlertDialog alertDialog = new AlertDialog.Builder(this).create();
				alertDialog.setTitle("Import settings");
				View v = getLayoutInflater().inflate(R.layout.import_dialog, null);

				final Spinner spinner = (Spinner) v.findViewById(R.id.import_spinner);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Vocabulary.types_import);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(adapter);
				final Spinner spinner1 = (Spinner) v.findViewById(R.id.type_spinner);
				adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Vocabulary.types);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner1.setAdapter(adapter);
				final CheckBox learned = (CheckBox)v.findViewById(R.id.learned_checkbox);

				alertDialog.setView(v);		
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Import",
					new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							dialog.dismiss();

							ImportType importType = ImportType.values()[spinner.getSelectedItemPosition()];
							VocabularyType type = VocabularyType.values()[spinner1.getSelectedItemPosition()];
							
							try
							{
								BufferedReader reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(data.getData())));

								int size = vocabulary.size();

								String line;
								while ((line = reader.readLine()) != null)
								{
									saveHandler.importVocabulary(line, importType, type, learned.isChecked());
								}

								onVocabularyChanged();
								saveHandler.save();

								dialogHelper.createDialog("Import", "Imported " + (vocabulary.size() - size) + ((vocabulary.size() - size) == 1 ? " new vocabulary!" : " new vocabularies!"));
							
								reader.close();
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					});
				alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
					new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							dialog.cancel();
						}
					});
				alertDialog.show();

				return;
			}
			else if (requestCode == 1)
			{
				try
				{
					saveHandler.load(new FileInputStream(getContentResolver().openFileDescriptor(data.getData(), "r").getFileDescriptor()));
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
				}

				saveHandler.save();
				
				return;
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	@Override
	public void onClick(final View view)
	{
		int item = recyclerView.getChildPosition(view) - 1;
	
		Fragment fragment;
		
		if (item == 0)
			fragment = home;
			
		else if (item == 1)
			fragment = add;
			
		else if (item == 2)
		{
			this.updateQuiz();
			
			if (!vocabulary_learned.isEmpty())
			{
				fragment = quiz;
			}
			else
			{
				fragment = home;

				dialogHelper.createDialog("Quiz", "Learn new vocabularies or come back later!");
			}
		}
		else if (item == 3)
			fragment = quiz_grammar;
			
		else if (item == 5)
		{
			this.startActivityForResult(new Intent().setClass(this, SettingsActivity.class), 0);
			drawerLayout.closeDrawer(recyclerView);
			return;
		}
			
		else
			return;
	
		changeFragment(fragment, "tab");
		
		drawerLayout.closeDrawer(recyclerView);
	}

	public void showVocabularyPopup(final View v)
	{
		vocabulary_selected = vocabulary.indexOf(vocabulary_filtered.get(home.recyclerView.getChildAdapterPosition((View)((View)((View)v.getParent()).getParent()).getParent()) - 1));
		
		PopupMenu popup = new PopupMenu(this, v)
		{
			@Override
			public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item)
			{
				switch (item.getItemId()) 
				{
					case R.id.delete:
						AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
						alertDialog.setTitle("Delete");
						alertDialog.setMessage("Do you really want to delete this vocabulary?");
						alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete",
							new DialogInterface.OnClickListener() 
							{
								public void onClick(DialogInterface dialog, int which) 
								{
									vocabulary.get(vocabulary_selected).remove(vocabulary);
									onVocabularyChanged();
									
									dialog.dismiss();
								}
							});
						alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
							new DialogInterface.OnClickListener() 
							{
								public void onClick(DialogInterface dialog, int which) 
								{
									dialog.cancel();
								}
							});
						alertDialog.show();
						
						return true;

					case R.id.detail:
						onVocabularyClicked((View)((View) v.getParent()).getParent());
						return true;
						
					case R.id.edit:
						changeFragment(getEditFragment(), "edit");
						
						return true;
						
					case R.id.learn_add:
						vocabulary.get(vocabulary_selected).learned = true;
						onVocabularyChanged();
						
						return true;
						
					case R.id.learn_remove:
						vocabulary.get(vocabulary_selected).learned = false;
						onVocabularyChanged();
						
						return true;
						
					default:
						return super.onMenuItemSelected(menu, item);
				}
			}
		};
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.vocabulary, popup.getMenu());
		popup.getMenu().findItem(R.id.learn_add).setVisible(!vocabulary.get(vocabulary_selected).learned);
		popup.getMenu().findItem(R.id.learn_remove).setVisible(vocabulary.get(vocabulary_selected).learned);
		popup.show();
	}
	
	public void showStrokeOrderPopup(final View v)
	{
		PopupMenu popup = new PopupMenu(this, v)
		{
			@Override
			public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item)
			{
				switch (item.getItemId()) 
				{
					case R.id.open_jisho_kanji:
						jishoHelper.search(vocabulary.get(vocabulary_selected).kanji + "%23kanji");
						return true;
						
					case R.id.settings:
						startActivityForResult(new Intent().setClass(MainActivity.this, SettingsActivity.class), 0);
						
						return true;
						
					default:
						return super.onMenuItemSelected(menu, item);
				}
			}
		};
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.stroke_order, popup.getMenu());
		popup.show();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		
		saveHandler.save();
		dbHelper.close();
	}
	

	@Override
	protected void onResume()
	{
		super.onResume();
		
		saveHandler.load();
	}
	
	public void onVocabularyChanged()
	{
		updateFilter();
		updateNotification();
	}
	
	public void refreshDetailView()
	{
		getFragmentManager().popBackStack();
		
		Fragment fragment = getDetailFragment();
		fragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_bottom));
		changeFragment(fragment, "");
	}
	
	public void updateNotification()
	{
		if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("notification", true))
			return;
			
		long nextReview = 0;
		for (Vocabulary v : vocabulary)
		{
			if (v.learned)
			{
				if (nextReview == 0)
					nextReview = v.lastChecked + v.getNextReview();
				else
					nextReview = Math.min(nextReview, v.lastChecked + v.getNextReview());
				
			}
		}

		AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, AlarmReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);
		
		if (nextReview < System.currentTimeMillis())
		{
			try
			{
				alarmManager.cancel(pendingIntent);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return;
		}
		nextReview += 1000 * 60 * 5;
		
		alarmManager.set(AlarmManager.RTC_WAKEUP, nextReview, pendingIntent);
	}
	
	public Fragment getDetailFragment()
	{
		DetailFragment detail = new DetailFragment();
		detail.setTransitions(this);
		
		return detail;
	}
	

	public Fragment getEditFragment()
	{
		EditFragment edit = new EditFragment();
		edit.setTransitions(this);

		return edit;
	}
	
	public void changeFragment(Fragment fragment, String backStack)
	{
		FragmentManager fragmentManager = getFragmentManager();
		
		if (backStack == null)
		{
			fragmentManager.beginTransaction()
				.replace(R.id.content_frame, fragment)
				.commit();
		}
		else
		{
			fragmentManager.beginTransaction()
				.replace(R.id.content_frame, fragment)
				.addToBackStack(backStack)
				.commit();
		}
	}
	
	public void setTap(Fragment fragment)
	{
		invalidateOptionsMenu();
		
		this.currentFragment = fragment;
		
		if (fragment == home)
		{
			if (tap == Tap.QUIZ)
				updateFilter();
			
			selectedTap = 0;
			recyclerView.getAdapter().notifyDataSetChanged();
			setTitle(item_names[0]);
			tap = Tap.HOME;
		
		}
		else if (fragment == quiz_grammar)
		{
			selectedTap = 3;
			recyclerView.getAdapter().notifyDataSetChanged();
			setTitle(item_names[3]);
			tap = Tap.QUIZ_GRAMMAR;
		}
		else if (fragment == add)
		{
			selectedTap = 1;
			recyclerView.getAdapter().notifyDataSetChanged();
			setTitle(item_names[1]);
			tap = Tap.ADD;
		}
		else if (fragment instanceof EditFragment)
		{
			setTitle("Edit " + vocabulary.get(vocabulary_selected).kanji);
			tap = Tap.EDIT;
		}
		else if (fragment == quiz)
		{
			selectedVocabulary_backStack.clear();
			
			selectedTap = 2;
			recyclerView.getAdapter().notifyDataSetChanged();
			setTitle(item_names[2] + ", " + vocabulary_learned.size() + (vocabulary_learned.size() == 1 ? " vocabulary" : " vocabularies"));
			tap = Tap.QUIZ;
		}
		else if (fragment instanceof DetailFragment)
		{
			setTitle(vocabulary.get(vocabulary_selected).correctAnswer(QuestionType.KANJI));
			tap = Tap.DETAIL;
		}
	}
	
	public void showFilterMenu()
	{
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Filter");
		LinearLayout v = (LinearLayout)getLayoutInflater().inflate(R.layout.view_dialog, null);

		final Spinner spinner = (Spinner) v.findViewById(R.id.sort_spinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Vocabulary.types_sort);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(sortType.ordinal());

		final Spinner spinner1 = (Spinner) v.findViewById(R.id.view_spinner);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Vocabulary.types_view);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner1.setAdapter(adapter);
		spinner1.setSelection(viewType.ordinal());

		final Spinner spinner2 = (Spinner) v.findViewById(R.id.show_spinner);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Vocabulary.types_show);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner2.setAdapter(adapter);
		spinner2.setSelection(showType.ordinal());

		final LinearLayout v1 = (LinearLayout)v.findViewById(R.id.show_boxes);
		
		final ArrayList<CheckBox> boxes = new ArrayList<>();
		for (int i = 0; i < Vocabulary.types.size(); ++i)
		{
			CheckBox box = new CheckBox(this);
			box.setChecked(show[i]);
			box.setText("Show " + Vocabulary.types.get(i));
			v1.addView(box);

			boxes.add(box);
		}

		final ScrollView scroll_preview = (ScrollView) v.findViewById(R.id.scroll_show_boxes);
		final View divider_scroll_top = v.findViewById(R.id.divider_scroll_top);
		final View divider_scroll_bottom = v.findViewById(R.id.divider_scroll_bottom);

		scroll_preview.setOnScrollChangeListener(
			new View.OnScrollChangeListener()
			{
				@Override
				public void onScrollChange(View p1, int p2, int p3, int p4, int p5)
				{
					if (scroll_preview.canScrollVertically(1)) 
						divider_scroll_bottom.setVisibility(View.VISIBLE);
					else
						divider_scroll_bottom.setVisibility(View.INVISIBLE);

					if (scroll_preview.canScrollVertically(-1)) 
						divider_scroll_top.setVisibility(View.VISIBLE);
					else
						divider_scroll_top.setVisibility(View.INVISIBLE);

				}
			});
		
		
		divider_scroll_bottom.setVisibility(View.VISIBLE);
		divider_scroll_top.setVisibility(View.INVISIBLE);
		
	
		alertDialog.setView(v);		
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.dismiss();

					sortType = SortType.values()[spinner.getSelectedItemPosition()];
					viewType = ViewType.values()[spinner1.getSelectedItemPosition()];
					showType = ShowType.values()[spinner2.getSelectedItemPosition()];

					for (int i = 0; i < show.length; ++i)
						show[i] = boxes.get(i).isChecked();

					updateFilter();
				}
			});
		alertDialog.show();
	}
	
	public void updateFilter()
	{
		String queryText = null;
		
		if (this.queryText != null)
			queryText = this.queryText.toLowerCase();
		
		vocabulary_filtered.clear();
		for (Vocabulary v : vocabulary)
		{
			if (show[v.type.ordinal()]
			&& (showType == ShowType.ALL || showType == ShowType.LEARNED && v.learned || showType == ShowType.UNLEARNED && !v.learned)
			&& (queryText == null || queryText.isEmpty() || v.searched(queryText) > 0))
				vocabulary_filtered.add(v);
		}
		
		if (queryText == null || queryText.isEmpty())
			Collections.sort(vocabulary_filtered);
		else
			Collections.sort(vocabulary_filtered, searchComparator);
		
		if (home != null && home.recyclerView != null)
		{
			home.recyclerView.getAdapter().notifyDataSetChanged();
		}
	}
	
	public void updateQuiz()
	{
		vocabulary_learned.clear();
		vocabulary_learned_new.clear();

		for (Vocabulary v : vocabulary)
		{
			if (v.learned && v.lastChecked + v.getNextReview() < System.currentTimeMillis())
			{
				vocabulary_learned.add(v);

				if (v.lastChecked == 0)
				{
					vocabulary_learned_new.add(v);
				}
			}	
		}

		if (vocabulary_learned_new.size() > 0)
		{
			this.askViewVocabularies();
		}
	}
	
	public void askViewVocabularies()
	{
		if (viewVocabularyDialog != null)
		{
			viewVocabularyDialog.alertDialog.cancel();
		}
		
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("New vocabularies");
		alertDialog.setMessage("There are new vocabularies in this quiz. Do you want to view them now?");
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.dismiss();
					
					viewVocabularyDialog = new ViewVocabularyDialog(MainActivity.this);
					
					for (Vocabulary v : vocabulary_learned_new)
					{
						v.category = 0;
					}			
				}
			});
		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Exclude",
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.dismiss();
					
					vocabulary_learned.removeAll(vocabulary_learned_new);
					vocabulary_learned_new.clear();
					
					if (!vocabulary_learned.contains(vocabulary.get(vocabulary_selected)))
					{
						quiz.answer = Answer.SKIP;
						quiz.next();
					}
				
					setTap(quiz);
				}
			});
		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.cancel();
					
					vocabulary_learned_new.clear();
				}
			});
		alertDialog.show();
	}

	
	public enum Tap
	{
		HOME,
		ADD,
		QUIZ,
		QUIZ_GRAMMAR,
		DETAIL,
		EDIT,
	}
}
