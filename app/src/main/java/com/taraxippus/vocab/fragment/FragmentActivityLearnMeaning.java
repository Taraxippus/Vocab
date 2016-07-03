package com.taraxippus.vocab.fragment;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.taraxippus.vocab.IVocabActivity;
import com.taraxippus.vocab.R;
import com.taraxippus.vocab.fragment.FragmentDetail;
import com.taraxippus.vocab.util.StringHelper;

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
		Cursor res =  db.rawQuery("SELECT meaning, sameMeaning, additionalInfo FROM vocab WHERE id = ?", new String[] {"" + id});
		if (res.getCount() <= 0)
		{
			res.close();
			return;
		}

		res.moveToFirst();

		String[] meaning = StringHelper.toStringArray(res.getString(res.getColumnIndex("meaning")));
		int[] sameMeaning = StringHelper.toIntArray(res.getString(res.getColumnIndex("sameMeaning")));
		String additionalInfo = res.getString(res.getColumnIndex("additionalInfo"));
		
		res.close();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < meaning.length; ++i)
		{
			sb.append(meaning[i]);
			if (i < meaning.length - 1)
				sb.append(",\n");
		}

		((TextView) v.findViewById(R.id.text_meaning)).setText(sb.toString());
		
		if (!additionalInfo.isEmpty())
			((TextView) v.findViewById(R.id.text_additional_info)).setText(additionalInfo);
		else
			((TextView) v.findViewById(R.id.text_additional_info)).setVisibility(View.GONE);
			
		if (sameMeaning.length == 0)
		{
			v.findViewById(R.id.text_title_same_meaning).setVisibility(View.GONE);
			v.findViewById(R.id.recycler_same_meaning).setVisibility(View.GONE);
		}

		RecyclerView recyclerView = (RecyclerView)v.findViewById(R.id.recycler_same_meaning);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
		recyclerView.setAdapter(new FragmentDetail.SynonymAdapter(getActivity(), vocabActivity.getDBHelper(), recyclerView, sameMeaning));		
	}
}
