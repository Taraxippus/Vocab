package com.taraxippus.vocab.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.format.DateFormat;
import android.transition.TransitionInflater;
import android.transition.TransitionSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.taraxippus.vocab.ActivityAdd;
import com.taraxippus.vocab.ActivityQuiz;
import com.taraxippus.vocab.ActivitySettings;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.dialog.DialogHelper;
import com.taraxippus.vocab.dialog.FilterDialog;
import com.taraxippus.vocab.dialog.ImportDialog;
import com.taraxippus.vocab.dialog.LearnNextDialog;
import com.taraxippus.vocab.util.JishoHelper;
import com.taraxippus.vocab.util.NotificationHelper;
import com.taraxippus.vocab.util.StringHelper;
import com.taraxippus.vocab.view.GraphView;
import com.taraxippus.vocab.view.LineGraphView;
import com.taraxippus.vocab.view.PercentageGraphView;
import com.taraxippus.vocab.vocabulary.DBHelper;
import com.taraxippus.vocab.vocabulary.HideType;
import com.taraxippus.vocab.vocabulary.QuestionType;
import com.taraxippus.vocab.vocabulary.ShowType;
import com.taraxippus.vocab.vocabulary.SortType;
import com.taraxippus.vocab.vocabulary.ViewType;
import com.taraxippus.vocab.vocabulary.Vocabulary;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

public class FragmentHome extends Fragment implements SearchView.OnQueryTextListener, SharedPreferences.OnSharedPreferenceChangeListener
{
	public RecyclerView recyclerView;
	public SharedPreferences preferences;	
	public DBHelper dbHelper;
	
	public int[] vocabularies;
	public ViewType viewType;
	public String searchQuery;
	public SortType sortType;
	public HideType hideType;
	
	public FragmentHome() {}
	
