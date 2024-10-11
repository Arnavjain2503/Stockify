package com.example.stockify;import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class GraphView extends View {
    private Paint paint;
    private float[] points;
    private float progress = 0;
    private int maxSteps;

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(8f);

        points = new float[] {
                0, 100, 200, 50, 300, 150, 400, 100, 500, 250, 600
        };

        maxSteps = points.length - 1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        float stepX = width / (float) maxSteps;

        for (int i = 0; i < points.length - 1 && i <= progress; i++) {
            canvas.drawLine(
                    stepX * i, height - points[i],
                    stepX * (i + 1), height - points[i + 1],
                    paint
            );
        }
        progress += 0.5f;
        if (progress < maxSteps) {
            postInvalidateDelayed(50);
        }
    }
}
