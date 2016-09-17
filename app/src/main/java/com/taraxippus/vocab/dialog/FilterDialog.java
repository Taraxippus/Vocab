package com.taraxippus.vocab.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.vocabulary.ShowType;
import com.taraxippus.vocab.vocabulary.SortType;
import com.taraxippus.vocab.vocabulary.ViewType;
import com.taraxippus.vocab.vocabulary.Vocabulary;
import java.util.ArrayList;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.taraxippus.vocab.util.StringHelper;
import java.util.Arrays;

public class FilterDialog extends DialogFragment
{
	ArrayList<CheckBox> boxes_show;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
		alertDialog.setTitle("Filter");
		
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_filter, null);

		final Spinner spinner_sort = (Spinner) v.findViewById(R.id.spinner_sort);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, Vocabulary.types_sort);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_sort.setAdapter(adapter);
		spinner_sort.setSelection(preferences.getInt("sortType", 0));

		final Spinner spinner_view = (Spinner) v.findViewById(R.id.spinner_view);
		adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, Vocabulary.types_view);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_view.setAdapter(adapter);
		spinner_view.setSelection(preferences.getInt("viewType", 1));

		final Spinner spinner_show = (Spinner) v.findViewById(R.id.spinner_show);
		adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, Vocabulary.types_show);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_show.setAdapter(adapter);
		spinner_show.setSelection(preferences.getInt("showType", 0));
		
		final Spinner spinner_hide = (Spinner) v.findViewById(R.id.spinner_hide);
		adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, Vocabulary.types_hide);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_hide.setAdapter(adapter);
		spinner_hide.setSelection(preferences.getInt("hideType", 0));
		
		boolean[] show = savedInstanceState == null ? StringHelper.toBooleanArray(preferences.getString("show", "")) : savedInstanceState.getBooleanArray("boxes_show");
		
		if (show.length != Vocabulary.types.size())
		{
			show = new boolean[Vocabulary.types.size()];
			for (int i = 0; i < show.length; ++i)
				show[i] = true;
		}
		
		final LinearLayout v1 = (LinearLayout)v.findViewById(R.id.layout_show_boxes);
		boxes_show = new ArrayList<>();
		for (int i = 0; i < Vocabulary.types.size(); ++i)
		{
			CheckBox box = new CheckBox(getContext());
			box.setChecked(show[i]);
			box.setText("Show " + Vocabulary.types.get(i));
			v1.addView(box);

			boxes_show.add(box);
		}

		final ScrollView scroll_show_boxes = (ScrollView) v.findViewById(R.id.scroll_show_boxes);
		final View divider_scroll_top = v.findViewById(R.id.divider_scroll_top);
		final View divider_scroll_bottom = v.findViewById(R.id.divider_scroll_bottom);

		scroll_show_boxes.setOnScrollChangeListener(
			new View.OnScrollChangeListener()
			{
				@Override
				public void onScrollChange(View p1, int p2, int p3, int p4, int p5)
				{
					if (scroll_show_boxes.canScrollVertically(1)) 
						divider_scroll_bottom.setVisibility(View.VISIBLE);
					else
						divider_scroll_bottom.setVisibility(View.INVISIBLE);

					if (scroll_show_boxes.canScrollVertically(-1)) 
						divider_scroll_top.setVisibility(View.VISIBLE);
					else
						divider_scroll_top.setVisibility(View.INVISIBLE);

				}
			});


		divider_scroll_bottom.setVisibility(View.VISIBLE);
		divider_scroll_top.setVisibility(View.INVISIBLE);

		alertDialog.setView(v);		
		alertDialog.setPositiveButton("OK",
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.dismiss();

					boolean[] show = new boolean[Vocabulary.types.size()];
					for (int i = 0; i < show.length; ++i)
						show[i] = boxes_show.get(i).isChecked();
					
					preferences.edit()
						.putInt("sortType", spinner_sort.getSelectedItemPosition())
						.putInt("viewType", spinner_view.getSelectedItemPosition())
						.putInt("showType", spinner_show.getSelectedItemPosition())
						.putInt("hideType", spinner_hide.getSelectedItemPosition())
						.putString("show", StringHelper.toString(show))
					.apply();
				}
			});
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
				@Override
				public void onClick(DialogInterface dialog, int p2)
				{
					dialog.cancel();
				}
		});
		return alertDialog.create();
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		
		boolean[] show = new boolean[Vocabulary.types.size()];
		for (int i = 0; i < show.length; ++i)
			show[i] = boxes_show.get(i).isChecked();
			
		outState.putBooleanArray("boxes_show", show);
	}
}
