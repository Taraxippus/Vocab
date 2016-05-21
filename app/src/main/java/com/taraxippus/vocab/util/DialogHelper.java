package com.taraxippus.vocab.util;

import android.app.AlertDialog;
import android.content.DialogInterface;
import com.taraxippus.vocab.MainActivity;
import com.taraxippus.vocab.R;

public class DialogHelper
{
	final MainActivity context;
	
	public DialogHelper(MainActivity context)
	{
		this.context = context;
	}
	
	public void createDialog(String title, String message)
	{
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setOwnerActivity(context);
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.dismiss();
				}
			});
		alertDialog.show();
	}
	
	public void createDialog(String title, String message, DialogInterface.OnClickListener onOk)
	{
		createDialog(title, message, "OK", onOk);
	}
		
	public void createDialog(String title, String message, String ok, DialogInterface.OnClickListener onOk)
	{
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setOwnerActivity(context);
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, ok, onOk);
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

	public void createDialog(String title, String message, String neutral, DialogInterface.OnClickListener onNeutral, String ok, DialogInterface.OnClickListener onOk)
	{
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setOwnerActivity(context);
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
	

	public void createDialog(String title, String message, String neutral, DialogInterface.OnClickListener onNeutral, String ok, DialogInterface.OnClickListener onOk, boolean reversed)
	{
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setOwnerActivity(context);
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