	public Fragment setDefaultTransitions(Context context)
	{
		TransitionSet enter = new TransitionSet();
		enter.addTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.slide_left).excludeTarget(R.id.card_stats, true).excludeChildren(R.id.layout_search, true));
		enter.addTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.slide_top).addTarget(R.id.card_stats).addTarget(R.id.layout_search));
		this.setEnterTransition(enter);
		
		TransitionSet exit = new TransitionSet();
		exit.addTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.slide_right).excludeTarget(R.id.card_stats, true).excludeChildren(R.id.layout_search, true));
		exit.addTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.slide_top).addTarget(R.id.card_stats).addTarget(R.id.layout_search));
		this.setExitTransition(exit);
		
		this.setAllowEnterTransitionOverlap(false);
		this.setAllowReturnTransitionOverlap(false);
		
		return this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		dbHelper = new DBHelper(getContext());
		preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		preferences.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        return inflater.inflate(R.layout.fragment_home, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState)
	{
		super.onViewCreated(v, savedInstanceState);
		
		recyclerView = (RecyclerView) v.findViewById(R.id.recycler_vocabulary);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new HomeAdapter());

		final SwipeRefreshLayout swipeContainer = (SwipeRefreshLayout)v.findViewById(R.id.swipe);
		swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() 
			{
				@Override
				public void onRefresh()
				{
					updateFilter();
					getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));
					
					swipeContainer.setRefreshing(false);
				} 
			});

        swipeContainer.setColorSchemeResources(R.color.accent);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.fragment_home, menu);
		SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.item_search));
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
		searchView.setOnQueryTextListener(this);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) 
	{
        switch (item.getItemId())
		{
			case R.id.item_filter:
				new FilterDialog().show(getFragmentManager(), "filter");
				return true;
				
			case R.id.item_add:
				startActivity(new Intent(getContext(), ActivityAdd.class));
            	return true;
        
			case R.id.item_learn_add_next:
				new LearnNextDialog().show(getFragmentManager(), "learn_next");
				return true;
				
			case R.id.item_import_file:
				startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("text/*"), 0);
				return true;

			case R.id.item_import_clipboard:
				ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);

				if (clipboard.hasPrimaryClip() && clipboard.getText() != null)
				{
					Bundle bundle = new Bundle();
					bundle.putString("text", clipboard.getText().toString());

					ImportDialog dialog = new ImportDialog();
					dialog.setArguments(bundle);
					dialog.show(getFragmentManager(), "import");
				}
				else
					DialogHelper.createDialog(getContext(), "Import", "Clipboard is empty or doesn't contain text!");

				return true;

			case R.id.item_export:
				dbHelper.exportToFile();
				return true;

			case R.id.item_load:
				DialogHelper.createDialog(getContext(), "Load", "Do you really want to load vocabularies from a file? Your current vocabularies will be lost!",
					"Load", new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							Intent i = new Intent(Intent.ACTION_GET_CONTENT);
							i.setType("*/*");

							startActivityForResult(i, 1);

							dialog.dismiss();
						}
					});
				return true;

			case R.id.item_save:
				dbHelper.saveToFile();
				return true;

			case R.id.item_clear:
				DialogHelper.createDialog(getContext(), "Clear", "Do you really want to delete every vocabulary? This cannot be undone!",
					"Delete filtered", 
					new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							dbHelper.deleteVocabularies(ShowType.values()[preferences.getInt("showType", 0)], StringHelper.toBooleanArray(preferences.getString("show", "")));
							updateFilter();
							getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));
							
							dialog.dismiss();
						}
					}, 
					"Delete", new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							dbHelper.deleteVocabularies();
							updateFilter();
							getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));
							
							dialog.dismiss();
						}
					});
				return true;
			case R.id.item_clear_reset:
				DialogHelper.createDialog(getContext(), "Reset", "Do you really want to reset all vocabularies? Your stats and progress will be lost. This cannot be undone!",
					"Reset filtered", new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							int[] vocabularies = dbHelper.getVocabularies(SortType.TIME_ADDED, ShowType.values()[preferences.getInt("showType", 0)], StringHelper.toBooleanArray(preferences.getString("show", "")), null);
							dbHelper.resetVocabulary(vocabularies);

							updateFilter();
							getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));
							
							dialog.dismiss();
						}
					}, 
					"Reset all", new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							int[] vocabularies = dbHelper.getVocabularies(SortType.TIME_ADDED, ShowType.ALL, null, null);
							dbHelper.resetVocabulary(vocabularies);

							updateFilter();
							getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));
							
							dialog.dismiss();
						}
					});
				return true;

			case R.id.item_learn_add_all:
				dbHelper.updateVocabulariesLearned(true);
				updateFilter();
				getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));
				return true;

			case R.id.item_learn_remove_all:
				dbHelper.updateVocabulariesLearned(false);
				updateFilter();
				getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));
				return true;
				
			case R.id.item_debug:
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				Cursor res =  db.rawQuery("SELECT kanji FROM vocab", null);
				if (res.getCount() <= 0)
				{
					res.close();
					return true;
				}

				res.moveToFirst();

				final HashMap<Character, Integer> map = new HashMap<>();
				char[] chars;
				int i;
				Integer old;
				
				do
				{
					chars = StringHelper.getKanji(res.getString(0));
					
					for (i = 0; i < chars.length; ++i)
					{
						old = map.get(chars[i]);
						map.put(chars[i], old == null ? 1 : old + 1);
					}
				}
				while(res.moveToNext());

				DialogHelper.createDialog(getContext(), "Kanji", "This database contains " + map.keySet().size() + " different kanji.");
				
				res.close();
				return true;
		}

        return super.onOptionsItemSelected(item);
    }

	@Override
	public boolean onQueryTextSubmit(String query)
	{
		return false;
	}

	@Override
	public boolean onQueryTextChange(String query)
	{
		searchQuery = query;
		recyclerView.scrollToPosition(0);
		updateFilter();

		return true;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences p1, String key)
	{
		switch (key)
		{
			case "show":
			case "sortType":
			case "viewType":
			case "showType":
			case "hideType":
			case "vocabulariesChanged":
				updateFilter();
				break;
		}
	}
	
	public void updateFilter()
	{
		vocabularies = dbHelper.getVocabularies((sortType = SortType.values()[preferences.getInt("sortType", 0)]), ShowType.values()[preferences.getInt("showType", 0)], StringHelper.toBooleanArray(preferences.getString("show", "")), searchQuery);
		viewType = ViewType.values()[preferences.getInt("viewType", 1)];
		hideType = HideType.values()[preferences.getInt("hideType", 0)];
		
		if (recyclerView != null)
			recyclerView.getAdapter().notifyDataSetChanged();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, final Intent data) 
	{
		if (resultCode == Activity.RESULT_OK)
		{
			if (requestCode == 0)
			{
				StringBuilder sb = new StringBuilder();

				try
				{
					BufferedReader reader = new BufferedReader(new InputStreamReader(getContext().getContentResolver().openInputStream(data.getData())));

					String line;
					while ((line = reader.readLine()) != null)
						sb.append(line).append('\n');

					reader.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				Bundle bundle = new Bundle();
				bundle.putString("text", sb.toString());

				ImportDialog dialog = new ImportDialog();
				dialog.setArguments(bundle);
				dialog.show(getFragmentManager(), "import");
			}
			else if (requestCode == 1)
			{
				try
				{
					dbHelper.loadFromFile(new FileInputStream(getActivity().getContentResolver().openFileDescriptor(data.getData(), "r").getFileDescriptor()));
					updateFilter();
					getContext().sendBroadcast(new Intent(getContext(), NotificationHelper.class));
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();

		getActivity().setTitle("Home");
		updateFilter();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		dbHelper.close();
		preferences.unregisterOnSharedPreferenceChangeListener(this);
	}

	public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener
	{
		@Override
		public void onClick(final View v)
		{
			if (v.getId() == R.id.button_filter)
				new FilterDialog().show(getFragmentManager(), "filter");

			else if (v.getId() == R.id.button_jisho)
				JishoHelper.search(getContext(), searchQuery);
				
			else if (v.getId() == R.id.button_start_quiz)
				getContext().startActivity(new Intent(getContext(), ActivityQuiz.class));
				
			else if (v.getId() == R.id.button_overflow)
			{
				final int index = recyclerView.getChildAdapterPosition((View) ((View) ((View) v.getParent()).getParent()).getParent());
				final int id = vocabularies[index - 1];
				PopupMenu popup = new PopupMenu(getContext(), v);
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
					{
						@Override
						public boolean onMenuItemClick(MenuItem item)
						{
							switch (item.getItemId()) 
							{
								case R.id.delete:
									DialogHelper.createDialog(getContext(), "Delete", "Do you really want to delete this vocabulary?", 
									"Delete", new DialogInterface.OnClickListener() 
										{
											public void onClick(DialogInterface dialog, int which) 
											{
												dbHelper.deleteVocabulary(id);
										
												int[] vocabularies1 = new int[vocabularies.length - 1];
												System.arraycopy(vocabularies, 0, vocabularies1, 0, index - 1);
												System.arraycopy(vocabularies, index, vocabularies1, index - 1, vocabularies.length - index);
												vocabularies = vocabularies1;
												notifyItemRemoved(index);

												dialog.dismiss();
											}
										});

									return true;

								case R.id.detail:
									onClick((View)((View) v.getParent()).getParent());
									return true;

								case R.id.edit:
									Intent intent1 = new Intent(getContext(), ActivityAdd.class);
									intent1.putExtra("id", id);
									startActivity(intent1);

									return true;

								case R.id.learn_add:
									dbHelper.updateVocabularyLearned(id, true);
									updateFilter();

									return true;

								case R.id.learn_remove:
									dbHelper.updateVocabularyLearned(id, false);
									updateFilter();

									return true;

								default:
									return false;
							}
						}
					});
				MenuInflater inflater = popup.getMenuInflater();
				inflater.inflate(R.menu.item_vocabulary, popup.getMenu());
				boolean learned = dbHelper.getInt(id, "learned") == 1;
				popup.getMenu().findItem(R.id.learn_add).setVisible(!learned);
				popup.getMenu().findItem(R.id.learn_remove).setVisible(learned);
				popup.show();
			}
			else if (v.getId() == R.id.button_overflow)
			{
				v.findViewById(R.id.button_overflow).setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View view)
						{
							PopupMenu popup = new PopupMenu(getContext(), view);
							popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
								{
									@Override
									public boolean onMenuItemClick(MenuItem item)
									{
										switch (item.getItemId()) 
										{
											case R.id.item_open_jisho_kanji:
												JishoHelper.search(getContext(), searchQuery + " #kanji");
												return true;
											case R.id.item_settings:
												getContext().startActivity(new Intent(getContext(), ActivitySettings.class).setAction(ActivitySettings.ACTION_STROKE_ORDER));
												return true;
											default:
												return false;
										}
									}
								});
							MenuInflater inflater = popup.getMenuInflater();
							inflater.inflate(R.menu.item_stroke_order, popup.getMenu());
							popup.show();
						}
					});
			}
			else
			{
				Fragment fragment = new FragmentDetail().setDefaultTransitions(getContext());

				Bundle bundle = new Bundle();
				bundle.putInt("id", vocabularies[recyclerView.getChildAdapterPosition((View) v.getParent()) - 1]);
				fragment.setArguments(bundle);

				View v1 = v.findViewById(R.id.text_kanji);
				View v2 = v.findViewById(R.id.text_reading);
				View v3 = v.findViewById(R.id.text_meaning);

				getFragmentManager().beginTransaction()
					.replace(R.id.layout_content, fragment)
					.addToBackStack("")
					.addSharedElement(v, v.getTransitionName())
					.addSharedElement(v1, v1.getTransitionName())
					.addSharedElement(v2, v2.getTransitionName())
					.addSharedElement(v3, v3.getTransitionName())
					.commit();
			}
		}

		public class StatsViewHolder extends RecyclerView.ViewHolder 
		{
			final TextView text_progress_learned, text_progress_total,
			text_progress_kanji, text_progress_reading, text_progress_meaning,
			text_date, text_next_review_values, date_next_review;
			
			final ProgressBar progress_learned, progress_total,
			progress_kanji, progress_meaning, progress_reading;
			
			final PercentageGraphView percentage_graph_types, percentage_graph_categories;
			final LineGraphView line_graph_review;
			final GraphView graph_reviewed;
			final Button button_start_quiz;
			
			public StatsViewHolder(View v) 
			{
				super(v);
				
				final View layout_stats = v.findViewById(R.id.layout_stats);
				final ImageView image_menu = (ImageView) v.findViewById(R.id.image_menu);

				v.setOnClickListener(new View.OnClickListener()
				{
						@Override
						public void onClick(View p1)
						{
							if (layout_stats.getVisibility() == View.VISIBLE)
							{
								layout_stats.setVisibility(View.GONE);
								image_menu.setImageResource(R.drawable.menu_down);
							}
							else
							{
								layout_stats.setVisibility(View.VISIBLE);
								image_menu.setImageResource(R.drawable.menu_up);
							}
						}
				});
				
				button_start_quiz = (Button) v.findViewById(R.id.button_start_quiz);
				button_start_quiz.setOnClickListener(HomeAdapter.this);
				
				text_progress_learned = (TextView) v.findViewById(R.id.text_progress_learned);
				text_progress_total = (TextView) v.findViewById(R.id.text_progress_total);
				text_progress_kanji = (TextView) v.findViewById(R.id.text_progress_kanji);
				text_progress_reading = (TextView) v.findViewById(R.id.text_progress_reading);
				text_progress_meaning = (TextView) v.findViewById(R.id.text_progress_meaning);
				
				text_date = (TextView) v.findViewById(R.id.text_date);
				text_next_review_values = (TextView) v.findViewById(R.id.text_next_review_values);
				date_next_review = (TextView) v.findViewById(R.id.date_next_review);
				
				progress_learned = (ProgressBar) v.findViewById(R.id.progress_learned);
				progress_total = (ProgressBar) v.findViewById(R.id.progress_total);
				progress_kanji = (ProgressBar) v.findViewById(R.id.progress_kanji);
				progress_reading = (ProgressBar) v.findViewById(R.id.progress_reading);
				progress_meaning = (ProgressBar) v.findViewById(R.id.progress_meaning);
				
				percentage_graph_types = (PercentageGraphView) v.findViewById(R.id.percentage_graph_types);
				percentage_graph_categories = (PercentageGraphView) v.findViewById(R.id.percentage_graph_categories);
				
				line_graph_review = (LineGraphView) v.findViewById(R.id.line_graph_review);
				
				graph_reviewed = (GraphView) v.findViewById(R.id.graph_reviewed);
			}
		}
		
		public class SearchViewHolder extends RecyclerView.ViewHolder 
		{
			final View button_stroke_order, card_stroke_order, progress_stroke_order;
			final TextView text_results;
			final ViewGroup layout_stroke_order;
			
			public SearchViewHolder(View v) 
			{
				super(v);
				
				card_stroke_order = v.findViewById(R.id.card_stroke_order);
				layout_stroke_order = (ViewGroup) v.findViewById(R.id.layout_stroke_order);
				progress_stroke_order = v.findViewById(R.id.progress_stroke_order);
				
				v.findViewById(R.id.button_jisho).setOnClickListener(HomeAdapter.this);
				v.findViewById(R.id.button_filter).setOnClickListener(HomeAdapter.this);
				v.findViewById(R.id.button_overflow).setOnClickListener(HomeAdapter.this);
				(button_stroke_order = v.findViewById(R.id.button_stroke_order)).setOnClickListener(new View.OnClickListener()
				{
						@Override
						public void onClick(View p1)
						{
							if (card_stroke_order.getVisibility() == View.VISIBLE)
								card_stroke_order.setVisibility(View.GONE);
						
							else
							{
								card_stroke_order.setVisibility(View.VISIBLE);
								
								if (progress_stroke_order.getVisibility() == View.VISIBLE)
								{
									final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
									params.addRule(RelativeLayout.BELOW, R.id.text_title_stroke_order);

									JishoHelper.addStrokeOrderView(getContext(), searchQuery, layout_stroke_order, params, progress_stroke_order, false, true);
								}
							}
						}
				});
				text_results = (TextView) v.findViewById(R.id.text_results);
			}
		}
		
		public class VocabularyViewHolder extends RecyclerView.ViewHolder 
		{
			final TextView text_category, text_kanji, text_reading, text_meaning;
			final ImageView image_check;
			final View card_vocabulary;
			
			public VocabularyViewHolder(View v) 
			{
				super(v);
				
				card_vocabulary = v.findViewById(R.id.card_vocabulary);
				card_vocabulary.setOnClickListener(HomeAdapter.this);
				v.findViewById(R.id.button_overflow).setOnClickListener(HomeAdapter.this);
				
				text_category = (TextView) v.findViewById(R.id.text_category);
				text_kanji = (TextView) v.findViewById(R.id.text_kanji);
				text_reading = (TextView) v.findViewById(R.id.text_reading);
				text_meaning = (TextView) v.findViewById(R.id.text_meaning);
				
				image_check = (ImageView) v.findViewById(R.id.image_check);
				
				text_kanji.setTextLocale(Locale.JAPANESE);
			}
		}

		
		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			if (viewType == 0)
				return new StatsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stats, parent, false));
			
			else if (viewType == 1)
				return new SearchViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search, parent, false));
			
			else
				return new VocabularyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vocabulary, parent, false));
			
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) 
		{
			int viewHolderType = getItemViewType(position);
			if (viewHolderType == 0)
			{
				StatsViewHolder holder1 = (StatsViewHolder) holder;
				
				int learned_total = 0,critical = 0;
				int correct_kanji = 0, total_kanji = 0, correct_reading = 0, total_reading = 0,
				correct_meaning = 0, total_meaning = 0;
				long nextReview = 0;
				int[] review = new int[25], types = new int[Vocabulary.types.size()];
				final ArrayList<Integer> categories_count = new ArrayList<>();
				categories_count.add(0);
				
				Cursor res = dbHelper.getReadableDatabase().rawQuery("SELECT category, learned, type, nextReview, timesChecked_kanji, timesChecked_reading, timesChecked_meaning, timesCorrect_kanji, timesCorrect_reading, timesCorrect_meaning FROM vocab", null);
				int count = res.getCount();
				if (count > 0)
				{
					res.moveToFirst();

					int vCategory;
					long vNextReview;

					do
					{
						types[res.getInt(res.getColumnIndex("type"))]++;

						if (res.getInt(res.getColumnIndex("learned")) == 1)
						{
							vCategory = res.getInt(res.getColumnIndex("category"));
							vNextReview = res.getLong(res.getColumnIndex("nextReview"));

							while (categories_count.size() <= vCategory + 1)
								categories_count.add(0);

							categories_count.set(vCategory + 1, categories_count.get(vCategory + 1) + 1);

							learned_total++;
							if (vCategory <= 1)
								critical++;
							correct_kanji += res.getInt(res.getColumnIndex("timesCorrect_kanji"));
							total_kanji += res.getInt(res.getColumnIndex("timesChecked_kanji"));
							correct_reading += res.getInt(res.getColumnIndex("timesCorrect_reading"));
							total_reading += res.getInt(res.getColumnIndex("timesChecked_reading"));
							correct_meaning += res.getInt(res.getColumnIndex("timesCorrect_meaning"));
							total_meaning += res.getInt(res.getColumnIndex("timesChecked_meaning"));

							if (nextReview == 0)
								nextReview = vNextReview;
							else
								nextReview = Math.min(nextReview, vNextReview);

							if (vNextReview < System.currentTimeMillis())
								review[0]++;
							else if (vNextReview < System.currentTimeMillis() + 1000 * 60 * 60 * 24)
								review[1 + (int) ((vNextReview - System.currentTimeMillis()) / (1000 * 60 * 60))]++;
						}
						else
							categories_count.set(0, categories_count.get(0) + 1);

					}
					while (res.moveToNext());
				}
				
				res.close();
				
				int reviews1 = 0;
				for (int i = 0; i < review.length; i++)
					review[i] = (reviews1 += review[i]);

				final String[] categories = new String[categories_count.size()];
				categories[0] = "Not Learned";
				if (categories.length > 1)
				{
					categories[1] = "Critical";
					for (int i = 2; i < categories.length; ++i)
						categories[i] = "" + (i - 1);
				}
				
				final int[] categories_values = new int[categories_count.size()];
				for(int i = 0; i < categories_count.size(); ++i)
					categories_values[i] = categories_count.get(i);
				
				holder1.button_start_quiz.setVisibility(review[0] > 0 ? View.VISIBLE : View.GONE);
					
				holder1.text_progress_learned.setText(learned_total + " / " + count);
				holder1.progress_learned.setMax(count);
				holder1.progress_learned.setProgress(learned_total - critical);
				holder1.progress_learned.setSecondaryProgress(learned_total);

				holder1.percentage_graph_types.setValues(Vocabulary.types.toArray(new String[Vocabulary.types.size()]), types, true, true);
				holder1.percentage_graph_categories.setValues(categories, categories_values, true, true);
				
				holder1.text_progress_total.setText((correct_meaning + correct_reading + correct_kanji) + " / " + (total_meaning + total_reading + total_meaning));
				holder1.progress_total.setMax(total_meaning + total_reading + total_kanji);
				holder1.progress_total.setProgress(correct_meaning + correct_reading + correct_kanji);

				holder1.text_progress_kanji.setText(correct_kanji + " / " + total_kanji);
				holder1.progress_kanji.setMax(total_kanji);
				holder1.progress_kanji.setProgress(correct_kanji);

				holder1.text_progress_reading.setText(correct_reading + " / " + total_reading);
				holder1.progress_reading.setMax(total_reading);
				holder1.progress_reading.setProgress(correct_reading);

				holder1.text_progress_meaning.setText(correct_meaning + " / " + total_meaning);
				holder1.progress_meaning.setMax(total_meaning);
				holder1.progress_meaning.setProgress(correct_meaning);

				holder1.line_graph_review.setValues(review);

				long lastDate = preferences.getLong("lastReviewDate", 0);
				int[] review1 = StringHelper.toIntArray(preferences.getString("review1", ""));
				int[] review2 = StringHelper.toIntArray(preferences.getString("review2", ""));
				
				GregorianCalendar calendar = new GregorianCalendar();
				calendar.setTimeInMillis(lastDate);
				int days = new GregorianCalendar().get(Calendar.DAY_OF_YEAR) - calendar.get(Calendar.DAY_OF_YEAR);
				if (days < 0)
					days += calendar.isLeapYear(calendar.get(Calendar.YEAR)) ? 366 : 365;
					
				if (days > 30 || review1.length != 30)
				{
					review1 = new int[30];
					review2 = new int[30];
				}
				else
				{
					System.arraycopy(review1, days, review1, 0, 30 - days);
					System.arraycopy(review2, days, review2, 0, 30 - days);
					
					for (int i = 29; i >= 30 - days; --i)
					{
						review1[i] = 0;
						review2[i] = 0;
					}
				}
				preferences.edit()
				.putLong("lastReviewDate", System.currentTimeMillis())
				.putString("review1", StringHelper.toString(review1))
				.putString("review2", StringHelper.toString(review2))
				.apply();
				
				holder1.graph_reviewed.setValues(review1, review2);
				
				calendar = new GregorianCalendar();
				calendar.add(Calendar.DAY_OF_YEAR, -30);
				holder1.text_date.setText(DateFormat.getDateFormat(getContext()).format(calendar.getTime()));

				holder1.text_next_review_values.setText(
					review[0] + "\n"
					+ review[1] + "\n"
					+ review[24]
				);
				holder1.date_next_review.setText("Next Review: " + (nextReview < System.currentTimeMillis() ? "Now" : nextReview == 0 ? "Never" : new SimpleDateFormat().format(new Date(nextReview))));
			}
			else if (viewHolderType == 1)
			{
				SearchViewHolder holder1 = (SearchViewHolder) holder;
				
				holder1.button_stroke_order.setVisibility(JishoHelper.isStrokeOrderAvailable(getContext()) && StringHelper.isKanaOrKanji(searchQuery) ? View.VISIBLE : View.GONE);
				holder1.text_results.setText(vocabularies.length + " results for search \"" + searchQuery + "\"");
				holder1.card_stroke_order.setVisibility(View.GONE);
				
				if (holder1.progress_stroke_order.getVisibility() == View.GONE)
				{
					holder1.progress_stroke_order.setVisibility(View.VISIBLE);
					holder1.layout_stroke_order.removeView(holder1.itemView.findViewWithTag("stroke_order"));
				}
			}
			else
			{
				VocabularyViewHolder holder1 = (VocabularyViewHolder) holder;
				
				int id = vocabularies[position - 1];

				Cursor res = dbHelper.getReadableDatabase().rawQuery("SELECT type, category, learned, kanji, reading, meaning, additionalInfo, showInfo, nextReview FROM vocab WHERE id = ?", new String[] {"" + id});
				res.moveToFirst();

				boolean learned = res.getInt(res.getColumnIndex("learned")) == 1;
				int category = res.getInt(res.getColumnIndex("category"));
				String kanji = res.getString(res.getColumnIndex("kanji"));
				String[] reading = StringHelper.toStringArray(res.getString(res.getColumnIndex("reading")));
				String[] meaning = StringHelper.toStringArray(res.getString(res.getColumnIndex("meaning")));
				String additionalInfo = res.getString(res.getColumnIndex("additionalInfo"));
				boolean showInfo = res.getInt(res.getColumnIndex("showInfo")) == 1;
				long nextReview = res.getLong(res.getColumnIndex("nextReview"));
				int type = res.getInt(res.getColumnIndex("type"));
				
				holder1.text_category.setVisibility(View.GONE);
				
				if ((searchQuery == null || searchQuery.isEmpty()))
				{
					if (sortType == SortType.CATEGORY || sortType == SortType.CATEGORY_REVERSED)
					{
						if (position == 1 || dbHelper.getInt(vocabularies[position - 2], "category") != category)
						{
							holder1.text_category.setVisibility(View.VISIBLE);
							holder1.text_category.setText(category == 0 ? "Critical vocabularies" : "Category " + category);
						}
					}
					else if (sortType == SortType.TYPE)
					{
						if (position == 1 || dbHelper.getInt(vocabularies[position - 2], "type") != type)
						{
							holder1.text_category.setVisibility(View.VISIBLE);
							holder1.text_category.setText(Vocabulary.types.get(type));
						}
					}
					else if (sortType == SortType.NEXT_REVIEW)
					{
						long nextReview1 = position == 1 ? 0 : dbHelper.getLong(vocabularies[position - 2], "nextReview");
						if (position == 1 || learned != (dbHelper.getInt(vocabularies[position - 2], "learned") == 1)
						|| learned && (
						nextReview >= System.currentTimeMillis() && nextReview1 < System.currentTimeMillis()
						|| nextReview >= System.currentTimeMillis() + 1000 * 60 * 60 && nextReview1 < System.currentTimeMillis() + 1000 * 60 * 60
						|| nextReview >= System.currentTimeMillis() && (nextReview - System.currentTimeMillis() < 1000 * 60 * 60 * 48 ? 
						((int)(nextReview - System.currentTimeMillis()) / 1000 / 60 / 60 > (int)((nextReview1 - System.currentTimeMillis()) / 1000 / 60 / 60))
						: ((int)((nextReview - System.currentTimeMillis()) / 1000 / 60 / 60 / 24) > (int)(((nextReview1 - System.currentTimeMillis()) / 1000 / 60 / 60 / 24))
						))))
						{
							holder1.text_category.setVisibility(View.VISIBLE);
							holder1.text_category.setText((!learned ? "Not yet learned" : nextReview < System.currentTimeMillis() ? "Review now" 
														  : nextReview < System.currentTimeMillis() + 1000 * 60 * 60 ? "Review in the next hour"
														  : nextReview < System.currentTimeMillis() + 1000 * 60 * 60 * 48  ? "Review in " + ((nextReview - System.currentTimeMillis()) / 1000 / 60 / 60 + 1) + " hours"
														  : "Review in " + ((nextReview - System.currentTimeMillis()) / 1000 / 60 / 60 / 24 + 1) + " days"));
						}
					}
				}
					
				res.close();
				
				holder1.card_vocabulary.setTransitionName("card" + id);
				
				if (hideType == HideType.NOTHING)
				{
					holder1.text_kanji.setText(Vocabulary.correctAnswer(QuestionType.KANJI, kanji, reading, meaning, additionalInfo, showInfo));
					holder1.text_kanji.setTransitionName("kanji" + id);

					holder1.text_reading.setText(reading.length == 0 ? "" : Vocabulary.correctAnswer(QuestionType.READING_INFO, kanji, reading, meaning, additionalInfo, showInfo));
					holder1.text_reading.setTransitionName("reading" + id);

					holder1.text_meaning.setText(Vocabulary.correctAnswer(QuestionType.MEANING, kanji, reading, meaning, additionalInfo, showInfo));
					holder1.text_meaning.setTransitionName("meaning" + id);
				}
				else if (hideType == HideType.KANJI)
				{
					holder1.text_kanji.setText(Vocabulary.correctAnswer(QuestionType.READING_INFO, kanji, reading, meaning, additionalInfo, showInfo));
					holder1.text_kanji.setTransitionName("reading" + id);

					holder1.text_reading.setText("");
					holder1.text_reading.setTransitionName("" + id);

					holder1.text_meaning.setText(Vocabulary.correctAnswer(QuestionType.MEANING, kanji, reading, meaning, additionalInfo, showInfo));
					holder1.text_meaning.setTransitionName("meaning" + id);
				}
				else if (hideType == HideType.READING)
				{
					holder1.text_kanji.setText(Vocabulary.correctAnswer(QuestionType.KANJI, kanji, reading, meaning, additionalInfo, showInfo));
					holder1.text_kanji.setTransitionName("kanji" + id);
					
					holder1.text_reading.setText("");
					holder1.text_reading.setTransitionName("" + id);

					holder1.text_meaning.setText(Vocabulary.correctAnswer(QuestionType.MEANING, kanji, reading, meaning, additionalInfo, showInfo));
					holder1.text_meaning.setTransitionName("meaning" + id);
				}
				else if (hideType == HideType.MEANING)
				{
					holder1.text_kanji.setText(Vocabulary.correctAnswer(QuestionType.KANJI, kanji, reading, meaning, additionalInfo, showInfo));
					holder1.text_kanji.setTransitionName("kanji" + id);
					
					holder1.text_reading.setText("");
					holder1.text_reading.setTransitionName("" + id);

					holder1.text_meaning.setText(Vocabulary.correctAnswer(QuestionType.READING_INFO, kanji, reading, meaning, additionalInfo, showInfo));
					holder1.text_meaning.setTransitionName("reading" + id);
				}
				
				if (viewType == ViewType.LARGE)
				{
					holder1.text_kanji.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
					holder1.text_reading.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
					holder1.text_meaning.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				}
				else if (viewType == ViewType.MEDIUM)
				{
					holder1.text_kanji.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);
					holder1.text_reading.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
					holder1.text_meaning.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
				}
				else if (viewType == ViewType.SMALL)
				{
					holder1.text_kanji.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
					holder1.text_reading.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
					holder1.text_meaning.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
				}

				holder1.text_reading.setVisibility(reading.length > 0 && hideType == HideType.NOTHING ? View.VISIBLE : View.GONE);
				holder1.image_check.setVisibility(learned ? View.VISIBLE : View.GONE);
				holder1.image_check.setImageResource(category == 0 ? R.drawable.alert : R.drawable.check);
			}
		}

		@Override
		public int getItemCount() 
		{
			return vocabularies.length + 1;
		}

		@Override
		public int getItemViewType(int position)
		{ 
			if (position == 0)
				return searchQuery == null || searchQuery.isEmpty() ? 0 : 1;

			return 2;
		}
	}
}
