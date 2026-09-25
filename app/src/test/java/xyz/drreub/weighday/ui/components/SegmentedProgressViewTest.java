package xyz.drreub.weighday.ui.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Shader;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class SegmentedProgressViewTest {

    private static class Arc {
        final float start, sweep;
        final Shader shader;
        final Paint.Cap cap;
        Arc(float start, float sweep, Paint paint) {
            this.start = start; this.sweep = sweep;
            this.shader = paint.getShader(); this.cap = paint.getStrokeCap();
        }
    }

    private static class RecordingCanvas extends Canvas {
        final List<Arc> arcs = new ArrayList<>();
        @Override
        public void drawArc(RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint) {
            arcs.add(new Arc(startAngle, sweepAngle, paint));
        }
    }

    private Context context;
    private SegmentedProgressView view;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        view = new SegmentedProgressView(context);
        view.layout(0, 0, 400, 400);
    }

    private List<Arc> draw() {
        RecordingCanvas canvas = new RecordingCanvas();
        view.draw(canvas);
        return canvas.arcs;
    }

    @Test
    public void attributeConstructor_works() {
        assertEquals(0f, new SegmentedProgressView(context, null).getProgress(), 0f);
    }

    @Test
    public void setProgress_clampsToZeroAndOne() {
        view.setProgress(-3f);
        assertEquals(0f, view.getProgress(), 0f);
        view.setProgress(7f);
        assertEquals(1f, view.getProgress(), 0f);
        view.setProgress(0.25f);
        assertEquals(0.25f, view.getProgress(), 0f);
    }

    @Test
    public void onMeasure_isSquareUsingSmallerSide() {
        view.measure(View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY));
        assertEquals(300, view.getMeasuredWidth());
        assertEquals(300, view.getMeasuredHeight());
    }

    @Test
    public void draw_zeroProgress_drawsContinuousTrack() {
        view.setProgress(0f);
        List<Arc> arcs = draw();
        assertEquals(1, arcs.size());
        assertEquals(360f, arcs.get(0).sweep, 0.001f);
        assertNull(arcs.get(0).shader);
    }

    @Test
    public void draw_halfProgress_drawsContinuousGradientWithRoundedEnds() {
        view.setProgress(0.5f);
        List<Arc> arcs = draw();
        assertEquals(2, arcs.size());
        assertEquals(-90f, arcs.get(1).start, 0.001f);
        assertEquals(180f, arcs.get(1).sweep, 0.001f);
        assertNotNull(arcs.get(1).shader);
        assertEquals(Paint.Cap.ROUND, arcs.get(1).cap);
    }

    @Test
    public void draw_fullProgress_closesRing() {
        view.setProgress(1f);
        List<Arc> arcs = draw();
        assertEquals(2, arcs.size());
        assertEquals(360f, arcs.get(1).sweep, 0.001f);
        assertNotNull(arcs.get(1).shader);
    }

    @Test
    public void draw_smallProgress_preservesExactSweep() {
        view.setProgress(0.0625f);
        List<Arc> arcs = draw();
        assertEquals(2, arcs.size());
        assertEquals(22.5f, arcs.get(1).sweep, 0.001f);
    }

    @Test
    public void draw_again_keepsTrackFreeOfGradient() {
        view.setProgress(0.5f);
        draw();
        assertNull(draw().get(0).shader);
    }
}
