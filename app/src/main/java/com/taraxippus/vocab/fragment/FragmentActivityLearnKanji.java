package com.taraxippus.vocab.fragment;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.taraxippus.vocab.ActivitySettings;
import com.taraxippus.vocab.IVocabActivity;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.util.JishoHelper;
import com.taraxippus.vocab.util.StringHelper;
import java.util.Locale;

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
		TextView text_kanji = (TextView) v.findViewById(R.id.text_kanji);
		text_kanji.setText(kanji);
		text_kanji.setTextLocale(Locale.JAPANESE);
		
		EditText text_kanji_practice = (EditText) v.findViewById(R.id.text_kanji_practice);
		text_kanji_practice.setCursorVisible(false);
		text_kanji_practice.setTextLocale(Locale.JAPANESE);
		
		if (StringHelper.isKana(kanji))
		{
			v.findViewById(R.id.card_stroke_order).setVisibility(View.GONE);
		}
		else
		{
			final View progress_stroke_order = v.findViewById(R.id.progress_stroke_order);
			final ViewGroup layout_stroke_order = (ViewGroup) v.findViewById(R.id.layout_stroke_order);

			final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.BELOW, R.id.text_title_stroke_order);
			
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
											JishoHelper.search(getContext(), kanji + " #kanji");
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
			
			final LinearLayout layout_main = (LinearLayout) v.findViewById(R.id.layout_main);
			final char[] kanji_list = StringHelper.getKanji(kanji);
			TextView text_title;
			RecyclerView recycler;
			float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
			LinearLayout.LayoutParams params_title = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			LinearLayout.LayoutParams params_recycler = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (dp * 100));
			params_recycler.bottomMargin = (int) (8 * dp);
			int[] ids;
			
			for (char c : kanji_list)
				if (c != '々')
				{
					Cursor res = vocabActivity.getDBHelper().getReadableDatabase().rawQuery("SELECT id FROM vocab WHERE id != " + id + " AND kanji LIKE '%" + c + "%' ORDER BY LENGTH(kanji)", null);
					if (res.getCount() <= 0)
					{
						res.close();
						continue;
					}
					
					ids = new int[res.getCount()];
					res.moveToFirst();
					int i = 0;
					do
					{
						ids[i++] = res.getInt(0);
					}
					while (res.moveToNext());
					res.close();
					
					text_title = new TextView(getContext());
					text_title.setText("Vocabularies Containing " + c);
					text_title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
					text_title.setPadding((int) (16 * dp), (int) (8 * dp), 0, (int) (4 * dp));
					
					recycler = new RecyclerView(getContext());
					recycler.setClipToPadding(false);
					recycler.setPadding((int) (8 * dp), 0, (int) (8 * dp), 0);
					recycler.setHasFixedSize(true);
					recycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
					recycler.setAdapter(new FragmentDetail.VocabularyAdapter(getActivity(), vocabActivity.getDBHelper(), recycler, ids));		
					
					layout_main.addView(text_title, params_title);
					layout_main.addView(recycler, params_recycler);
				}
		}
	}
}
