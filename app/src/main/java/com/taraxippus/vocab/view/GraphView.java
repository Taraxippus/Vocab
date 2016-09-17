package com.taraxippus.vocab.view;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import com.taraxippus.vocab.*;
import java.util.*;

public class GraphView extends View
{
	final Paint linePaint;
	final Paint linePaint2;
	final Paint linePaint_alpha;
	final Paint linePaint2_alpha;
	final Paint bottomPaint;
	final Paint textPaint;

	int[] values;
	int[] values2;
	int tallest;
	int best;

	public int width, height;
	
	public GraphView(Context context, AttributeSet set)
	{
		super(context, set);

		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setColor(context.getResources().getColor(R.color.accent, context.getTheme()));
		linePaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint2.setStyle(Paint.Style.STROKE);
		linePaint2.setColor(context.getResources().getColor(R.color.primary, context.getTheme()));

		linePaint_alpha = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint_alpha.setStyle(Paint.Style.STROKE);
		linePaint_alpha.setColor(context.getResources().getColor(R.color.accent, context.getTheme()));
		linePaint_alpha.setAlpha(64);
		linePaint2_alpha = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint2_alpha.setStyle(Paint.Style.STROKE);
		linePaint2_alpha.setColor(context.getResources().getColor(R.color.primary, context.getTheme()));
		linePaint2_alpha.setAlpha(64);

		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setColor(context.getResources().getColor(R.color.primary, context.getTheme()));
		textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
		textPaint.setTextAlign(Paint.Align.CENTER);

		TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(android.R.attr.textColorSecondary, typedValue, true);
		
		bottomPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		bottomPaint.setStyle(Paint.Style.STROKE);
		bottomPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
		bottomPaint.setColor(context.getColor(typedValue.resourceId));
		
		setValues(new int[] {2}, new int[] {1});
	}
	
	public void setValues(int[] values1, int[] values12)
	{
		if (values1.length == 0 || values1.length != values12.length)
			return;
			
		values = new int[values1.length];
		values2 = new int[values12.length];
		tallest = 0;
		
		for (int i = 0; i < values1.length; ++i)
		{
			values[i] = values1[i];
			values2[i] = values12[i];

			if (values[i] > values[tallest])
				best = tallest = i;

//			if ((float) values2[i] / values[i] > (float) values2[best] / values[best]
//				|| (float) values2[i] / values[i] == (float) values2[best] / values[best] && values2[i] > values2[best])
//				best = i;
		}
		
		textPaint.setTextAlign(best == 0 ? Paint.Align.LEFT : best == values.length - 1 ? Paint.Align.RIGHT : Paint.Align.CENTER);
		
		onSizeChanged(getWidth(), getHeight(), getWidth(), getHeight());
		invalidate();
	}

	public GraphView(Context context)
	{
		this(context, null);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) 
	{
		super.onSizeChanged(w, h, oldw, oldh);

		width = w;
		height = h;
		
		linePaint.setStrokeWidth(w / (values.length + 1F) / 2F);
		linePaint2.setStrokeWidth(w / (values.length + 1F) / 2F);
		linePaint_alpha.setStrokeWidth(w / (values.length + 1F) / 2F);
		linePaint2_alpha.setStrokeWidth(w / (values.length + 1F) / 2F);
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		for (int i = 0; i < values.length; ++i)
		{
			canvas.drawLine(canvas.getWidth() / (values.length + 1F) * (i + 1F), (canvas.getHeight() - bottomPaint.getStrokeWidth() - textPaint.getTextSize() * 1.5F) * (1F - values[i] / (float)values[tallest]) + textPaint.getTextSize() * 1.5F, canvas.getWidth() / (values.length + 1F) * (i + 1F), canvas.getHeight() - bottomPaint.getStrokeWidth(), values[i] == values[best] ? linePaint2_alpha : linePaint_alpha);
			canvas.drawLine(canvas.getWidth() / (values.length + 1F) * (i + 1F), (canvas.getHeight() - bottomPaint.getStrokeWidth() - textPaint.getTextSize() * 1.5F) * (1F - values2[i] / (float)values[tallest]) + textPaint.getTextSize() * 1.5F, canvas.getWidth() / (values.length + 1F) * (i + 1F), canvas.getHeight() - bottomPaint.getStrokeWidth(), values[i] == values[best] ? linePaint2 : linePaint);
		}

		canvas.drawText("" + values2[best] + " / " + values[best], canvas.getWidth() / (values.length + 1F) * (best + 1F), (canvas.getHeight() - bottomPaint.getStrokeWidth() - textPaint.getTextSize() * 1.5F) * (1F - values[best] / (float)values[tallest]) + textPaint.getTextSize() * 1F, textPaint);

		canvas.drawLine(0, canvas.getHeight() - bottomPaint.getStrokeWidth() / 2F, canvas.getWidth(), canvas.getHeight() - bottomPaint.getStrokeWidth() / 2F, bottomPaint);
	}
}
