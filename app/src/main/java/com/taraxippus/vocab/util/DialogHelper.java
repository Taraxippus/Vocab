package com.taraxippus.vocab.util;

import android.content.*;
import android.support.v7.app.*;
import com.taraxippus.vocab.*;
import android.support.v7.view.*;

public class DialogHelper
{
	final MainActivity context;
	
	public DialogHelper(MainActivity context)
	{
		this.context = context;
	}
	
	public void createDialog(String title, String message)
	{
		AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.DialogTheme).create();
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
}
