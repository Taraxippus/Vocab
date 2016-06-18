package com.taraxippus.vocab;

import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.dialog.FilterDialog;
import com.taraxippus.vocab.dialog.ImportDialog;
import com.taraxippus.vocab.dialog.LearnNextDialog;
import com.taraxippus.vocab.fragment.FragmentDetail;
import com.taraxippus.vocab.fragment.FragmentGrammarQuiz;
import com.taraxippus.vocab.fragment.FragmentHome;
import com.taraxippus.vocab.fragment.FragmentQuiz;
import com.taraxippus.vocab.util.AlarmReceiver;
import com.taraxippus.vocab.util.DialogHelper;
import com.taraxippus.vocab.util.JishoHelper;
import com.taraxippus.vocab.util.SaveHelper;
import com.taraxippus.vocab.util.StringHelper;
import com.taraxippus.vocab.util.ViewVocabularyDialog;
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
import android.net.wifi.SupplicantState;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
	public final SaveHelper saveHandler = new SaveHelper(this);
	
	public Fragment home, quiz, quiz_grammar;
              
    DrawerLayout drawerLayout;     
	NavigationView navigationView;
	ActionBarDrawerToggle drawerToggle;
	View.OnClickListener navigationListener, navigationBackListener;
	
	public int vocabulary_selected;
	
	public final ArrayList<Vocabulary> vocabulary = new ArrayList<Vocabulary>();
	public final ArrayList<Vocabulary> vocabulary_filtered = new ArrayList<Vocabulary>();
	public final ArrayList<Vocabulary> vocabulary_learned = new ArrayList<Vocabulary>();
	public final ArrayList<Vocabulary> vocabulary_learned_new = new ArrayList<Vocabulary>();
	
	public static SortType sortType = SortType.CATEGORY;
	public ViewType viewType = ViewType.MEDIUM;
	public ShowType showType = ShowType.ALL;
	public final boolean[] show = new boolean[VocabularyType.values().length];
	public String queryText;
	public final Comparator<Vocabulary> searchComparator = new Comparator<Vocabulary>()
	{
		@Override
		public int compare(Vocabulary v1, Vocabulary v2)
		{
			return (int) Math.signum(v2.searchResult - v1.searchResult);
		}
	};
	
	public MainActivity() {}
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
		System.out.println("Vocab start! Logcat?");
		System.out.println("....");
		System.err.println("=ↀωↀ= ...Nyan!");
		
        super.onCreate(savedInstanceState);
		
        setContentView(R.layout.main_navigation_drawer);
		updateFilter();
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
		setSupportActionBar(toolbar);
	
		navigationView = (NavigationView) findViewById(R.id.navigation_view);
		navigationView.setNavigationItemSelectedListener(this);  

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);  
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name)
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

		navigationListener = drawerToggle.getToolbarNavigationClickListener();
		navigationBackListener = new View.OnClickListener()
		{
			@Override
			public void onClick(View p1)
			{
				onBackPressed();
			}
		};

		home = getFragmentManager().findFragmentByTag("HOME");
		quiz = getFragmentManager().findFragmentByTag("QUIZ");
		quiz_grammar = getFragmentManager().findFragmentByTag("QUIZ_GRAMMAR");
		
		if (home == null)
			home = new FragmentHome().setDefaultTransitions(this);
			
		if (quiz == null)
			quiz = new FragmentQuiz().setDefaultTransitions(this);
			
		if (quiz_grammar == null)
			quiz_grammar = new FragmentGrammarQuiz().setDefaultTransitions(this);
			
		//if (savedInstanceState == null)
		//	getFragmentManager().beginTransaction().replace(R.id.content_frame, home, "HOME").commit();
    }
	
	@Override
	public void onBackPressed()
	{
		if (drawerLayout.isDrawerOpen(navigationView))
			drawerLayout.closeDrawer(navigationView);
		
		else 
			super.onBackPressed();
	}
	
	@Override
	public boolean onNavigationItemSelected(MenuItem menuItem)
	{
		if (menuItem.getItemId() != R.id.item_add && menuItem.getItemId() != R.id.item_settings)
			menuItem.setChecked(true);
		else
			menuItem.setChecked(false);

		drawerLayout.closeDrawers();

		switch (menuItem.getItemId())
		{
			case R.id.item_home:
				getFragmentManager().beginTransaction()
					.replace(R.id.content_frame, home, "HOME")
					.addToBackStack("")
					.commit();
				break;

			case R.id.item_add:
				startActivity(new Intent(MainActivity.this, AddActivity.class));
				break;

			case R.id.item_quiz:
				getFragmentManager().beginTransaction()
					.replace(R.id.content_frame, quiz, "QUIZ")
					.addToBackStack("")
					.commit();
				break;
			case R.id.item_quiz_grammar:
				getFragmentManager().beginTransaction()
					.replace(R.id.content_frame, quiz_grammar, "QUIZ_GRAMMAR")
					.addToBackStack("")
					.commit();
				break;

			case R.id.item_settings:
				startActivity(new Intent(MainActivity.this, SettingsActivity.class));
				break;
		}

		return true;
	}
	
	public void onVocabularyClicked(View v)
	{
		vocabulary_selected = vocabulary.indexOf(vocabulary_filtered.get(((FragmentHome) home).recyclerView.getChildAdapterPosition((LinearLayout) v.getParent()) - 1));
		
		Fragment fragment = new FragmentDetail().setDefaultTransitions(this);
		
		Bundle bundle = new Bundle();
		bundle.putInt("id", vocabulary_selected);//dbHelper.getId(vocabulary.get(vocabulary_selected).kanji));
		fragment.setArguments(bundle);
		
		View v1 = v.findViewById(R.id.text_kanji);
		View v2 = v.findViewById(R.id.text_reading);
		View v3 = v.findViewById(R.id.text_meaning);
		
		getFragmentManager().beginTransaction()
			.replace(R.id.content_frame, fragment)
			.addToBackStack("")
			.addSharedElement(v, v.getTransitionName())
			.addSharedElement(v1, v1.getTransitionName())
			.addSharedElement(v2, v2.getTransitionName())
			.addSharedElement(v3, v3.getTransitionName())
		.commit();
	}
	
	public void showStrokeOrderPopup(final View v)
	{
		PopupMenu popup = new PopupMenu(this, v);
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(MenuItem item)
			{
				switch (item.getItemId()) 
				{
					case R.id.open_jisho_kanji:
						JishoHelper.search(MainActivity.this, vocabulary.get(vocabulary_selected).kanji + "%23kanji");
						return true;
						
					case R.id.settings:
						startActivity(new Intent(MainActivity.this, SettingsActivity.class));
						
						return true;
						
					default:
						return false;
				}
			}
		});
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.stroke_order, popup.getMenu());
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
	
	public void onVocabularyChanged()
	{
		updateFilter();
		updateNotification();
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

		if (nextReview > System.currentTimeMillis())
		{
			AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, AlarmReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);

			alarmManager.set(AlarmManager.RTC_WAKEUP, nextReview, pendingIntent);
		}
	}
	
	public void setDisplayHomeAsUp(boolean homeAsUp)
	{
		if (homeAsUp)
		{
			drawerToggle.setDrawerIndicatorEnabled(false);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			drawerToggle.setToolbarNavigationClickListener(navigationBackListener);
		}
		else
		{
			getSupportActionBar().setDisplayShowHomeEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(false);
			drawerToggle.setDrawerIndicatorEnabled(true);
			drawerToggle.setToolbarNavigationClickListener(navigationListener);
		}
	}

	public void updateFilter()
	{
		String queryText = null;
		
		if (this.queryText != null)
			queryText = StringHelper.trim(this.queryText.toLowerCase());
		
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
		
		//if (home != null && home.recyclerView != null)
			//home.recyclerView.getAdapter().notifyDataSetChanged();
	}
}
