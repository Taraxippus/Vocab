package com.taraxippus.vocab.util;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.NotificationCompat;
import com.taraxippus.vocab.QuizActivity;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.vocabulary.DBHelper;
import com.taraxippus.vocab.vocabulary.Vocabulary;

public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) 
	{
		DBHelper dbHelper = new DBHelper(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		Cursor res =  db.rawQuery("SELECT category, learned, lastChecked FROM vocab", null);
		res.moveToFirst();
		
		int ticker = 0;
		long nextReview = 0;
		boolean vLearned;
		long vNextReview;
		do
		{
			vLearned = res.getInt(res.getColumnIndex("learned")) == 1;
			vNextReview = Vocabulary.getNextReview(res.getInt(res.getColumnIndex("category"))) + res.getLong(res.getColumnIndex("lastChecked"));
			
			if (vLearned && vNextReview > System.currentTimeMillis())
			{
				if (nextReview == 0)
					nextReview = vNextReview;
				else
					nextReview = Math.min(nextReview, vNextReview);
			}
			else if (vLearned)
				ticker++;
				
		}
		while (res.moveToNext());
		
		res.close();
		dbHelper.close();
		
		if (nextReview != 0)
		{
			AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, AlarmReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);

			alarmManager.set(AlarmManager.RTC_WAKEUP, nextReview, pendingIntent);
		}
		
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
     
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		Intent intent_open = new Intent(context, QuizActivity.class);
		intent_open.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		builder.setContentIntent(PendingIntent.getActivity(context, 0, intent_open, PendingIntent.FLAG_CANCEL_CURRENT));
		builder.setSmallIcon(R.drawable.notification);
		builder.setContentTitle("Next review");
		builder.setContentText("Your vocabularies are ready to review");
		builder.setColor(context.getResources().getColor(R.color.primary));
		builder.setAutoCancel(true);
		builder.setNumber(ticker);
		
        notificationManager.notify(R.string.notification_id, builder.build());
    }
}
