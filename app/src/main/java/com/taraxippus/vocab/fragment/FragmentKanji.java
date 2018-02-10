package com.taraxippus.vocab.fragment;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.format.DateFormat;
import android.transition.AutoTransition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.taraxippus.vocab.ActivityAddKanji;
import com.taraxippus.vocab.ActivityQuiz;
import com.taraxippus.vocab.ActivitySettings;
import com.taraxippus.vocab.ActivityStats;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.dialog.DialogFilter;
import com.taraxippus.vocab.dialog.DialogHelper;
import com.taraxippus.vocab.dialog.DialogImportKanjiJisho;
import com.taraxippus.vocab.dialog.DialogLearnNext;
import com.taraxippus.vocab.fragment.FragmentDetail;
import com.taraxippus.vocab.util.JishoHelper;
import com.taraxippus.vocab.util.NotificationHelper;
import com.taraxippus.vocab.util.StringHelper;
import com.taraxippus.vocab.vocabulary.DBHelper;
import com.taraxippus.vocab.vocabulary.HideType;
import com.taraxippus.vocab.vocabulary.Kanji;
import com.taraxippus.vocab.vocabulary.QuestionType;
import com.taraxippus.vocab.vocabulary.ShowType;
import com.taraxippus.vocab.vocabulary.SortType;
import com.taraxippus.vocab.vocabulary.ViewType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class FragmentKanji extends Fragment implements SearchView.OnQueryTextListener, SharedPreferences.OnSharedPreferenceChangeListener
{
	public RecyclerView recyclerView;
	public SharedPreferences preferences;	
	public DBHelper dbHelper;

	public char[] kanji;
	public ViewType viewType;
	public String searchQuery;
	public SortType sortType;
	public HideType hideType;
	
	public FragmentKanji() {}

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

		searchQuery = savedInstanceState == null ? "" : savedInstanceState.getString("searchQuery");
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
        recyclerView.setAdapter(new KanjiAdapter());

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
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Bundle bundle;
		DialogFragment dialog;
		switch (item.getItemId())
		{
			case R.id.item_filter:
				dialog = new DialogFilter();
				bundle = new Bundle();
				bundle.putBoolean("kanji", true);
				dialog.setArguments(bundle);
				dialog.show(getFragmentManager(), "filter");
				
				return true;
				
			case R.id.item_add:
				getContext().startActivity(new Intent(getContext(), ActivityAddKanji.class));
				return true;
				
			case R.id.item_learn_add_next:
				dialog = new DialogLearnNext();
				bundle = new Bundle();
				bundle.putBoolean("kanji", true);
				dialog.setArguments(bundle);
				dialog.show(getFragmentManager(), "learn_next");
				return true;
				
			case R.id.item_import_jisho:
				new DialogImportKanjiJisho().show(getFragmentManager(), "import_jisho");
				return true;
				
			case R.id.item_debug:
				DialogHelper.createDialog(getContext(), "Debug", "Nya?");
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.fragment_kanji, menu);
		SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.item_search));
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
		if (!searchQuery.isEmpty())
		{
			menu.findItem(R.id.item_search).expandActionView();
			searchView.setQuery(searchQuery, true);
		}
		searchView.setOnQueryTextListener(this);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onQueryTextSubmit(String query)
	{
		return false;
	}

	@Override
	public boolean onQueryTextChange(String query)
	{
		if (!searchQuery.equals(query))
		{
			searchQuery = query;
			recyclerView.scrollToPosition(0);
			updateFilter();
		}

		return true;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences p1, String key)
	{
		switch (key)
		{
			case "sortTypeKanji":
			case "viewTypeKanji":
			case "showTypeKanji":
			case "hideTypeKanji":
			case "sortReversedKanji":
			case "kanjiChanged":
				updateFilter();
				break;
		}
	}

	public void updateFilter()
	{
		kanji = dbHelper.getKanji((sortType = SortType.values()[preferences.getInt("sortTypeKanji", 0)]), ShowType.values()[preferences.getInt("showTypeKanji", 0)], preferences.getBoolean("sortReversedKanji", false), searchQuery);
		viewType = ViewType.values()[preferences.getInt("viewTypeKanji", 1)];
		hideType = HideType.values()[preferences.getInt("hideTypeKanji", 0)];

		TransitionManager.beginDelayedTransition(recyclerView, new AutoTransition().excludeTarget(TextView.class, true).setDuration(300));

		if (recyclerView != null)
			recyclerView.getAdapter().notifyDataSetChanged();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		outState.putString("searchQuery", searchQuery);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		getActivity().setTitle("Kanji");
		updateFilter();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		dbHelper.close();
		preferences.unregisterOnSharedPreferenceChangeListener(this);
	}
	
	public class KanjiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener
	{
		@Override
		public void onClick(final View v)
		{
			if (v.getId() == R.id.button_stats)
				getContext().startActivity(new Intent(getContext(), ActivityStats.class));
			
			else if (v.getId() == R.id.button_filter)
			{
				DialogFilter dialog = new DialogFilter();
				Bundle bundle = new Bundle();
				bundle.putBoolean("kanji", true);
				dialog.setArguments(bundle);
				dialog.show(getFragmentManager(), "filter");
			}
			else if (v.getId() == R.id.button_jisho)
				JishoHelper.search(getContext(), searchQuery);

			else if (v.getId() == R.id.button_start_quiz)
				getContext().startActivity(new Intent(getContext(), ActivityQuiz.class));

			else if (v.getId() == R.id.button_overflow)
			{
				final int index = recyclerView.getChildAdapterPosition((View) ((View) ((View) v.getParent()).getParent()).getParent());
				final char id = kanji[index - 1];
				PopupMenu popup = new PopupMenu(getContext(), v);
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
					{
						@Override
						public boolean onMenuItemClick(MenuItem item)
						{
							switch (item.getItemId()) 
							{
								case R.id.delete:
									DialogHelper.createDialog(getContext(), "Delete", "Do you really want to delete this kanji?", 
										"Delete", new DialogInterface.OnClickListener() 
										{
											public void onClick(DialogInterface dialog, int which) 
											{
												dbHelper.deleteKanji(id);

												char[] kanji1 = new char[kanji.length - 1];
												System.arraycopy(kanji, 0, kanji1, 0, index);
												System.arraycopy(kanji, index + 1, kanji1, index, kanji.length - index - 1);
												kanji = kanji1;
												notifyItemRemoved(index);

												dialog.dismiss();
											}
										});

									return true;

								case R.id.detail:
									onClick((View)((View) v.getParent()).getParent());
									return true;

								case R.id.edit:
									Intent intent1 = new Intent(getContext(), ActivityAddKanji.class);
									intent1.putExtra("kanji", id);
									startActivity(intent1);

									return true;

								case R.id.learn_add:
									dbHelper.updateKanjiLearned(id, true);
									updateFilter();

									return true;

								case R.id.learn_remove:
									dbHelper.updateKanjiLearned(id, false);
									updateFilter();

									return true;

								default:
									return false;
							}
						}
					});
				MenuInflater inflater = popup.getMenuInflater();
				inflater.inflate(R.menu.item_vocabulary, popup.getMenu());
				boolean learned = dbHelper.getIntKanji(id, "learned") == 1;
				popup.getMenu().findItem(R.id.learn_add).setTitle("Add to learned kanji").setVisible(!learned);
				popup.getMenu().findItem(R.id.learn_remove).setTitle("Remove from learned kanji").setVisible(learned);
				popup.show();
			}
			else if (v.getId() == R.id.button_stroke_order)
			{
				FragmentHome.SearchViewHolder holder = (FragmentHome.SearchViewHolder) recyclerView.getChildViewHolder((View) ((View) ((View) v.getParent()).getParent()).getParent());

				TransitionManager.beginDelayedTransition(recyclerView);

				if (holder.card_stroke_order.getVisibility() == View.VISIBLE)
					holder.card_stroke_order.setVisibility(View.GONE);

				else
				{
					holder.card_stroke_order.setVisibility(View.VISIBLE);

					if (holder.progress_stroke_order.getVisibility() == View.VISIBLE)
					{
						final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
						params.addRule(RelativeLayout.BELOW, R.id.text_title_stroke_order);

						JishoHelper.addStrokeOrderView(getContext(), searchQuery, holder.layout_stroke_order, params, holder.progress_stroke_order, false, true);
					}
				}
			}
			else if (v.getId() == R.id.button_sentences)
			{}
			else if (v.getId() == R.id.button_overflow_stroke_order)
			{
				PopupMenu popup = new PopupMenu(getContext(), v);
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
			else if (v.getId() == R.id.button_overflow_sentences)
			{}
			else
			{
				Fragment fragment = new FragmentDetailKanji().setDefaultTransitions(getContext());

				Bundle bundle = new Bundle();
				bundle.putChar("kanji", kanji[recyclerView.getChildAdapterPosition((View) v.getParent()) - 1]);
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

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			if (viewType == 0)
			{
				FragmentHome.StatsViewHolder holder = new FragmentHome.StatsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stats, parent, false), this, recyclerView);
				((TextView) holder.itemView.findViewById(R.id.text_learned_progress)).setText("Kanji learned:");
				((TextView) holder.itemView.findViewById(R.id.text_percentage_graph_types)).setText("Kanji stroke count:");
				((TextView) holder.itemView.findViewById(R.id.text_percentage_graph_categories)).setText("Kanji categories:");
				((TextView) holder.itemView.findViewById(R.id.text_graph_reviewed)).setText("Kanji reviewed:");
				((TextView) holder.itemView.findViewById(R.id.text_line_graph_review)).setText("Kanji to review:");
				((TextView) holder.itemView.findViewById(R.id.text_next_review)).setText("Kanji to review:");
				
				return holder;
			}
			else if (viewType == 1)
			{
				FragmentHome.SearchViewHolder holder = new FragmentHome.SearchViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search, parent, false), this);
				holder.recycler_kanji_contained.setAdapter(holder.adapter_kanji_contained = new FragmentDetail.VocabularyAdapter(getActivity(), dbHelper, holder.recycler_kanji_contained, null));
				holder.button_sentences.setVisibility(View.GONE);
				return holder;
			} 
			else
			{
				FragmentHome.VocabularyViewHolder holder = new FragmentHome.VocabularyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vocabulary, parent, false), this);
				((CardView) holder.card_vocabulary).setRadius(0);
				return holder;
			}
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) 
		{
			int viewHolderType = getItemViewType(position);
			if (viewHolderType == 0)
			{
				FragmentHome.StatsViewHolder holder1 = (FragmentHome.StatsViewHolder) holder;

				int learned_total = 0, critical = 0;
				long nextReview = 0;
				int[] review = new int[25];
				final ArrayList<Integer> categories_count = new ArrayList<>();
				final HashMap<Integer, Integer> strokes_count = new HashMap<>();
				categories_count.add(0);

				Cursor res = dbHelper.getReadableDatabase().rawQuery("SELECT category, learned, strokes, nextReview FROM kanji", null);
				int count = res.getCount();
				if (count > 0)
				{
					res.moveToFirst();

					int vCategory, vStrokes;
					long vNextReview;

					do
					{
						vStrokes = res.getInt(res.getColumnIndex("strokes"));
						strokes_count.put(vStrokes, strokes_count.get(vStrokes) != null ? strokes_count.get(vStrokes) + 1 : 1);
						
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
					categories[1] = "New";
					for (int i = 2; i < categories.length; ++i)
						categories[i] = "" + (i - 1);
				}

				final int[] categories_values = new int[categories_count.size()];
				for(int i = 0; i < categories_count.size(); ++i)
					categories_values[i] = categories_count.get(i);

				
				final String[] strokes = new String[strokes_count.size()];
				final int[] strokes_values = new int[strokes.length];
				int i = 0;
				for (Map.Entry<Integer, Integer> e : strokes_count.entrySet())
				{
					strokes[i] = e.getKey() + (e.getKey() == 1 ? " Stroke" : " Strokes");
					strokes_values[i] =  e.getValue();
					i++;
				}

				holder1.button_start_quiz.setVisibility(review[0] > 0 ? View.VISIBLE : View.GONE);

				holder1.text_progress_learned.setText(learned_total + " / " + count);
				holder1.progress_learned.setMax(count);
				holder1.progress_learned.setProgress(learned_total - critical);
				holder1.progress_learned.setSecondaryProgress(learned_total);

				holder1.percentage_graph_types.setValues(strokes, strokes_values, true, true);
				holder1.percentage_graph_categories.setValues(categories, categories_values, true, true);

				holder1.line_graph_review.setValues(review);

				long lastDate = preferences.getLong("lastReviewDateKanji", 0);
				int[] review1 = StringHelper.toIntArray(preferences.getString("reviewKanji1", ""));
				int[] review2 = StringHelper.toIntArray(preferences.getString("reviewKanji2", ""));

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

					for (i = 29; i >= 30 - days; --i)
					{
						review1[i] = 0;
						review2[i] = 0;
					}
				}
				preferences.edit()
					.putLong("lastReviewDateKanji", System.currentTimeMillis())
					.putString("reviewKanji1", StringHelper.toString(review1))
					.putString("reviewKanji2", StringHelper.toString(review2))
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
				FragmentHome.SearchViewHolder holder1 = (FragmentHome.SearchViewHolder) holder;

				holder1.button_stroke_order.setVisibility(JishoHelper.isStrokeOrderAvailable(getContext()) && StringHelper.isKanaOrKanji(searchQuery) ? View.VISIBLE : View.GONE);
				holder1.text_results.setText(kanji.length + (kanji.length == 1 ? " result" : " results") + " for search \"" + searchQuery + "\"");
				holder1.card_stroke_order.setVisibility(View.GONE);
				int[] vocabularies = dbHelper.getVocabularies(SortType.TIME_ADDED, ShowType.ALL, null, false, searchQuery);
				holder1.text_title_kanji_contained.setVisibility(vocabularies.length > 0 ? View.VISIBLE : View.GONE);
				holder1.recycler_kanji_contained.setVisibility(vocabularies.length > 0 ? View.VISIBLE : View.GONE);
				holder1.text_title_kanji_contained.setText(vocabularies.length == 1 ? "1 Vocabulary" : vocabularies.length + " Vocabularies");
				((FragmentDetail.VocabularyAdapter) holder1.adapter_kanji_contained).data = vocabularies;
				holder1.adapter_kanji_contained.notifyDataSetChanged();

				if (holder1.progress_stroke_order.getVisibility() == View.GONE)
				{
					holder1.progress_stroke_order.setVisibility(View.VISIBLE);
					holder1.layout_stroke_order.removeView(holder1.itemView.findViewWithTag("stroke_order"));
				}
			}
			else
			{
				FragmentHome.VocabularyViewHolder holder1 = (FragmentHome.VocabularyViewHolder) holder;

				char id = kanji[position - 1];

				Cursor res = dbHelper.getReadableDatabase().rawQuery("SELECT category, learned, strokes, reading_kun, reading_on, meaning, nextReview FROM kanji WHERE kanji = " + (int) id, null);
				res.moveToFirst();

				boolean learned = res.getInt(res.getColumnIndex("learned")) == 1;
				int category = res.getInt(res.getColumnIndex("category"));
				String[] reading_kun = StringHelper.toStringArray(res.getString(res.getColumnIndex("reading_kun")));
				String[] reading_on = StringHelper.toStringArray(res.getString(res.getColumnIndex("reading_on")));
				String[] meaning = StringHelper.toStringArray(res.getString(res.getColumnIndex("meaning")));
				long nextReview = res.getLong(res.getColumnIndex("nextReview"));
				int strokes = res.getInt(res.getColumnIndex("strokes"));

				holder1.text_category.setVisibility(View.GONE);

				if ((searchQuery == null || searchQuery.isEmpty()))
				{
					if (sortType == SortType.CATEGORY)
					{
						if (position == 1 || dbHelper.getIntKanji(kanji[position - 2], "category") != category)
						{
							holder1.text_category.setVisibility(View.VISIBLE);
							holder1.text_category.setText(category == 0 ? "New kanji" : "Category " + category);
						}
					}
					else if (sortType == SortType.TYPE)
					{
						if (position == 1 || dbHelper.getIntKanji(kanji[position - 2], "strokes") != strokes)
						{
							holder1.text_category.setVisibility(View.VISIBLE);
							holder1.text_category.setText(strokes + (strokes == 1 ? " Stroke" : " Strokes"));
						}
					}
					else if (sortType == SortType.NEXT_REVIEW)
					{
						long nextReview1 = position == 1 ? 0 : dbHelper.getLongKanji(kanji[position - 2], "nextReview");
						if (position == 1 || learned != (dbHelper.getIntKanji(kanji[position - 2], "learned") == 1)
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
					holder1.text_kanji.setText(Kanji.correctAnswer(QuestionType.KANJI, id, reading_kun, reading_on, meaning));
					holder1.text_kanji.setTransitionName("kanji" + id);

					holder1.text_reading.setText(Kanji.correctAnswer(QuestionType.READING_INFO, id, reading_kun, reading_on, meaning));
					holder1.text_reading.setTransitionName("reading" + id);

					holder1.text_meaning.setText(Kanji.correctAnswer(QuestionType.MEANING, id, reading_kun, reading_on, meaning));
					holder1.text_meaning.setTransitionName("meaning" + id);
				}
				else if (hideType == HideType.KANJI)
				{
					holder1.text_kanji.setText(Kanji.correctAnswer(QuestionType.READING_INFO, id, reading_kun, reading_on, meaning));
					holder1.text_kanji.setTransitionName("reading" + id);

					holder1.text_reading.setText("");
					holder1.text_reading.setTransitionName("" + id);

					holder1.text_meaning.setText(Kanji.correctAnswer(QuestionType.MEANING, id, reading_kun, reading_on, meaning));
					holder1.text_meaning.setTransitionName("meaning" + id);
				}
				else if (hideType == HideType.READING)
				{
					holder1.text_kanji.setText(Kanji.correctAnswer(QuestionType.KANJI, id, reading_kun, reading_on, meaning));
					holder1.text_kanji.setTransitionName("kanji" + id);

					holder1.text_reading.setText("");
					holder1.text_reading.setTransitionName("" + id);

					holder1.text_meaning.setText(Kanji.correctAnswer(QuestionType.MEANING, id, reading_kun, reading_on, meaning));
					holder1.text_meaning.setTransitionName("meaning" + id);
				}
				else if (hideType == HideType.MEANING)
				{
					holder1.text_kanji.setText(Kanji.correctAnswer(QuestionType.KANJI, id, reading_kun, reading_on, meaning));
					holder1.text_kanji.setTransitionName("kanji" + id);

					holder1.text_reading.setText("");
					holder1.text_reading.setTransitionName("" + id);

					holder1.text_meaning.setText(Kanji.correctAnswer(QuestionType.READING_INFO, id, reading_kun, reading_on, meaning));
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

				holder1.text_reading.setVisibility(hideType == HideType.NOTHING ? View.VISIBLE : View.GONE);
				holder1.image_check.setVisibility(learned ? View.VISIBLE : View.GONE);
				holder1.image_check.setImageResource(category == 0 ? R.drawable.alert : R.drawable.check);
			}
		}

		@Override
		public int getItemCount() 
		{
			return kanji.length + 1;
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
