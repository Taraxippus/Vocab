package com.taraxippus.vocab;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import com.taraxippus.vocab.ActivityMain;
import com.taraxippus.vocab.ActivitySettings;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.fragment.FragmentGrammar;
import com.taraxippus.vocab.fragment.FragmentHome;
import com.taraxippus.vocab.fragment.FragmentQuiz;
import java.util.Locale;
import com.taraxippus.vocab.fragment.FragmentKana;
import com.taraxippus.vocab.fragment.FragmentKanji;

public class ActivityMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
	public static final String ACTION_QUIZ = "com.taraxippus.vocab.action.ACTION_QUIZ";
	
	public Fragment home, grammar, kana, kanji, quiz;
              
    DrawerLayout drawerLayout;     
	NavigationView navigationView;
	ActionBarDrawerToggle drawerToggle;
	View.OnClickListener navigationListener, navigationBackListener;
	
	public ActivityMain() {}
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
		System.out.println("Nyan!");
	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_navigation_drawer);

		Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
		setSupportActionBar(toolbar);
	
		navigationView = (NavigationView) findViewById(R.id.navigation_view);
		navigationView.setNavigationItemSelectedListener(this);  

		drawerLayout = (DrawerLayout) findViewById(R.id.layout_main);  
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
		kana = getFragmentManager().findFragmentByTag("KANA");
		kanji = getFragmentManager().findFragmentByTag("KANJI");
		grammar = getFragmentManager().findFragmentByTag("GRAMMAR");
		quiz = getFragmentManager().findFragmentByTag("QUIZ");
		
		if (home == null)
			home = new FragmentHome().setDefaultTransitions(this);
			
		if (kana == null)
			kana = new FragmentKana().setDefaultTransitions(this);
		
		if (kanji == null)
			kanji = new FragmentKanji().setDefaultTransitions(this);
		
		if (grammar == null)
			grammar = new FragmentGrammar().setDefaultTransitions(this);
		
		if (quiz == null)
			quiz = new FragmentQuiz().setDefaultTransitions(this);
			
		
		getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() 
		{
				public void onBackStackChanged() 
				{
					String tag = getFragmentManager().findFragmentById(R.id.layout_content).getTag();
					
					if (tag == null)
						return;
					
					if (tag.equals("HOME"))
						navigationView.setCheckedItem(R.id.item_home);
						
					else if (tag.equals("KANA"))
						navigationView.setCheckedItem(R.id.item_kana);
						
					else if (tag.equals("KANJI"))
						navigationView.setCheckedItem(R.id.item_kanji);
					
					else if (tag.equals("GRAMMAR"))
						navigationView.setCheckedItem(R.id.item_grammar);
					
					else if (tag.equals("QUIZ"))
						navigationView.setCheckedItem(R.id.item_quiz);
						
				}
		});
			
		if (getIntent() != null && ACTION_QUIZ.equals(getIntent().getAction()))
			getFragmentManager().beginTransaction().replace(R.id.layout_content, quiz, "QUIZ").commit();
		
		else if (savedInstanceState == null)
			getFragmentManager().beginTransaction().replace(R.id.layout_content, home, "HOME").commit();
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
					.replace(R.id.layout_content, home, "HOME")
					.addToBackStack("")
					.commit();
				break;

			case R.id.item_add:
				startActivity(new Intent(ActivityMain.this, ActivityAdd.class));
				break;

			case R.id.item_kana:
				getFragmentManager().beginTransaction()
					.replace(R.id.layout_content, kana, "KANA")
					.addToBackStack("")
					.commit();
				break;
				
			case R.id.item_kanji:
				getFragmentManager().beginTransaction()
					.replace(R.id.layout_content, kanji, "KANJI")
					.addToBackStack("")
					.commit();
				break;
				
			case R.id.item_grammar:
				getFragmentManager().beginTransaction()
					.replace(R.id.layout_content, grammar, "GRAMMAR")
					.addToBackStack("")
					.commit();
				break;
				
			case R.id.item_quiz:
				getFragmentManager().beginTransaction()
					.replace(R.id.layout_content, quiz, "QUIZ")
					.addToBackStack("")
					.commit();
				break;

			case R.id.item_settings:
				startActivity(new Intent(ActivityMain.this, ActivitySettings.class));
				break;
		}

		return true;
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
}
