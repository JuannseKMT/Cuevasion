package app.cuevision;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

public class MarkerView extends View {

    public interface Listener {
        void onDrag(MarkerView v, float rawX, float rawY);
        void onDrop(MarkerView v);
    }

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Listener listener;
    private final float d;

    public MarkerView(Context c, int color, Listener l) {
        super(c);
        listener = l;
        d = c.getResources().getDisplayMetrics().density;

        fill.setColor(color);
        ring.setStyle(Paint.Style.STROKE);
        ring.setStrokeWidth(2 * d);
        ring.setColor(Color.parseColor("#AA000000"));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, 11 * d, fill);
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, 11 * d, ring);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                listener.onDrag(this, e.getRawX(), e.getRawY());
                return true;
            case MotionEvent.ACTION_UP:
                listener.onDrop(this);
                return true;
        }
        return false;
    }
}