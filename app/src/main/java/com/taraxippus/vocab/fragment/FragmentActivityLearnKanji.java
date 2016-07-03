package com.taraxippus.vocab.fragment;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.taraxippus.vocab.ActivitySettings;
import com.taraxippus.vocab.IVocabActivity;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.util.JishoHelper;
import com.taraxippus.vocab.util.StringHelper;

public class FragmentActivityLearnKanji extends Fragment
{
	IVocabActivity vocabActivity;

	public FragmentActivityLearnKanji() {}


    @Override
    public void onAttach(Activity activity)
	{
        super.onAttach(activity);

		try 
		{
            vocabActivity = (IVocabActivity) activity;
        }
		catch (ClassCastException e)
		{
            throw new ClassCastException(activity.toString() + " must implement IVocabActivity!");
        }
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.activity_learn_kanji, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState)
	{
		int id = getArguments().getInt("id");
		final String kanji = vocabActivity.getDBHelper().getString(id, "kanji");
		((TextView) v.findViewById(R.id.text_kanji)).setText(kanji);
		
		if (StringHelper.isKana(kanji))
		{
			v.findViewById(R.id.card_stroke_order).setVisibility(View.GONE);
		}
		else
		{
			final View progress_stroke_order = v.findViewById(R.id.progress_stroke_order);
			final ViewGroup layout_stroke_order = (ViewGroup) v.findViewById(R.id.layout_stroke_order);

			final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
			params.addRule(RelativeLayout.BELOW, R.id.text_title_stroke_order);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

			JishoHelper.addStrokeOrderView(getContext(), kanji, layout_stroke_order, params, progress_stroke_order, false, false);
			
			v.findViewById(R.id.button_overflow).setOnClickListener(new View.OnClickListener()
			{
					@Override
					public void onClick(View view)
					{
						PopupMenu popup = new PopupMenu(getContext(), view);
						popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
							{
								@Override
								public boolean onMenuItemClick(MenuItem item)
								{
									switch (item.getItemId()) 
									{
										case R.id.item_open_jisho_kanji:
											JishoHelper.search(getContext(), kanji + "%23kanji");
											return true;
										case R.id.item_settings:
											getContext().startActivity(new Intent(getContext(), ActivitySettings.class).setAction(ActivitySettings.ACTION_STROKE_ORDER));
											return true;
										default:
											return false;
									}
								}
							});
						MenuInflater inflater = popup.getMenuInflater();
						inflater.inflate(R.menu.item_stroke_order, popup.getMenu());
						popup.show();
					}
			});
		}
	}
}
