package com.taraxippus.vocab.util;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import com.taraxippus.vocab.ActivityQuiz;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.vocabulary.DBHelper;
import com.taraxippus.vocab.vocabulary.Vocabulary;

public class NotificationHelper extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) 
	{
		if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notification", true))
			return;
			
		DBHelper dbHelper = new DBHelper(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		Cursor res =  db.rawQuery("SELECT nextReview, lastChecked FROM vocab WHERE learned = 1", null);
		
		if (res.getCount() <= 0)
		{
			res.close();
			dbHelper.close();
			
			((NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE)).cancel(R.string.notification_id);
			return;
		}
		
		res.moveToFirst();
		
		int ticker = 0;
		int newVocab = 0;
		long nextReview = 0;
		long vNextReview;
		do
		{
			vNextReview = res.getLong(0);
			
			if (vNextReview > System.currentTimeMillis())
			{
				if (nextReview == 0)
					nextReview = vNextReview;
				else
					nextReview = Math.min(nextReview, vNextReview);
			}
			else
			{
				ticker++;
				
				if (res.getLong(1) == 0)
					newVocab++;
			}
		}
		while (res.moveToNext());
		
		res.close();
		dbHelper.close();
		
		if (nextReview != 0)
		{
			AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, NotificationHelper.class), PendingIntent.FLAG_CANCEL_CURRENT);

			alarmManager.set(AlarmManager.RTC_WAKEUP, nextReview, pendingIntent);
		}
		
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
		
		if (ticker > 0)
		{
			NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
			Intent intent_open = new Intent(context, ActivityQuiz.class);
			intent_open.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			builder.setContentIntent(PendingIntent.getActivity(context, 0, intent_open, PendingIntent.FLAG_CANCEL_CURRENT));
			builder.setSmallIcon(R.drawable.notification);
			builder.setContentTitle("Next Review");
			builder.setContentText(ticker == 1 ? "1 Vocabulary" : ticker + " Vocabularies" + (newVocab == 0 ? "" : " ("  + newVocab + " New)"));
			builder.setColor(context.getResources().getColor(R.color.primary));
			builder.setNumber(ticker);

			notificationManager.notify(R.string.notification_id, builder.build());
		}
		else
			notificationManager.cancel(R.string.notification_id);
    }
}
