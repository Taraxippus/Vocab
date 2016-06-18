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
	public String unit;
	
	public int width, height;

	public float paddingWidth = 0.0125F, maxWidth = 0.975F - paddingWidth * 2, maxHeight = 0.9F;
	
	public LineGraphView(Context context, AttributeSet set)
	{
		super(context, set);
		
		TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(android.R.attr.textColorSecondary, typedValue, true);
		
		borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
		borderPaint.setColor(context.getColor(typedValue.resourceId));
		
		graphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		graphPaint.setStyle(Paint.Style.FILL);
		graphPaint.setColor(context.getColor(R.color.accent));
		graphPaint.setAlpha(64);
		
		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
		linePaint.setColor(context.getColor(R.color.accent));
		
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setColor(context.getColor(R.color.primary));
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
		setValues("", values1);
	}

	public void setValues(String unit, int... values1)
	{
		this.unit = unit;
		
		int length = 0;
		for (int i = 0; i < values1.length; ++i)
			if (values1[i] >= 0)
				length++;
		
		if (length == 0)
			return;
			
		this.values = new int[length];
		tallest = 0;
		
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
		graphPath.moveTo(paddingWidth * width + borderPaint.getStrokeWidth(), (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * values[0] / values[tallest]));
		
		linePath.reset();
		linePath.moveTo(paddingWidth * width, (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * values[0] / values[tallest]));
		linePath.lineTo(paddingWidth * width + borderPaint.getStrokeWidth(), (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * values[0] / values[tallest]));
		
		for (int i = 1; i < values.length; ++i)
		{
			graphPath.lineTo(paddingWidth * width + borderPaint.getStrokeWidth() + width / (float) (values.length - 1) * i * (maxWidth - paddingWidth * 2), (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * values[i] / values[tallest]));
			linePath.lineTo(paddingWidth * width + borderPaint.getStrokeWidth() + width / (float) (values.length - 1) * i * (maxWidth - paddingWidth * 2), (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * values[i] / values[tallest]));
		}
		
		graphPath.lineTo(paddingWidth * width + borderPaint.getStrokeWidth() + width * (maxWidth - paddingWidth * 2), height - borderPaint.getStrokeWidth());
		graphPath.lineTo(paddingWidth * width + borderPaint.getStrokeWidth(), height - borderPaint.getStrokeWidth());
		graphPath.lineTo(paddingWidth * width + borderPaint.getStrokeWidth(), (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * values[0] / values[tallest]));
		
		linePath.lineTo(paddingWidth * width + borderPaint.getStrokeWidth() + width * (1 - paddingWidth * 2) / (values.length - 1F) * (values.length - 0.9F) * maxWidth, (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * (values[values.length - 1] + (values[values.length - 1] - values[values.length - 2]) * 0.1F) / values[tallest]));
		
		invalidate();
	}

	public void setValues(String unit, float... values1)
	{
		this.unit = unit;
		
		int length = 0;
		for (int i = 0; i < values1.length; ++i)
			if (values1[i] >= 0)
				length++;

		if (length == 0)
			return;
				
		this.values = new int[length];

		int i1 = 0;
		for (int i = 0; i < values1.length; i++)
		{
			if (values1[i] >= 0)
			{
				values[i1] = (int) (values1[i] * 100);

				if (values[i1] > values[tallest])
					tallest = i1;

				i1++;
			}
		}

		graphPath.reset();
		graphPath.moveTo(paddingWidth * width + borderPaint.getStrokeWidth(), (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * values[0] / values[tallest]));

		linePath.reset();
		linePath.moveTo(paddingWidth * width, (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * values[0] / values[tallest]));
		linePath.lineTo(paddingWidth * width + borderPaint.getStrokeWidth(), (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * values[0] / values[tallest]));

		for (int i = 1; i < values.length; ++i)
		{
			graphPath.lineTo(paddingWidth * width + borderPaint.getStrokeWidth() + width / (float) (values.length - 1) * i * (maxWidth - paddingWidth * 2), (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * values[i] / values[tallest]));
			linePath.lineTo(paddingWidth * width + borderPaint.getStrokeWidth() + width / (float) (values.length - 1) * i * (maxWidth - paddingWidth * 2), (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * values[i] / values[tallest]));
		}

		graphPath.lineTo(paddingWidth * width + borderPaint.getStrokeWidth() + width * (maxWidth - paddingWidth * 2), height - borderPaint.getStrokeWidth());
		graphPath.lineTo(paddingWidth * width + borderPaint.getStrokeWidth(), height - borderPaint.getStrokeWidth());
		graphPath.lineTo(paddingWidth * width + borderPaint.getStrokeWidth(), (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * values[0] / values[tallest]));

		linePath.lineTo(paddingWidth * width + borderPaint.getStrokeWidth() + width * (1 - paddingWidth * 2) / (values.length - 1F) * (values.length - 0.9F) * maxWidth, (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * (values[values.length - 1] + (values[values.length - 1] - values[values.length - 2]) * 0.1F) / values[tallest]));

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
				canvas.drawCircle(paddingWidth * width + borderPaint.getStrokeWidth() + width * (maxWidth - paddingWidth * 2) / (values.length - 1F) * i, (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * values[i] / values[tallest]), textPaint.getTextSize() / 4F, textPaint);
				canvas.drawText(values[i] + unit, paddingWidth * width + borderPaint.getStrokeWidth() + width * (maxWidth - paddingWidth * 2) / (values.length - 1F) * i, (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * values[i] / values[tallest]) - textPaint.getTextSize() * 0.5F, textPaint);
			}
			
		canvas.drawLine(width * paddingWidth + borderPaint.getStrokeWidth() / 2F, 0, width * paddingWidth + borderPaint.getStrokeWidth() / 2F, height, borderPaint);
		canvas.drawLine(width * paddingWidth + borderPaint.getStrokeWidth(), height - borderPaint.getStrokeWidth() / 2F, width - width * paddingWidth, height - borderPaint.getStrokeWidth() / 2F, borderPaint);
		
		textPaint.setTextAlign(Paint.Align.LEFT);
		canvas.drawCircle(borderPaint.getStrokeWidth() / 2F + width * paddingWidth, (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * values[0] / values[tallest]), textPaint.getTextSize() / 4F, textPaint);
		canvas.drawText(values[0] + unit, textPaint.getTextSize() * 0.25F + borderPaint.getStrokeWidth() / 2F + width * paddingWidth, (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * values[0] / values[tallest]) - textPaint.getTextSize() * 0.5F, textPaint);
		textPaint.setTextAlign(Paint.Align.CENTER);
	}
}
