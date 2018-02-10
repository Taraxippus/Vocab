package com.taraxippus.vocab.util;

import android.content.*;
import android.preference.*;
import android.widget.*;
import com.taraxippus.vocab.vocabulary.*;
import java.util.*;

public class AddQueueReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		//System.out.println("Received broadcast");
		
		String kanji = intent.getStringExtra(Intent.EXTRA_TEXT);
		
		Set<String> queue = new HashSet<>();
		queue = PreferenceManager.getDefaultSharedPreferences(context).getStringSet("addQueue", queue);
		
		DBHelper dbHelper = new DBHelper(context);
		
		if (dbHelper.getId(kanji) != -1 || queue.contains(kanji))
			Toast.makeText(context, kanji + " is already added to vocab!", Toast.LENGTH_SHORT).show();
		
		else
		{
			queue.add(kanji);
			PreferenceManager.getDefaultSharedPreferences(context).edit().putStringSet("addQueue", queue).commit();
		
			Toast.makeText(context, "Added " + kanji, Toast.LENGTH_SHORT).show();
		}
			
		dbHelper.close();
	}
}
