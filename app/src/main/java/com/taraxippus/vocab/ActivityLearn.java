package com.taraxippus.vocab;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import com.taraxippus.vocab.fragment.FragmentActivityLearn;
import com.taraxippus.vocab.vocabulary.DBHelper;

public class ActivityLearn extends AppCompatActivity implements IVocabActivity
{
	DBHelper dbHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		dbHelper = new DBHelper(this);
		final int[] newVocabularies = dbHelper.getNewVocabularies();
		
		if (newVocabularies.length == 0)
		{
			finish();
			return;
		}
		
		final Fragment fragment = new FragmentActivityLearn().setDefaultTransitions(this);
		final Bundle args = new Bundle();
		args.putIntArray("newVocabularies", newVocabularies);
		args.putInt("index", 0);
		fragment.setArguments(args);
		
		getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
	}

	@Override
	public DBHelper getDBHelper()
	{
		return dbHelper;
	}
	
	@Override
    public void onBackPressed()
	{
 		super.onBackPressed();
    }

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		dbHelper.close();
	}
}
