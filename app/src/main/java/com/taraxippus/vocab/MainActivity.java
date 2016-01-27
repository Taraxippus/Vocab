package com.taraxippus.vocab;
import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.support.v4.widget.*;
import android.support.v7.app.*;
import android.support.v7.view.menu.*;
import android.support.v7.widget.*;
import android.transition.*;
import android.view.*;
import android.widget.*;
import com.taraxippus.vocab.fragment.*;
import com.taraxippus.vocab.notification.*;
import com.taraxippus.vocab.save.*;
import com.taraxippus.vocab.util.*;
import com.taraxippus.vocab.vocabulary.*;
import java.io.*;
import java.text.*;
import java.util.*;

import android.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;

public class MainActivity extends ActionBarActivity implements View.OnClickListener
{

	public static final String[] item_names = new String[] {"Home", "Add new Vocabulary", "Quiz", "Grammar quiz"};
	public static final int[] item_icons = new int[] {R.drawable.home, R.drawable.add, R.drawable.quiz, R.drawable.quiz_grammar};
	
	public SaveHandler saveHandler = new SaveHandler(this);
	public DialogHelper dialogHelper = new DialogHelper(this);
	
	public HomeFragment home;
	public Fragment add;
	public QuizFragment quiz;
	public GrammarQuizFragment quiz_grammar;
	
	private Toolbar toolbar;                       
	
    RecyclerView recyclerView;                  
    RecyclerView.Adapter adapter;                    
    RecyclerView.LayoutManager layoutManager;            
    DrawerLayout drawerLayout;                             

    ActionBarDrawerToggle mDrawerToggle;             
	
	public ViewVocabularyDialog viewVocabularyDialog;
	
	public int vocabulary_selected;
	
	public final ArrayList<Vocabulary> vocabulary = new ArrayList<Vocabulary>();
	public final ArrayList<Vocabulary> vocabulary_filtered = new ArrayList<Vocabulary>();
	public final ArrayList<Vocabulary> vocabulary_learned = new ArrayList<Vocabulary>();
	public final ArrayList<Vocabulary> vocabulary_learned_new = new ArrayList<Vocabulary>();
	
	public static Vocabulary.SortType sortType = Vocabulary.SortType.CATEGORY;
	public Vocabulary.ViewType viewType = Vocabulary.ViewType.LARGE;
	public Vocabulary.ShowType showType = Vocabulary.ShowType.ALL;
	public final boolean[] show = new boolean[Vocabulary.Type.values().length];
	public String queryText;

	public Tap tap;
	public int selectedTap = 0;
	
