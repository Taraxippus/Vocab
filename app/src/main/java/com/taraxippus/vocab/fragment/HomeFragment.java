package com.taraxippus.vocab.fragment;

import android.app.*;
import android.os.*;
import android.support.v4.widget.*;
import android.support.v7.widget.*;
import android.view.*;
import com.taraxippus.vocab.*;

public class HomeFragment extends Fragment
{
	public RecyclerView recyclerView;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        View v = inflater.inflate(R.layout.home, container, false);
		
		recyclerView = (RecyclerView) v.findViewById(R.id.vocabulary_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        VocabularyAdapter mAdapter = new VocabularyAdapter((MainActivity)getActivity(), false);
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
        
        swipeContainer.setColorSchemeColors(0xFFFFC107);
											   
		return v;
	}

	@Override
	public void onResume()
	{
		super.onResume();

		((MainActivity)getActivity()).setTap(this);
	}
}
