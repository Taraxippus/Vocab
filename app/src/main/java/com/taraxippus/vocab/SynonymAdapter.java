package com.taraxippus.vocab;

import android.app.*;
import android.support.v7.widget.*;
import android.transition.*;
import android.view.*;
import android.widget.*;
import com.taraxippus.vocab.fragment.*;
import com.taraxippus.vocab.vocabulary.*;

public class SynonymAdapter extends RecyclerView.Adapter<SynonymAdapter.ViewHolder> implements View.OnClickListener
{
	@Override
	public void onClick(View v)
	{
		int position = view.getChildAdapterPosition(v);
		Vocabulary vocab = reading ? main.vocabulary.get(main.vocabulary_selected).sameReading.get(position) : main.vocabulary.get(main.vocabulary_selected).sameMeaning.get(position);
		
		main.selectedVocabulary_backStack.add(main.vocabulary_selected);
		main.vocabulary_selected = main.vocabulary.indexOf(vocab);
		
		Fragment fragment = new VocabularyFragment();
		
		fragment.setSharedElementEnterTransition(TransitionInflater.from(main).inflateTransition(R.transition.change_image_transform));
		fragment.setSharedElementReturnTransition(TransitionInflater.from(main).inflateTransition(R.transition.change_image_transform));
		
		fragment.setEnterTransition(TransitionInflater.from(main).inflateTransition(android.R.transition.slide_top));
		fragment.setReturnTransition(TransitionInflater.from(main).inflateTransition(android.R.transition.fade));
		
		position = main.vocabulary_filtered.indexOf(vocab);
		
		v.setTransitionName("card" + position);
		
		View v1 = v.findViewById(R.id.kanji_text);
		v1.setTransitionName("kanji" + position);
		
		FragmentTransaction ft = main.getFragmentManager().beginTransaction()
			.replace(R.id.content_frame, fragment)
			.addToBackStack("detail")
			.addSharedElement(v, v.getTransitionName())
			.addSharedElement(v1, v1.getTransitionName());
		ft.commit();
	}

    public static class ViewHolder extends RecyclerView.ViewHolder 
	{
        public ViewHolder(View v) 
		{
            super(v);
        }
    }

	final MainActivity main;
	final RecyclerView view;
	final boolean reading;

    public SynonymAdapter(MainActivity main, RecyclerView view, boolean reading)
	{
		this.main = main;
		this.view = view;
		this.reading = reading;
    }

    @Override
    public SynonymAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.synonym_item, parent, false);
		
		v.setOnClickListener(this);
		
		ViewHolder vh = new ViewHolder(v);
		return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) 
	{
		Vocabulary v = reading ? main.vocabulary.get(main.vocabulary_selected).sameReading.get(position) : main.vocabulary.get(main.vocabulary_selected).sameMeaning.get(position);
		
		int pos = main.vocabulary_filtered.indexOf(v);
		
		holder.itemView.setTransitionName("card" + pos);
		TextView v1 = (TextView)holder.itemView.findViewById(R.id.kanji_text);
		v1.setTransitionName("kanji" + pos);
		v1.setText(v.kanji);
	}

    @Override
    public int getItemCount() 
	{
        return reading ? main.vocabulary.get(main.vocabulary_selected).sameReading.size() : main.vocabulary.get(main.vocabulary_selected).sameMeaning.size() ;
    }
}
