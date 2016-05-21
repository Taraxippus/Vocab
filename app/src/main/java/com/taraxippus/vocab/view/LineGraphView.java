package com.taraxippus.vocab.view;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import com.taraxippus.vocab.*;

public class LineGraphView extends View
{
	public final Paint borderPaint;
	public final Paint graphPaint;
	public final Paint linePaint;
	public final Paint textPaint;
	
	public final Path graphPath = new Path();
	public final Path linePath = new Path();
	
	public int[] values;
	public int tallest;
	
	public int width, height;

	public float maxWidth = 0.95F, maxHeight = 0.9F;
	
	public LineGraphView(Context context, AttributeSet set)
	{
		super(context, set);
		
		borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
		borderPaint.setColor(0);
		borderPaint.setAlpha((int) (0.54F * 128));
		
		graphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		graphPaint.setStyle(Paint.Style.FILL);
		graphPaint.setColor(context.getResources().getColor(R.color.accent, context.getTheme()));
		graphPaint.setAlpha(64);
		
		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
		linePaint.setColor(context.getResources().getColor(R.color.accent, context.getTheme()));
		
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setColor(context.getResources().getColor(R.color.primary, context.getTheme()));
		textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
		textPaint.setTextAlign(Paint.Align.CENTER);
		
		setValues(1, 1);
	}

	public LineGraphView(Context context)
	{
		this(context, null);
	}

	public void setValues(int... values1)
	{
		int length = 0;
		for (int i = 0; i < values1.length; ++i)
			if (values1[i] >= 0)
				length++;
		
		this.values = new int[length];
		
		int i1 = 0;
		for (int i = 0; i < values1.length; i++)
		{
			if (values1[i] >= 0)
			{
				values[i1] = values1[i];
				
				if (values[i1] > values[tallest])
					tallest = i1;
					
				i1++;
			}
		}
			
		graphPath.reset();
		graphPath.moveTo(borderPaint.getStrokeWidth() / 2F, (height - borderPaint.getStrokeWidth() / 2F) * (1 - maxHeight * values[0] / values[tallest]));
		
		linePath.reset();
		linePath.moveTo(0, (height - borderPaint.getStrokeWidth() / 2F) * (1 - maxHeight * values[0] / values[tallest]));
		linePath.lineTo(borderPaint.getStrokeWidth() / 2F, (height - borderPaint.getStrokeWidth() / 2F) * (1 - maxHeight * values[0] / values[tallest]));
		
		for (int i = 1; i < values.length; ++i)
		{
			graphPath.lineTo(borderPaint.getStrokeWidth() / 2F + width / (float) (values.length - 1) * i * maxWidth, (height - borderPaint.getStrokeWidth() / 2F) * (1 - maxHeight * values[i] / values[tallest]));
			linePath.lineTo(borderPaint.getStrokeWidth() / 2F + width / (float) (values.length - 1) * i * maxWidth, (height - borderPaint.getStrokeWidth() / 2F) * (1 - maxHeight * values[i] / values[tallest]));
		}
		
		graphPath.lineTo(borderPaint.getStrokeWidth() / 2F + width * maxWidth, height - borderPaint.getStrokeWidth() / 2F);
		graphPath.lineTo(borderPaint.getStrokeWidth() / 2F, height - borderPaint.getStrokeWidth() / 2F);
		graphPath.lineTo(borderPaint.getStrokeWidth() / 2F, (height - borderPaint.getStrokeWidth() / 2F) * (1 - maxHeight * values[0] / values[tallest]));
		
		linePath.lineTo(borderPaint.getStrokeWidth() / 2F + width / (values.length - 1F) * (values.length - 0.9F) * maxWidth, (height - borderPaint.getStrokeWidth() / 2F) * (1 - maxHeight * (values[values.length - 1] + (values[values.length - 1] - values[values.length - 2]) * 0.1F) / values[tallest]));
		
		invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) 
	{
		super.onSizeChanged(w, h, oldw, oldh);

		width = w;
		height = h;
		
		setValues(values);
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		canvas.drawPath(graphPath, graphPaint);
		canvas.drawPath(linePath, linePaint);
		
		for (int i = 1; i < values.length; ++i)
			if (i == values.length - 1 || values[i] - values[i - 1] != 0 && (values[i] - values[i - 1]) != (values[i + 1] - values[i]))
			{
				canvas.drawCircle( borderPaint.getStrokeWidth() / 2F + width * maxWidth / (values.length - 1F) * i, (height - borderPaint.getStrokeWidth() / 2F) * (1 - maxHeight * values[i] / values[tallest]), textPaint.getTextSize() / 4F, textPaint);
				canvas.drawText("" + values[i], borderPaint.getStrokeWidth() / 2F + width * maxWidth / (values.length - 1F) * i, (height - borderPaint.getStrokeWidth() / 2F) * (1 - maxHeight * values[i] / values[tallest]) - textPaint.getTextSize() * 0.5F, textPaint);
				
			}
			
		canvas.drawLine(0, 0, 0, height, borderPaint);
		canvas.drawLine(borderPaint.getStrokeWidth() / 2F, height, width, height, borderPaint);
	}
}
