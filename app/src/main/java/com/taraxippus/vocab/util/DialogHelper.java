package com.taraxippus.vocab.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public final class DialogHelper
{
	private DialogHelper() {}
	
	public static void createDialog(Context context, String title, String message)
	{
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setPositiveButton("OK",
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.dismiss();
				}
			});
		alertDialog.show();
	}
	
	public static void createDialog(Context context, String title, String message, DialogInterface.OnClickListener onOk)
	{
		createDialog(context, title, message, "OK", onOk);
	}
		
	public static void createDialog(Context context, String title, String message, String ok, DialogInterface.OnClickListener onOk)
	{
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setPositiveButton(ok, onOk);
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.cancel();
				}
		});
		alertDialog.show();
	}

	public static void createDialog(Context context, String title, String message, String neutral, DialogInterface.OnClickListener onNeutral, String ok, DialogInterface.OnClickListener onOk)
	{
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, ok, onOk);
		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, neutral, onNeutral);
		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.cancel();
				}
			});
		alertDialog.show();
	}

	public static void createDialog(Context context, String title, String message, String neutral, DialogInterface.OnClickListener onNeutral, String ok, DialogInterface.OnClickListener onOk, boolean reversed)
	{
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, ok, onOk);
		alertDialog.setButton(reversed ? AlertDialog.BUTTON_NEGATIVE : AlertDialog.BUTTON_NEUTRAL, neutral, onNeutral);
		alertDialog.setButton(reversed ? AlertDialog.BUTTON_NEUTRAL : AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.cancel();
				}
			});
		alertDialog.show();
	}
}
