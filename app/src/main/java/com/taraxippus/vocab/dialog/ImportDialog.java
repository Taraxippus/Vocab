package com.taraxippus.vocab.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import com.taraxippus.vocab.MainActivity;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.util.DialogHelper;
import com.taraxippus.vocab.vocabulary.ImportType;
import com.taraxippus.vocab.vocabulary.Vocabulary;
import com.taraxippus.vocab.vocabulary.VocabularyType;

public class ImportDialog extends DialogFragment
{
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
		alertDialog.setTitle("Import");
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_import, null);

		final Spinner spinner = (Spinner) v.findViewById(R.id.import_spinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, Vocabulary.types_import);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		final Spinner spinner1 = (Spinner) v.findViewById(R.id.type_spinner);
		adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, Vocabulary.types);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner1.setAdapter(adapter);
		
		final CheckBox learned = (CheckBox)v.findViewById(R.id.learned_checkbox);

		alertDialog.setView(v);		
		alertDialog.setPositiveButton("Import",
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.dismiss();

					ImportType importType = ImportType.values()[spinner.getSelectedItemPosition()];
					VocabularyType type = VocabularyType.values()[spinner1.getSelectedItemPosition()];

					String[] text = getArguments().getString("text").split("\n");
					int size = ((MainActivity) getActivity()).vocabulary.size();

					for (String line : text)
					{
						((MainActivity) getActivity()).saveHandler.importVocabulary(line, importType, type, learned.isChecked());
					}

					((MainActivity) getActivity()).updateFilter();
					((MainActivity) getActivity()).saveHandler.save();
					((MainActivity) getActivity()).updateNotification();

					DialogHelper.createDialog(getActivity(), "Import", "Imported " + (((MainActivity) getActivity()).vocabulary.size() - size) + ((((MainActivity) getActivity()).vocabulary.size() - size) == 1 ? " new vocabulary!" : " new vocabularies!"));
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
}
