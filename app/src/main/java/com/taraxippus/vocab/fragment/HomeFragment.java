package com.taraxippus.vocab.fragment;

import android.app.*;
import android.os.*;
import android.support.v4.widget.*;
import android.support.v7.widget.*;
import android.transition.*;
import android.view.*;
import com.taraxippus.vocab.*;

public class HomeFragment extends Fragment
{
	public RecyclerView recyclerView;
	
	public HomeFragment()
	{
		
	}
	
	public void setTransitions(MainActivity main)
	{
		this.setEnterTransition(TransitionInflater.from(main).inflateTransition(android.R.transition.slide_left));
		this.setReturnTransition(TransitionInflater.from(main).inflateTransition(android.R.transition.fade));

		this.setAllowEnterTransitionOverlap(false);
		this.setAllowReturnTransitionOverlap(false);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        View v = inflater.inflate(R.layout.home, container, false);
		
		recyclerView = (RecyclerView) v.findViewById(R.id.vocabulary_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        VocabularyAdapter mAdapter = new VocabularyAdapter((MainActivity)getActivity());
        recyclerView.setAdapter(mAdapter);
		
		final SwipeRefreshLayout swipeContainer = (SwipeRefreshLayout)v.findViewById(R.id.swipeContainer);
		swipeContainer.setOnRefreshListener(new android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener() 
			{
				@Override
				public void onRefresh()
				{
					recyclerView.getAdapter().notifyDataSetChanged();
					((MainActivity) getActivity()).updateNotification();
					swipeContainer.setRefreshing(false);
				} 
			});
        
        swipeContainer.setColorSchemeResources(R.color.accent);
											   
		return v;
	}

	@Override
	public void onResume()
	{
		super.onResume();

		((MainActivity)getActivity()).setTap(this);
	}
}
