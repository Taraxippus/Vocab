package com.taraxippus.vocab.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.util.StringHelper;
import com.taraxippus.vocab.vocabulary.Vocabulary;
import java.util.ArrayList;

public class DialogFilter extends DialogFragment
{
	ArrayList<CheckBox> boxes_show;
	boolean kanji;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
		alertDialog.setTitle("Filter");
		
		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_filter, null);

		kanji = getArguments() != null && getArguments().getBoolean("kanji", false);
		final String suffix = kanji ? "Kanji" : "";
		
		final Spinner spinner_sort = (Spinner) v.findViewById(R.id.spinner_sort);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, kanji ? Vocabulary.types_sort_kanji : Vocabulary.types_sort);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_sort.setAdapter(adapter);
		spinner_sort.setSelection(preferences.getInt("sortType" + suffix, 0));

		final CheckBox box_sort = (CheckBox) v.findViewById(R.id.box_sort);
		box_sort.setChecked(preferences.getBoolean("sortReversed" + suffix, false));
		
		final Spinner spinner_view = (Spinner) v.findViewById(R.id.spinner_view);
		adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, Vocabulary.types_view);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_view.setAdapter(adapter);
		spinner_view.setSelection(preferences.getInt("viewType" + suffix, 1));

		final Spinner spinner_show = (Spinner) v.findViewById(R.id.spinner_show);
		adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, Vocabulary.types_show);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_show.setAdapter(adapter);
		spinner_show.setSelection(preferences.getInt("showType" + suffix, 0));
		
		final Spinner spinner_hide = (Spinner) v.findViewById(R.id.spinner_hide);
		adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, Vocabulary.types_hide);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_hide.setAdapter(adapter);
		spinner_hide.setSelection(preferences.getInt("hideType" + suffix, 0));
		
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

		if (kanji)
		{
			divider_scroll_bottom.setVisibility(View.GONE);
			divider_scroll_top.setVisibility(View.GONE);
			scroll_show_boxes.setVisibility(View.GONE);
		}
		else
		{
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
		}
		
		alertDialog.setView(v);		
		alertDialog.setPositiveButton("OK",
			new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					dialog.dismiss();

					SharedPreferences.Editor editor = preferences.edit()
						.putInt("sortType" + suffix, spinner_sort.getSelectedItemPosition())
						.putInt("viewType" + suffix, spinner_view.getSelectedItemPosition())
						.putInt("showType" + suffix, spinner_show.getSelectedItemPosition())
						.putInt("hideType" + suffix, spinner_hide.getSelectedItemPosition())
						.putBoolean("sortReversed" + suffix, box_sort.isChecked());
						
					if (!kanji)
					{
						boolean[] show = new boolean[Vocabulary.types.size()];
						for (int i = 0; i < show.length; ++i)
							show[i] = boxes_show.get(i).isChecked();
							
						editor.putString("show", StringHelper.toString(show));
					}
					
					editor.apply();
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
		
		if (!kanji)
		{
			boolean[] show = new boolean[Vocabulary.types.size()];
			for (int i = 0; i < show.length; ++i)
				show[i] = boxes_show.get(i).isChecked();

			outState.putBooleanArray("boxes_show", show);
		}
	}
}
