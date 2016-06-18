package com.taraxippus.vocab.view;

import android.content.*;
import android.graphics.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class FragmentLinearLayout extends LinearLayout
{
	private int[] mInsets = new int[4];

    public FragmentLinearLayout(Context context) 
	{
        super(context);
    }

    public FragmentLinearLayout(Context context, AttributeSet attrs)
	{
        super(context, attrs);
    }

    public FragmentLinearLayout(Context context, AttributeSet attrs, int defStyle) 
	{
        super(context, attrs, defStyle);
    }

    public final int[] getInsets()
	{
        return mInsets;
    }

    @Override
    protected final boolean fitSystemWindows(Rect insets) 
	{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
		{
            mInsets[0] = insets.left;
            mInsets[1] = insets.top;
            mInsets[2] = insets.right;

            insets.left = 0;
            insets.top = 0;
            insets.right = 0;
        }

        return super.fitSystemWindows(insets);
	}
	
	@Override
	public final WindowInsets onApplyWindowInsets(WindowInsets insets)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) 
		{
			mInsets[0] = insets.getSystemWindowInsetLeft();
			mInsets[1] = insets.getSystemWindowInsetTop();
			mInsets[2] = insets.getSystemWindowInsetRight();
			return super.onApplyWindowInsets(insets.replaceSystemWindowInsets(0, 0, 0, insets.getSystemWindowInsetBottom()));
		} 
		else
		{
			return insets;
		}
	}
}
