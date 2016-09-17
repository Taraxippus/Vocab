package com.taraxippus.vocab.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.dialog.DialogHelper;
import com.taraxippus.vocab.util.NotificationHelper;
import com.taraxippus.vocab.util.OnProcessSuccessListener;
import com.taraxippus.vocab.vocabulary.DBHelper;
import com.taraxippus.vocab.vocabulary.ImportType;
import com.taraxippus.vocab.vocabulary.Vocabulary;
import com.taraxippus.vocab.vocabulary.VocabularyType;

public class ImportDialog extends DialogFragment
{
	DBHelper dbHelper;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		dbHelper = new DBHelper(getContext());
		
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
		alertDialog.setTitle("Import");
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_import, null);

		final Spinner spinner_import = (Spinner) v.findViewById(R.id.spinner_import);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, Vocabulary.types_import);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_import.setAdapter(adapter);
		spinner_import.setSelection(ImportType.ASK.ordinal());
		
		final Spinner spinner_type = (Spinner) v.findViewById(R.id.spinner_type);
		adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, Vocabulary.types);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_type.setAdapter(adapter);
		
		final CheckBox checkbox_learned = (CheckBox)v.findViewById(R.id.checkbox_learned);

		alertDialog.setView(v);		
		alertDialog.setPositiveButton("Import",
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					ImportType importType = ImportType.values()[spinner_import.getSelectedItemPosition()];
					VocabularyType type = VocabularyType.values()[spinner_type.getSelectedItemPosition()];

					final String[] text = getArguments().getString("text").split("\n");
					
					dbHelper.importVocabulary(text[0], importType, type, checkbox_learned.isChecked(), new OnVocabularyImported(dbHelper, text, 0, 0, importType, type, checkbox_learned.isChecked()));
				}
			});
		alertDialog.setNegativeButton("Cancel",
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.dismiss();
				}
			});
		return alertDialog.create();
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();

		dbHelper.close();
	}
	
	public static class OnVocabularyImported implements OnProcessSuccessListener
	{
		public final DBHelper dbHelper;
		public final String[] text;
		public final int index;
		public int count;
		public final ImportType importType;
		public final VocabularyType type;
		public final boolean learned;
		
		public OnVocabularyImported(DBHelper dbHelper, String[] text, int index, int count, ImportType importType, VocabularyType type, boolean learned)
		{
			this.dbHelper = dbHelper;
			this.text = text;
			this.index = index;
			this.count = count;
			this.importType = importType;
			this.type = type;
			this.learned = learned;
		}

		@Override
		public void onProcessSuccess(Object... args)
		{
			if ((Boolean) args[0])
				count++;
				
			if (index >= text.length - 1)
			{
				PreferenceManager.getDefaultSharedPreferences(dbHelper.context).edit().putLong("vocabulariesChanged", System.currentTimeMillis()).apply();
				dbHelper.context.sendBroadcast(new Intent(dbHelper.context, NotificationHelper.class));
				
				dbHelper.close();
				DialogHelper.createDialog(dbHelper.context, "Import", "Imported " + count + (count == 1 ? " new vocabulary!" : " new vocabularies!"));
			}
			else
				dbHelper.importVocabulary(text[index], importType, type, learned, new OnVocabularyImported(dbHelper, text, index + 1, count, importType, type, learned));
				
		}
	}
}
