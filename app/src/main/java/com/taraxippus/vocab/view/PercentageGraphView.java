package com.taraxippus.vocab.view;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import com.taraxippus.vocab.*;
import java.util.*;
import android.support.v4.view.*;
import android.text.TextPaint;

public class PercentageGraphView extends View
{
	float size;
	final RectF circle = new RectF();
	final RectF circleSmall = new RectF();

	final Paint circlePaint;
	final Paint circlePaintLargest;
	final Paint erasePaint;
	final Paint textPaint;

	public String[] names = new String[] {"No values"};
	public float[] values = new float[] {1};
	public int largest;
	
	public float differenceWidth = 0.1F;
	public int width, height;
	

	public PercentageGraphView(Context context)
	{
		this(context, null);
	}

	public PercentageGraphView(Context context, AttributeSet attributeSet)
	{
		super(context, attributeSet);

		setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		circlePaint.setStyle(Paint.Style.FILL);
		circlePaint.setColor(context.getResources().getColor(R.color.accent, context.getTheme()));

		circlePaintLargest = new Paint(Paint.ANTI_ALIAS_FLAG);
		circlePaintLargest.setStyle(Paint.Style.FILL);
		circlePaintLargest.setColor(context.getResources().getColor(R.color.primary, context.getTheme()));

		erasePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		erasePaint.setStyle(Paint.Style.STROKE);
		erasePaint.setColor(Color.TRANSPARENT);
		erasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

		TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(android.R.attr.textColorSecondary, typedValue, true);
		
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setColor(context.getColor(typedValue.resourceId));
		textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));
		textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
	}

	public void setValues(String[] names1, float[] values1, boolean sort, boolean removeZero)
	{
		values = new float[values1.length];
		names = new String[names1.length];
		largest = 0;

		if (sort)
		{
			String swapString;
			float swapInt;
			for (int n = 0; n < values.length; n++)
				for (int m = 0; m < values.length - 1 - n; m++) 
					if (values1[m] > values1[m + 1]) 
					{
						swapString = names1[m];
						names1[m] = names1[m + 1];
						names1[m + 1] = swapString;
						swapInt = values1[m];
						values1[m] = values1[m + 1];
						values1[m + 1] = swapInt;
					}
		}
		
		float sum = 0;
		
		for (int i = 0; i < values.length; ++i)
			sum += values1[i];
		
		for (int i = 0; i < values.length; ++i)
		{
			names[i] = names1[i];
			values[i] = values1[i] / sum;
			
			if (values[i] > values[largest])
				largest = i;
				
			if (removeZero && values[i] == 0)
				names[i] = "";
			
			else if (i > 0 && values[i - 1] + values[i] < 0.05F)
			{
				if (!names[i - 1].isEmpty())
				{
					names[i] = names[i - 1] + ", " + names[i];
					names[i - 1] = "";
				}
					
				values[i] += values[i - 1];
				values[i - 1] = 0;
			}
		}

		invalidate();
	}

	public void setValues(String[] names1, int[] values1, boolean sort, boolean removeZero)
	{
		values = new float[values1.length];
		names = new String[names1.length];
		largest = 0;
		
		if (sort)
		{
			String swapString;
			int swapInt;
			for (int n = 0; n < values.length; n++)
				for (int m = 0; m < values.length - 1 - n; m++) 
					if (values1[m] > values1[m + 1]) 
					{
						swapString = names1[m];
						names1[m] = names1[m + 1];
						names1[m + 1] = swapString;
						swapInt = values1[m];
						values1[m] = values1[m + 1];
						values1[m + 1] = swapInt;
					}
		}
				
		float sum = 0;
		
		for (int i = 0; i < values.length; ++i)
			sum += values1[i];

		for (int i = 0; i < values.length; ++i)
		{
			names[i] = names1[i];
			values[i] = values1[i] / sum;

			if (values[i] > values[largest])
				largest = i;

			if (removeZero && values[i] == 0)
				names[i] = "";
			
			else if (i > 0 && values[i - 1] + values[i] < 0.05F)
			{
				if (!names[i - 1].isEmpty())
				{
					names[i] = names[i - 1] + ", " + names[i];
					names[i - 1] = "";
				}
					
				values[i] += values[i - 1];
				values[i - 1] = 0;
			}
		}

		invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) 
	{
		super.onSizeChanged(w, h, oldw, oldh);
		
		width = w;
		height = h;
		
		size = Math.min(w, h) * 0.9F;
		circle.set(w / 2F - size / 2F, h / 2F - size / 2F, w / 2F + size / 2F, h / 2F + size / 2F);
		circleSmall.set(w / 2F - size / 2F * (1 - differenceWidth), h / 2F - size / 2F * (1 - differenceWidth), w / 2F + size / 2F * (1 - differenceWidth), h / 2F + size / 2F * (1 - differenceWidth));
		
		erasePaint.setStrokeWidth(size / 2F * 0.1F);
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		float sum = 0;
		for (int i = 0; i < values.length; ++i)
		{
			if (i != largest)
			{
				canvas.drawArc(circle, -90 + sum * 360, values[i] * 360, true, circlePaint);
				canvas.drawArc(circle, -90 + sum * 360, values[i] * 360, true, erasePaint);
			}

			sum += values[i];
		}

		sum = 0;
		float x, y, lastY = Float.NaN;
		for (int i = 0; i < values.length; ++i)
		{
			if (i == largest)
				canvas.drawArc(circleSmall, -90 + sum * 360, values[i] * 360, true, circlePaintLargest);

			x = (float) Math.cos((sum + values[i] / 2F - 0.25F) * Math.PI * 2);
			y = (float) Math.sin((sum + values[i] / 2F - 0.25F) * Math.PI * 2);
			
			textPaint.setTextAlign(y < -0.95 || y >= 0.95 ? Paint.Align.CENTER : x > 0 ? Paint.Align.LEFT : Paint.Align.RIGHT);

			y = y * (size / 2F * 1.075F - textPaint.getTextSize() / 4F) + textPaint.getTextSize() / 4F;
			
			if (x > 0 && lastY == lastY && y - lastY < textPaint.getTextSize() * 1.05F)
			{
				y += (textPaint.getTextSize() * 1.05F - (y - lastY));
				x = (float) Math.cos(Math.asin((y - textPaint.getTextSize() / 4F) / (size / 2F * 1.075F - textPaint.getTextSize() / 4F)));
			}
			
			x *= size / 2F * 1.075F;
				
			if (!names[i].isEmpty())
			{
				canvas.drawText(names[i] + " - " + ((int)(values[i] * 1000) / 10F) + "℅", canvas.getWidth() / 2F + x, canvas.getHeight() / 2F + y, textPaint);
				lastY = y;
			}
				
			sum += values[i];
		}
	}
}
