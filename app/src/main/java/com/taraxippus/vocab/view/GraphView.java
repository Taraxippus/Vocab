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

	final int[] values = new int[30];
	final int[] values2 = new int[30];
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

		bottomPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		bottomPaint.setStyle(Paint.Style.STROKE);
		bottomPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
		bottomPaint.setColor(0);
		bottomPaint.setAlpha((int) (0.54F * 128));

		Random random = new Random();
		for (int i = 0; i < 30; ++i)
		{
			values[i] = random.nextInt(50);
			values2[i] = random.nextInt(values[i] + 1);

			if (values[i] > values[tallest])
				tallest = i;

			if ((float) values2[i] / values[i] > (float) values2[best] / values[best]
				|| (float) values2[i] / values[i] == (float) values2[best] / values[best] && values2[i] > values2[best])
				best = i;
		}

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
		
		linePaint.setStrokeWidth(w / 64);
		linePaint2.setStrokeWidth(w / 64);
		linePaint_alpha.setStrokeWidth(w / 64);
		linePaint2_alpha.setStrokeWidth(w / 64);
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		for (int i = 0; i < 30; ++i)
		{
			canvas.drawLine(canvas.getWidth() / 31F * (i + 1F), (canvas.getHeight() - bottomPaint.getStrokeWidth() / 2F - textPaint.getTextSize() * 1.5F) * (1F - values[i] / (float)values[tallest]) + textPaint.getTextSize() * 1.5F, canvas.getWidth() / 31F * (i + 1F), canvas.getHeight() - bottomPaint.getStrokeWidth() / 2F, (float) values2[i] / values[i] == (float) values2[best] / values[best] ? linePaint2_alpha : linePaint_alpha);
			canvas.drawLine(canvas.getWidth() / 31F * (i + 1F), (canvas.getHeight() - bottomPaint.getStrokeWidth() / 2F - textPaint.getTextSize() * 1.5F) * (1F - values2[i] / (float)values[tallest]) + textPaint.getTextSize() * 1.5F, canvas.getWidth() / 31F * (i + 1F), canvas.getHeight() - bottomPaint.getStrokeWidth() / 2F,  (float) values2[i] / values[i] == (float) values2[best] / values[best] ? linePaint2 : linePaint);
		}

		canvas.drawText("" + values2[best] + " / " + values[best], canvas.getWidth() / 31F * (best + 1F), (canvas.getHeight() - bottomPaint.getStrokeWidth() / 2F - textPaint.getTextSize() * 1.5F) * (1F - values[best] / (float)values[tallest]) + textPaint.getTextSize() * 1F, textPaint);

		canvas.drawLine(0, canvas.getHeight(), canvas.getWidth(), canvas.getHeight(), bottomPaint);
	}
}
