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
	public final Paint selectedPaint;
	
	public final Path graphPath = new Path();
	public final Path linePath = new Path();
	public final Path selectedPath = new Path();
	
	public int[] values;
	public int tallest, selected = -1;
	public String unit;
	public boolean numbers;
	
	public int width, height;

	public float paddingWidth = 0.0125F, maxWidth = 0.975F - paddingWidth * 2, maxHeight = 0.9F;
	
	public LineGraphView(Context context, AttributeSet set)
	{
		super(context, set);
		
		TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(android.R.attr.textColorSecondary, typedValue, true);
		final float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
		
		borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setStrokeWidth(dp);
		borderPaint.setColor(context.getColor(typedValue.resourceId));
		
		graphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		graphPaint.setStyle(Paint.Style.FILL);
		graphPaint.setColor(context.getColor(R.color.accent));
		graphPaint.setAlpha(64);
		
		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setStrokeWidth(dp * 2);
		linePaint.setColor(context.getColor(R.color.accent));
		
		selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		selectedPaint.setPathEffect(new DashPathEffect(new float[] {dp * 5, dp * 5}, 10 * dp));
		selectedPaint.setStyle(Paint.Style.STROKE);
		selectedPaint.setStrokeWidth(dp);
		selectedPaint.setColor(context.getColor(typedValue.resourceId));
		
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
		setValues("", 0, values1.length, values1);
	}

	public void setValues(String unit, int offset1, int length1, int... values1)
	{
		if (length1 == -1)
			length1 = values1.length - offset1;
		
		if (values1.length < offset1 + length1 || offset1 + length1 <= 1)
			return;
			
		this.numbers = true;
		this.unit = unit;
		
		if (unit.equalsIgnoreCase("#noNumbers"))
		{
			this.unit = "";
			this.numbers = false;
		}
		
		int length = 0;
		for (int i = 0; i < length1; ++i)
			if (values1[offset1 + i] >= 0)
				length++;
		
		if (length == 0)
			return;
			
		this.values = new int[length];
		tallest = 0;
		
		int i1 = 0;
		for (int i = 0; i < length1; i++)
		{
			if (values1[offset1 + i] >= 0)
			{
				values[i1] = values1[offset1 + i];
				
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

	public void setValues(String unit, int offset1, int length1, float... values1)
	{
		if (length1 == -1)
			length1 = values1.length - offset1;
		
		if (values1.length < offset1 + length1 || offset1 + length1 <= 1)
			return;
			
		this.numbers = true;
		this.unit = unit;

		if (unit.equalsIgnoreCase("#noNumbers"))
		{
			this.unit = "";
			this.numbers = false;
		}
		int length = 0;
		for (int i = 0; i < length1; ++i)
			if (values1[offset1 + i] >= 0)
				length++;

		if (length <= 1)
			return;
				
		this.values = new int[length];

		int i1 = 0;
		for (int i = 0; i < length1; i++)
		{
			if (values1[offset1 + i] >= 0)
			{
				values[i1] = (int) (values1[offset1 + i] * 100);

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
		
		if (numbers && selected != -1)
		{
			textPaint.setTextAlign(selected == 0 ? Paint.Align.LEFT : selected == values.length - 1 ? Paint.Align.RIGHT : Paint.Align.CENTER);
			if (selected != 0)
			{
				selectedPath.reset();
				selectedPath.moveTo(paddingWidth * width + borderPaint.getStrokeWidth() + width * (maxWidth - paddingWidth * 2) / (values.length - 1F) * selected, textPaint.getTextSize() * 1.25F);
				selectedPath.lineTo(paddingWidth * width + borderPaint.getStrokeWidth() + width * (maxWidth - paddingWidth * 2) / (values.length - 1F) * selected, height);
				canvas.drawPath(selectedPath, selectedPaint);
			}
				
			canvas.drawText(selected + " - " + values[selected] + unit, paddingWidth * width + borderPaint.getStrokeWidth() + width * (maxWidth - paddingWidth * 2) / (values.length - 1F) * selected, textPaint.getTextSize(), textPaint);
		}
		
		textPaint.setTextAlign(Paint.Align.CENTER);
		
		for (int i = 1; i < values.length; ++i)
			if (selected != -1 || i == values.length - 1 || numbers && values[i] - values[i - 1] != 0 && (values[i] - values[i - 1]) != (values[i + 1] - values[i]))
			{
				canvas.drawCircle(paddingWidth * width + borderPaint.getStrokeWidth() + width * (maxWidth - paddingWidth * 2) / (values.length - 1F) * i, (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * values[i] / values[tallest]), textPaint.getTextSize() / 4F, textPaint);
				
				if (selected == -1 && numbers && (i == values.length - 1 || values[i + 1] - values[i] < values[i] - values[i - 1]))
					canvas.drawText(values[i] + unit, paddingWidth * width + borderPaint.getStrokeWidth() + width * (maxWidth - paddingWidth * 2) / (values.length - 1F) * i, (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * values[i] / values[tallest]) - textPaint.getTextSize() * 0.5F, textPaint);
			}
			
		canvas.drawLine(width * paddingWidth + borderPaint.getStrokeWidth() / 2F, 0, width * paddingWidth + borderPaint.getStrokeWidth() / 2F, height, borderPaint);
		canvas.drawLine(width * paddingWidth + borderPaint.getStrokeWidth(), height - borderPaint.getStrokeWidth() / 2F, width - width * paddingWidth, height - borderPaint.getStrokeWidth() / 2F, borderPaint);
		
		canvas.drawCircle(borderPaint.getStrokeWidth() / 2F + width * paddingWidth, (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * values[0] / values[tallest]), textPaint.getTextSize() / 4F, textPaint);
		if (selected == -1 && numbers && values[1] - values[0] < 1)
		{
			textPaint.setTextAlign(Paint.Align.LEFT);
			canvas.drawText(values[0] + unit, textPaint.getTextSize() * 0.25F + borderPaint.getStrokeWidth() / 2F + width * paddingWidth, (height - borderPaint.getStrokeWidth()) * (1 - maxHeight * values[0] / values[tallest]) - textPaint.getTextSize() * 0.5F, textPaint);
		}
	}
	
	int index = -1;

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				index = event.getActionIndex();
			case MotionEvent.ACTION_MOVE:
				if (index != event.getActionIndex())
					return super.onTouchEvent(event);

				int selected1 = selected;
				selected = Math.max(0, Math.min(values.length - 1, Math.round((event.getX() - paddingWidth * width + borderPaint.getStrokeWidth()) / (width * (maxWidth - paddingWidth * 2) / (values.length - 1F)))));
				if (selected1 != selected)
					invalidate();
				return true;

			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				if (index != event.getActionIndex())
					return super.onTouchEvent(event);

				index = -1;
				selected = -1;
				invalidate();
				return true;

			default:
				return super.onTouchEvent(event);
		}
	}
}
