package com.taraxippus.vocab.view;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import com.taraxippus.vocab.*;
import java.util.*;
import android.support.v4.view.*;

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

		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setColor(0);
		textPaint.setAlpha((int) (0.45F * 255));
		textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));
		textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

	}

	public void setValues(String[] names1, float[] values1)
	{
		values = new float[values1.length];
		names = new String[names1.length];

		float sum = 0;
		for (int i = 0; i < values.length; ++i)
		{
			names[i] = names1[i];
			values[i] = values1[i];
			sum += values[i];

			if (values1[i] == 0 && i > 0 && values1[i - 1] == 0)
			{
				names[i] = names[i - 1] + ", " + names[i];
				names[i - 1] = "";
			}
		}

		for (int i = 0; i < values.length; ++i)
		{
			values[i] /= sum;

			if (values[i] > values[largest])
				largest = i;
		}

		invalidate();
	}

	public void setValues(String[] names1, int[] values1)
	{
		values = new float[values1.length];
		names = new String[names1.length];

		float sum = 0;
		for (int i = 0; i < values.length; ++i)
		{
			names[i] = names1[i];
			values[i] = values1[i];
			sum += values[i];

			if (values1[i] == 0 && i > 0 && values1[i - 1] == 0)
			{
				names[i] = names[i - 1] + ", " + names[i];
				names[i - 1] = "";
			}
		}

		for (int i = 0; i < values.length; ++i)
		{
			values[i] /= sum;

			if (values[i] > values[largest])
				largest = i;
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
		for (int i = 0; i < values.length; ++i)
		{
			if (i == largest)
				canvas.drawArc(circleSmall, -90 + sum * 360, values[i] * 360, true, circlePaintLargest);

			textPaint.setTextAlign(Math.cos((sum + values[i] / 2F - 0.25F) * Math.PI * 2) >= 0 ? Paint.Align.LEFT : Paint.Align.RIGHT);

			if (!names[i].isEmpty())
				canvas.drawText(names[i] + " - " + ((int)(values[i] * 1000) / 10F) + "℅", canvas.getWidth() / 2F + (float)Math.cos((sum + values[i] / 2F - 0.25F) * Math.PI * 2) * size / 2F * 1.05F, canvas.getHeight() / 2F + (float)Math.sin((sum + values[i] / 2F - 0.25F) * Math.PI * 2) * (size / 2F * 1.05F - textPaint.getTextSize() / 4F) + textPaint.getTextSize() / 4F, textPaint);

			sum += values[i];
		}
	}
}
