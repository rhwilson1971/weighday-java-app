package net.cynreub.weighday.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SegmentedProgressView extends View {

    private final Paint paint;
    private final RectF rectF;
    private float progress = 0f; // 0.0 to 1.0
    private final float strokeWidth = 50f;
    private final int[] segmentColors = {
            Color.RED, Color.RED,
            0xFFFFA500, 0xFFFFA500, // Orange
            Color.YELLOW, Color.YELLOW,
            Color.GREEN, Color.GREEN
    };

    public SegmentedProgressView(Context context) {
        super(context);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectF = new RectF();
        init();
    }

    public SegmentedProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectF = new RectF();
        init();
    }

    private void init() {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Simple square measurement logic
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float halfStroke = strokeWidth / 2f;
        rectF.set(halfStroke, halfStroke, getWidth() - halfStroke, getHeight() - halfStroke);

        int totalSegments = 8;
        float gap = 6f;
        float degreesPerSegment = 360f / totalSegments;
        float fillDegrees = progress * 360f;

        // Shadow Paint
        paint.setColor(Color.LTGRAY);
        paint.setAlpha(50); // Low alpha for shadow

        // Draw Shadow Background
        for (int i = 0; i < totalSegments; i++) {
            float startAngle = -90f + (i * degreesPerSegment);
            canvas.drawArc(rectF, startAngle + (gap / 2), degreesPerSegment - gap, false, paint);
        }

        // Reset Alpha for active segments
        paint.setAlpha(255);

        // Draw Active Progress
        for (int i = 0; i < totalSegments; i++) {
            float startAngle = -90f + (i * degreesPerSegment);
            float segmentPassed = i * degreesPerSegment;
            
            // Determine how much of this segment is filled
            // fillDegrees is total progress in degrees.
            // If fillDegrees > segmentPassed, we have some fill here.
            float remainingDegrees = Math.max(0, Math.min(degreesPerSegment, fillDegrees - segmentPassed));

            if (remainingDegrees > 0) {
                float sweep = remainingDegrees;
                // If we are filling the whole segment, subtract gap.
                // If partial, strictly we should check if it covers the gap, but simpler is just to draw what we have.
                // Logic from Compose: if (remainingDegrees == degreesPerSegment) remainingDegrees - gap else remainingDegrees
                
                if (remainingDegrees >= degreesPerSegment) {
                    sweep = degreesPerSegment - gap;
                } else if (remainingDegrees > gap / 2) {
                     // small adjustment if partially filled to look right? 
                     // actually standard arc is fine, but let's stick to compose logic closely.
                }

                paint.setColor(segmentColors[i]);
                canvas.drawArc(rectF, startAngle + (gap / 2), sweep, false, paint);
            }
        }
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
        invalidate();
    }
}
