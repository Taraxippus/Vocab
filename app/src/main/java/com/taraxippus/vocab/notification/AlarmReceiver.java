package com.taraxippus.vocab.notification;

import android.app.*;
import android.content.*;
import android.support.v7.app.*;
import com.taraxippus.vocab.*;

public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) 
	{
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
     
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		Intent intent_open = new Intent(context, MainActivity.class);
		intent_open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent_open.putExtra("review", true);
		builder.setContentIntent(PendingIntent.getActivity(context, 1, intent_open, PendingIntent.FLAG_CANCEL_CURRENT));
		builder.setSmallIcon(R.drawable.quiz);
		builder.setContentTitle("Next review");
		builder.setContentText("Your vocabularies are ready to review");
		builder.setColor(0xffF44336);
		
        notificationManager.notify(R.string.notification_id, builder.build());
    }
}
