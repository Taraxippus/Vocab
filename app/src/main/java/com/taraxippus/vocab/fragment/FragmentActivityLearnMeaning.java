package com.taraxippus.vocab.fragment;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.taraxippus.vocab.fragment.FragmentDetail;
import com.taraxippus.vocab.util.JishoHelper;
import com.taraxippus.vocab.util.StringHelper;
import java.util.Locale;

public class FragmentActivityLearnMeaning extends Fragment
{
	IVocabActivity vocabActivity;

	public FragmentActivityLearnMeaning() {}


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
		return inflater.inflate(R.layout.activity_learn_meaning, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState)
	{
		int id = getArguments().getInt("id");
		SQLiteDatabase db = vocabActivity.getDBHelper().getReadableDatabase();
		Cursor res =  db.rawQuery("SELECT kanji, meaning, sameMeaning, additionalInfo FROM vocab WHERE id = ?", new String[] {"" + id});
		if (res.getCount() <= 0)
		{
			res.close();
			return;
		}

		res.moveToFirst();

		final String kanji =  res.getString(0);
		final String[] meaning = StringHelper.toStringArray(res.getString(1));
		int[] sameMeaning = StringHelper.toIntArray(res.getString(2));
		String additionalInfo = res.getString(3);
		
		res.close();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < meaning.length; ++i)
		{
			sb.append(meaning[i]);
			if (i < meaning.length - 1)
				sb.append(",\n");
		}

		((TextView) v.findViewById(R.id.text_meaning)).setText(sb.toString());
		
		TextView text_additional_info = (TextView) v.findViewById(R.id.text_additional_info);
		if (!additionalInfo.isEmpty())
		{
			text_additional_info.setTextLocale(Locale.JAPANESE);
			text_additional_info.setText(additionalInfo);
		}
		else
			text_additional_info.setVisibility(View.GONE);
			
		if (sameMeaning.length == 0)
		{
			v.findViewById(R.id.text_title_same_meaning).setVisibility(View.GONE);
			v.findViewById(R.id.recycler_same_meaning).setVisibility(View.GONE);
		}

		RecyclerView recyclerView = (RecyclerView)v.findViewById(R.id.recycler_same_meaning);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
		recyclerView.setAdapter(new FragmentDetail.SynonymAdapter(getActivity(), vocabActivity.getDBHelper(), recyclerView, sameMeaning));		
		
		
		final View progress_sentences = v.findViewById(R.id.progress_sentences);
		final ViewGroup layout_sentences = (ViewGroup) v.findViewById(R.id.layout_sentences);

		final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.BELOW, R.id.text_title_sentences);

		JishoHelper.addExampleSentences(getContext(), kanji, meaning, layout_sentences, params, progress_sentences);
		
		v.findViewById(R.id.button_overflow_sentences).setOnClickListener(new View.OnClickListener()
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
										case R.id.item_open_jisho_sentences:
											JishoHelper.search(getContext(), kanji + " " + meaning[0] +  " #sentences");
											return true;
										case R.id.item_settings:
											getContext().startActivity(new Intent(getContext(), ActivitySettings.class).setAction(ActivitySettings.ACTION_SENTENCES));
											return true;
										default:
											return false;
									}
								}
							});
						MenuInflater inflater = popup.getMenuInflater();
						inflater.inflate(R.menu.item_sentences, popup.getMenu());
						popup.show();
					}
			});
	}
}
