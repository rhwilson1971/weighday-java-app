package xyz.drreub.weighday.ui.components;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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
        final int color;
        Arc(float start, float sweep, int color) { this.start = start; this.sweep = sweep; this.color = color; }
    }

    private static class RecordingCanvas extends Canvas {
        final List<Arc> arcs = new ArrayList<>();
        @Override
        public void drawArc(RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint) {
            arcs.add(new Arc(startAngle, sweepAngle, paint.getColor()));
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
    public void draw_zeroProgress_drawsOnlyEightBackgroundSegments() {
        view.setProgress(0f);
        List<Arc> arcs = draw();
        assertEquals(8, arcs.size());
        for (Arc a : arcs) {
            assertEquals(Color.LTGRAY, a.color);
            assertEquals(45f - 6f, a.sweep, 0.001f); // degreesPerSegment - gap
        }
    }

    @Test
    public void draw_halfProgress_fillsFirstFourSegmentsWithTheirColors() {
        view.setProgress(0.5f);
        List<Arc> arcs = draw();
        assertEquals(8 + 4, arcs.size());
        assertEquals(Color.RED, arcs.get(8).color);
        assertEquals(Color.RED, arcs.get(9).color);
        assertEquals(0xFFFFA500, arcs.get(10).color);
        assertEquals(0xFFFFA500, arcs.get(11).color);
    }

    @Test
    public void draw_fullProgress_fillsAllEightSegments() {
        view.setProgress(1f);
        List<Arc> arcs = draw();
        assertEquals(16, arcs.size());
        assertEquals(Color.GREEN, arcs.get(15).color);
    }

    @Test
    public void draw_partialSegment_drawsPartialSweep() {
        view.setProgress(0.0625f); // 22.5 degrees = half of the first 45-degree segment
        List<Arc> arcs = draw();
        assertEquals(9, arcs.size());
        assertEquals(22.5f, arcs.get(8).sweep, 0.001f);
    }
}