	public MainActivity()
	{
		super();
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		updateFilter();
		
		toolbar = (Toolbar) findViewById(R.id.tool_bar);
		setSupportActionBar(toolbar);
		
        recyclerView = (RecyclerView) findViewById(R.id.RecyclerView);                        
		recyclerView.addItemDecoration(new DividerItemDecoration(this));
		
        adapter = new NavigationAdapter(this);     
        recyclerView.setAdapter(adapter);                            

        layoutManager = new LinearLayoutManager(this);               
        recyclerView.setLayoutManager(layoutManager);            

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);   

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name)
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
        drawerLayout.setDrawerListener(mDrawerToggle); 
        mDrawerToggle.syncState();              
		
		home = new HomeFragment();
		add = new AddFragment();
		quiz = new QuizFragment();
		quiz_grammar = new GrammarQuizFragment();
		
		quiz.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_top));
		quiz.setReturnTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
		
		quiz.setAllowEnterTransitionOverlap(false);
		quiz.setAllowReturnTransitionOverlap(false);
		
		quiz_grammar.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_left));
		quiz_grammar.setReturnTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
		
		quiz_grammar.setAllowEnterTransitionOverlap(false);
		quiz_grammar.setAllowReturnTransitionOverlap(false);
		
		add.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_left));
		add.setReturnTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
		
		add.setAllowEnterTransitionOverlap(false);
		add.setAllowReturnTransitionOverlap(false);
		
		home.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_left));
		home.setReturnTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
		
		home.setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.change_image_transform));
		home.setSharedElementReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.change_image_transform));
	
		home.setAllowEnterTransitionOverlap(false);
		home.setAllowReturnTransitionOverlap(false);
		
		if (getIntent() != null && getIntent().getAction() == Intent.ACTION_SEND)
		{
			changeFragment(add, null);
			setTitle(item_names[1]);
		}
		else
		{
			changeFragment(home, null);
			setTitle(item_names[0]);
		}
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		
		if (intent.getAction() == Intent.ACTION_SEND)
		{
			changeFragment(add, null);
			setTitle(item_names[1]);
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
		vocabulary_selected = vocabulary.indexOf(vocabulary_filtered.get(home.recyclerView.getChildAdapterPosition((LinearLayout)v.getParent()) - 1));
		
		VocabularyFragment fragment = new VocabularyFragment();
		fragment.setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.change_image_transform));
		fragment.setSharedElementReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.change_image_transform));
		
		fragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_top));
		fragment.setReturnTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));

		fragment.setAllowEnterTransitionOverlap(false);
		fragment.setAllowReturnTransitionOverlap(false);
		
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
		android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) menu.findItem(R.id.item_search).getActionView();
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
		menu.findItem(R.id.item_search).setVisible(tap == Tap.HOME);
		menu.findItem(R.id.item_sort).setVisible(tap == Tap.HOME);
		menu.findItem(R.id.item_add).setVisible(tap == Tap.HOME);
        menu.findItem(R.id.item_import_file).setVisible(tap == Tap.HOME);
		menu.findItem(R.id.item_import_clipboard).setVisible(tap == Tap.HOME);
		menu.findItem(R.id.item_export).setVisible(tap == Tap.HOME);
		menu.findItem(R.id.item_load).setVisible(tap == Tap.HOME);
		menu.findItem(R.id.item_save).setVisible(tap == Tap.HOME);
		menu.findItem(R.id.item_clear).setVisible(tap == Tap.HOME);
		menu.findItem(R.id.item_learn_add_next).setVisible(tap == Tap.HOME);
		menu.findItem(R.id.item_learn_add_all).setVisible(tap == Tap.HOME);
		menu.findItem(R.id.item_learn_remove_all).setVisible(tap == Tap.HOME);
		
		menu.findItem(R.id.item_open_jisho).setVisible(tap == Tap.DETAIL);
		menu.findItem(R.id.item_open_jisho_kanji).setVisible(tap == Tap.DETAIL);
		menu.findItem(R.id.item_edit).setVisible(tap == Tap.DETAIL);
		menu.findItem(R.id.item_learn_add).setVisible(tap == Tap.DETAIL && !vocabulary.get(vocabulary_selected).learned);
		menu.findItem(R.id.item_learn_remove).setVisible(tap == Tap.DETAIL && vocabulary.get(vocabulary_selected).learned);
		menu.findItem(R.id.item_reset).setVisible(tap == Tap.DETAIL);
		menu.findItem(R.id.item_cheat).setVisible(tap == Tap.DETAIL);
		menu.findItem(R.id.item_delete).setVisible(tap == Tap.DETAIL);
		
		menu.findItem(R.id.item_show).setVisible(tap == Tap.QUIZ);
		
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
							
							Vocabulary.ImportType importType = Vocabulary.ImportType.values()[spinner.getSelectedItemPosition()];
							Vocabulary.Type type = Vocabulary.Type.values()[spinner1.getSelectedItemPosition()];
							
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
			
			File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
			file.mkdirs();
			
			try
			{
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath() + "/" + filename)));
			
				int i;
				for (Vocabulary v : vocabulary)
				{
					writer.write(v.kanji + "【" + v.reading + "】- ");
					for (i = 0; i < v.meaning.length - 1; ++i)
					{
						writer.write(v.meaning[i] + "; ");
					}
					writer.write(v.meaning[v.meaning.length -1]);
					if (!v.additionalInfo.isEmpty())
						writer.write(" (" + v.additionalInfo + ")");
					writer.write("\n");
				}
				
				writer.close();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}

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
			AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
			alertDialog.setTitle("Clear");
			alertDialog.setMessage("Do you really want to delete every vocabulary? This cannot be undone!");
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete",
				new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						vocabulary.clear();
						saveHandler.save();
						home.recyclerView.getAdapter().notifyDataSetChanged();
						updateNotification();
						
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
		}
		else if (id == R.id.item_learn_add_next)
		{
			int count = 0;
			for (int i = 0; i < vocabulary.size() && count < 20; ++i)
			{
				if (!vocabulary.get(i).learned)
				{
					vocabulary.get(i).learned = true;
					++count;
				}
			}
				
			invalidateOptionsMenu();
			updateFilter();
			updateNotification();
		}
		else if (id == R.id.item_learn_add_all)
		{
			for (Vocabulary v : vocabulary)
				v.learned = true;

			invalidateOptionsMenu();
			updateFilter();
			updateNotification();
		}
		else if (id == R.id.item_learn_remove_all)
		{
			for (Vocabulary v : vocabulary)
				v.learned = false;

			invalidateOptionsMenu();
			updateFilter();
			updateNotification();
		}
		else if (id == R.id.item_open_jisho) 
		{
			searchJisho(vocabulary.get(vocabulary_selected).kanji);
			
            return true;
        }
		else if (id == R.id.item_open_jisho_kanji) 
		{
			searchJisho(vocabulary.get(vocabulary_selected).kanji + "%23kanji");

            return true;
        }
        else if (id == R.id.item_edit) 
		{
			Fragment fragment = new AddFragment(true);
			
			fragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_left));
			fragment.setReturnTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));

			fragment.setAllowEnterTransitionOverlap(false);
			fragment.setAllowReturnTransitionOverlap(false);
			
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
				.replace(R.id.content_frame, fragment)
				.addToBackStack("edit")
				.commit();
			
            return true;
        }
		else if (id == R.id.item_reset) 
		{
			AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
			alertDialog.setTitle("Reset");
			alertDialog.setMessage("Do you really want to reset this vocabulary? All progress and statistics will be lost!");
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Reset",
				new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						vocabulary.get(vocabulary_selected).reset();

						home.recyclerView.getAdapter().notifyItemRemoved(vocabulary_selected);
						home.recyclerView.getAdapter().notifyItemChanged(0);

						Fragment fragment = new VocabularyFragment();
						fragment.setSharedElementEnterTransition(TransitionInflater.from(MainActivity.this).inflateTransition(R.transition.change_image_transform));
						fragment.setSharedElementReturnTransition(TransitionInflater.from(MainActivity.this).inflateTransition(R.transition.change_image_transform));

						fragment.setEnterTransition(TransitionInflater.from(MainActivity.this).inflateTransition(android.R.transition.slide_right));
						fragment.setReturnTransition(TransitionInflater.from(MainActivity.this).inflateTransition(android.R.transition.fade));

						FragmentManager fragmentManager = getFragmentManager();
						fragmentManager.beginTransaction()
							.replace(R.id.content_frame, fragment)
							.addToBackStack("detail_refresh")
							.commit();

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
		else if (id == R.id.item_delete) 
		{
			AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
			alertDialog.setTitle("Delete");
			alertDialog.setMessage("Do you really want to delete this vocabulary?");
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete",
				new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						vocabulary.get(vocabulary_selected).remove(vocabulary);
					
						home.recyclerView.getAdapter().notifyItemRemoved(vocabulary_selected);
						home.recyclerView.getAdapter().notifyItemChanged(0);
						
						getFragmentManager().popBackStack();
						
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
		else if (id == R.id.item_show) 
		{
			Fragment fragment = new VocabularyFragment();
			fragment.setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.change_image_transform));
			fragment.setSharedElementReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.change_image_transform));

			fragment.setAllowEnterTransitionOverlap(false);
			fragment.setAllowReturnTransitionOverlap(false);
			
			fragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_top));
			fragment.setReturnTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
			
			changeFragment(fragment, "detail");

            return true;
        }
		else if (id == R.id.item_learn_add)
		{
			vocabulary.get(vocabulary_selected).learned = true;
	
			invalidateOptionsMenu();
			updateFilter();
			updateNotification();
		}
		else if (id == R.id.item_learn_remove)
		{
			vocabulary.get(vocabulary_selected).learned = false;
		
			invalidateOptionsMenu();
			updateFilter();
			updateNotification();
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
			
			updateFilter();
			updateNotification();
			saveHandler.save();
			
			Fragment fragment = new VocabularyFragment();
			fragment.setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.change_image_transform));
			fragment.setSharedElementReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.change_image_transform));

			fragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.slide_right));
			fragment.setReturnTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));

			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
				.replace(R.id.content_frame, fragment)
				.addToBackStack("detail_refresh")
				.commit();
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

							Vocabulary.ImportType importType = Vocabulary.ImportType.values()[spinner.getSelectedItemPosition()];
							Vocabulary.Type type = Vocabulary.Type.values()[spinner1.getSelectedItemPosition()];
							
							try
							{
								BufferedReader reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(data.getData())));

								int size = vocabulary.size();

								String line;
								while ((line = reader.readLine()) != null)
								{
									saveHandler.importVocabulary(line, importType, type, learned.isChecked());
								}

								updateFilter();
								saveHandler.save();
								updateNotification();

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
							dialog.dismiss();
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
		else
		{
			fragment = quiz_grammar;
		}
	
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction()
			.replace(R.id.content_frame, fragment)
			.addToBackStack("tab")
			.commit();

		drawerLayout.closeDrawer(recyclerView);
	}

	public void showPopup(final View v)
	{
		vocabulary_selected = vocabulary.indexOf(vocabulary_filtered.get(home.recyclerView.getChildAdapterPosition((LinearLayout)((CardView)v.getParent()).getParent()) - 1));
		
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
									home.recyclerView.getAdapter().notifyItemRemoved(vocabulary_selected + 1);
									home.recyclerView.getAdapter().notifyItemChanged(0);
									
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

					case R.id.detail:
						onVocabularyClicked((View)v.getParent());
						return true;
						
					case R.id.edit:
						Fragment fragment = new AddFragment(true);

						fragment.setEnterTransition(TransitionInflater.from(MainActivity.this).inflateTransition(android.R.transition.slide_left));
						fragment.setReturnTransition(TransitionInflater.from(MainActivity.this).inflateTransition(android.R.transition.fade));
						
						FragmentManager fragmentManager = getFragmentManager();
						fragmentManager.beginTransaction()
							.replace(R.id.content_frame, fragment)
							.addToBackStack("edit")
							.commit();
						
						return true;
						
					case R.id.learn_add:
						vocabulary.get(vocabulary_selected).learned = true;
			
						updateFilter();
						updateNotification();
						return true;
						
					case R.id.learn_remove:
						vocabulary.get(vocabulary_selected).learned = false;
						
						updateFilter();
						updateNotification();
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
	
	@Override
	protected void onPause()
	{
		super.onPause();
		
		saveHandler.save();
	}
	

	@Override
	protected void onResume()
	{
		super.onResume();
		
		saveHandler.load();
	}
	
	public void updateNotification()
	{
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
		else if (fragment instanceof AddFragment)
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
		else if (fragment instanceof VocabularyFragment)
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

		
		alertDialog.setView(v);		
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.dismiss();

					sortType = Vocabulary.SortType.values()[spinner.getSelectedItemPosition()];
					viewType = Vocabulary.ViewType.values()[spinner1.getSelectedItemPosition()];
					showType = Vocabulary.ShowType.values()[spinner2.getSelectedItemPosition()];

					for (int i = 0; i < show.length; ++i)
						show[i] = boxes.get(i).isChecked();

					updateFilter();
				}
			});
		alertDialog.show();
	}
	
	public void updateFilter()
	{
		vocabulary_filtered.clear();
		for (Vocabulary v : vocabulary)
		{
			if (show[v.type.ordinal()]
			&& (showType == Vocabulary.ShowType.ALL || showType == Vocabulary.ShowType.LEARNED && v.learned || showType == Vocabulary.ShowType.UNLEARNED && !v.learned)
			&& (queryText == null || queryText.isEmpty() || v.searched(queryText)))
				vocabulary_filtered.add(v);
		}
		
		Collections.sort(vocabulary_filtered);
		
		if (home != null)
		{
			home.recyclerView.getAdapter().notifyDataSetChanged();
		}
	}
	
	public void searchJisho(String query)
	{
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse("http://jisho.org/search/" + query));
		startActivity(i);
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
						quiz.answer = Answer.CORRECT;
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
		EDIT
	}
}
