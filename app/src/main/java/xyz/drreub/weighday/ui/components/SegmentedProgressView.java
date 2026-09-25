package xyz.drreub.weighday.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SegmentedProgressView extends View {

    private final Paint paint;
    private final RectF rectF;
    private float progress = 0f; // 0.0 to 1.0
    private final float strokeWidth = 50f;
    private Shader progressGradient;

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
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        float halfStroke = strokeWidth / 2f;
        rectF.set(halfStroke, halfStroke, width - halfStroke, height - halfStroke);
        // A spatial gradient stays seamless where the full ring closes at the top.
        progressGradient = new LinearGradient(0f, 0f, width, height,
                new int[]{0xFFE99086, 0xFFE6C575, 0xFF77B8A0},
                new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP);
    }

    // Draws the ring and the progress arc
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        paint.setShader(null);
        paint.setColor(Color.LTGRAY);
        paint.setAlpha(50);
        canvas.drawArc(rectF, -90f, 360f, false, paint);

        if (progress > 0f) {
            paint.setColor(Color.WHITE);
            paint.setAlpha(255);
            paint.setShader(progressGradient);
            canvas.drawArc(rectF, -90f, progress * 360f, false, paint);
        }
    }

    // Setters and getters for progress
    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
        invalidate();
    }

    public float getProgress() {
        return progress;
    }
}
