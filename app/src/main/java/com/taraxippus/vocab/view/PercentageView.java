package com.taraxippus.vocab.view;

import android.animation.*;
import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.view.animation.*;
import com.taraxippus.vocab.*;

public class PercentageView extends View implements ValueAnimator.AnimatorUpdateListener
{
	float size;
	final RectF circle = new RectF();
	final RectF circleSmall = new RectF();
	
	final Paint circlePaint1;
	final Paint circlePaint2;
	final Paint erasePaint;
	final Paint textPaint;

	public float lineWidth = 0.2F;
	public float textWidth = 0.6F;
	public float value = 1;
	
	public int width, height;
	public float textLine;
	
	public ValueAnimator animator;
	
	public PercentageView(Context context)
	{
		this(context, null);
	}

	public PercentageView(Context context, AttributeSet attributeSet)
	{
		super(context, attributeSet);

		setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		circlePaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
		circlePaint1.setStyle(Paint.Style.FILL);
		circlePaint1.setColor(context.getResources().getColor(R.color.accent, context.getTheme()));
		
		circlePaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
		circlePaint2.setStyle(Paint.Style.FILL);
		circlePaint2.setColor(context.getResources().getColor(R.color.accent, context.getTheme()));
		circlePaint2.setAlpha(64);
		
		erasePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		erasePaint.setStyle(Paint.Style.FILL);
		erasePaint.setColor(Color.TRANSPARENT);
		erasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setColor(context.getResources().getColor(R.color.accent, context.getTheme()));
		textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
		textPaint.setTextAlign(Paint.Align.CENTER);
		
		value = 0.0F;
	}

	public void setValue(float value)
	{
		this.value = value;
		
		adjustTextSize("100%");
		invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) 
	{
		super.onSizeChanged(w, h, oldw, oldh);
	
		width = w;
		height = h;
		
		size = Math.min(w, h);
		circle.set(w / 2F - size / 2F, h / 2F - size / 2F, w / 2F + size / 2F, h / 2F + size / 2F);
		circleSmall.set(w / 2F - size / 2F * (1 - lineWidth), h / 2F - size / 2F * (1 - lineWidth), w / 2F + size / 2F * (1 - lineWidth), h / 2F + size / 2F * (1 - lineWidth));
		
		adjustTextSize("" + (int)(value * 100) + "%");
	}
	
	private void adjustTextSize(String text)
	{
		textPaint.setTextSize(100);
		textPaint.setTextScaleX(1.0F);
		
		Rect bounds = new Rect();
		textPaint.getTextBounds(text, 0, text.length(), bounds);
		
		textPaint.setTextSize((height * textWidth / (bounds.right - bounds.left)) * 100);
		
		textPaint.getTextBounds(text, 0, text.length(), bounds);
		textLine = bounds.bottom + (height - bounds.top + bounds.bottom) / 2F;
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		float value = animator == null || !animator.isRunning() ? this.value : this.value * animator.getAnimatedFraction();
		
		canvas.drawArc(circle, -90, value * 360, true, circlePaint1);
		canvas.drawArc(circle, -90 + value * 360, (1 - value) * 360, true, erasePaint);
		canvas.drawArc(circle, -90 + value * 360, (1 - value) * 360, true, circlePaint2);
		canvas.drawArc(circleSmall, -90, 360, true, erasePaint);
		
		canvas.drawText("" + (int)(value * 100) + "%", canvas.getWidth() / 2F, textLine, textPaint);
	}
	
	public void startAnimation(int duration)
	{
		animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(duration);
        animator.addUpdateListener(this);
		animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
	}
	
	@Override
	public void onAnimationUpdate(ValueAnimator a)
	{
		invalidate();
	}
	
    @Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        final int originalWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int originalHeight = MeasureSpec.getSize(heightMeasureSpec);
		
        if (originalWidth > originalHeight)
			super.onMeasure(MeasureSpec.makeMeasureSpec(originalHeight, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(originalHeight, MeasureSpec.EXACTLY));
        else
        	super.onMeasure(MeasureSpec.makeMeasureSpec(originalWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(originalWidth, MeasureSpec.EXACTLY));
    }
}
