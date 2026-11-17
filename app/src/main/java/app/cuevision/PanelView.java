package app.cuevision;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

public class PanelView extends View {

    public interface Listener {
        void onClose();
        void onMarkersChanged(boolean on);
        void onMove(int x, int y);
        void onRefresh();
        void onResize();
    }

    private static final int R_GL = 0, R_LEN = 1, R_ANG = 2, R_MESA = 3,
            R_GHOST = 4, R_BANKS = 5, R_MARK = 6, R_MIN = 7, R_CLOSE = 8;

    private final Prefs prefs;
    private final Listener listener;
    private final float d;

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint accent = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF header = new RectF();
    private final RectF[] rows = new RectF[9];

    public int winX = 40, winY = 40;
    private boolean collapsed = false;
    private int dragging = -1;
    private float grabX, grabY;

    public PanelView(Context c, Prefs p, Listener l) {
        super(c);
        prefs = p;
        listener = l;
        d = c.getResources().getDisplayMetrics().density;

        bgPaint.setColor(Color.parseColor("#F00B0B0D"));
        accent.setColor(Color.parseColor("#E02A2A"));
        textPaint.setColor(Color.WHITE);

        for (int i = 0; i < rows.length; i++) rows[i] = new RectF();
    }

    private float rowH() { return 30 * d; }
    private float headerH() { return 34 * d; }

    @Override
    protected void onMeasure(int wSpec, int hSpec) {
        int w = (int) (240 * d);
        int h = (int) (headerH() + (collapsed ? 0 : rowH() * rows.length));
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();

        header.set(0, 0, w, headerH());
        for (int i = 0; i < rows.length; i++) {
            float top = header.bottom + rowH() * i;
            rows[i].set(0, top, w, top + rowH());
        }

        canvas.drawRoundRect(new RectF(0, 0, w, h), 10 * d, 10 * d, bgPaint);
        canvas.drawRoundRect(header, 10 * d, 10 * d, accent);

        textPaint.setTextSize(12 * d);
        canvas.drawText("CUEVISION", 12 * d, header.centerY() + 4 * d, textPaint);
        canvas.drawText(collapsed ? "+" : "-", w - 22 * d, header.centerY() + 4 * d, textPaint);

        if (collapsed) return;

        textPaint.setTextSize(11 * d);
        drawRow(canvas, R_GL, "Linea", prefs.guideline() ? "ON" : "OFF", prefs.guideline());
        drawSlider(canvas, R_LEN, "Largo", prefs.length(), 0.1f, 1.0f);
        drawSlider(canvas, R_ANG, "Angulo", prefs.angle(), -45f, 45f);
        drawSlider(canvas, R_MESA, "Mesa", prefs.inset(), 0.03f, 0.22f);
        drawRow(canvas, R_GHOST, "Fantasma", prefs.ghost() ? "ON" : "OFF", prefs.ghost());
        drawRow(canvas, R_BANKS, "Bandas", prefs.banks() ? "ON" : "OFF", prefs.banks());
        drawRow(canvas, R_MARK, "Marcadores", prefs.markers() ? "ON" : "OFF", prefs.markers());
        drawRow(canvas, R_MIN, "Minimizar", "", false);
        drawRow(canvas, R_CLOSE, "Cerrar", "", false);
    }

    private void drawRow(Canvas canvas, int i, String label, String value, boolean on) {
        RectF r = rows[i];
        textPaint.setColor(Color.parseColor("#ECECEE"));
        canvas.drawText(label, r.left + 10 * d, r.centerY() + 4 * d, textPaint);

        if (!value.isEmpty()) {
            textPaint.setColor(on ? Color.parseColor("#4ADE80") : Color.parseColor("#8A8A93"));
            canvas.drawText(value, r.right - 34 * d, r.centerY() + 4 * d, textPaint);
        }

        if (on) {
            canvas.drawCircle(r.right - 16 * d, r.centerY(), 6 * d, accent);
        } else {
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2 * d);
            p.setColor(Color.parseColor("#4A4A55"));
            canvas.drawCircle(r.right - 16 * d, r.centerY(), 6 * d, p);
        }
    }

    private void drawSlider(Canvas canvas, int i, String label, float value, float min, float max) {
        RectF r = rows[i];
        textPaint.setColor(Color.parseColor("#ECECEE"));
        canvas.drawText(label, r.left + 10 * d, r.centerY() + 4 * d, textPaint);

        float barLeft = r.left + 78 * d;
        float barRight = r.right - 14 * d;
        float y = r.centerY();

        Paint bar = new Paint(Paint.ANTI_ALIAS_FLAG);
        bar.setColor(Color.parseColor("#2A2A32"));
        canvas.drawRoundRect(barLeft, y - 2 * d, barRight, y + 2 * d, 2 * d, 2 * d, bar);

        float t = (value - min) / (max - min);
        float knob = barLeft + (barRight - barLeft) * t;
        canvas.drawRoundRect(barLeft, y - 2 * d, knob, y + 2 * d, 2 * d, 2 * d, accent);
        canvas.drawCircle(knob, y, 5 * d, accent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX(), y = e.getY();

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (collapsed && y > headerH()) return false;

                if (header.contains(x, y)) {
                    if (x > header.right - 34 * d) {
                        dragging = -3;
                        return true;
                    }
                    dragging = -2;
                    grabX = e.getRawX() - winX;
                    grabY = e.getRawY() - winY;
                    return true;
                }

                for (int i = 0; i < rows.length; i++) {
                    if (rows[i].contains(x, y)) {
                        dragging = i;
                        if (i >= R_LEN && i <= R_MESA) applySlider(i, x);
                        return true;
                    }
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (dragging == -2) {
                    listener.onMove((int) (e.getRawX() - grabX), (int) (e.getRawY() - grabY));
                } else if (dragging >= R_LEN && dragging <= R_MESA) {
                    applySlider(dragging, x);
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (dragging == R_GL) {
                    prefs.setGuideline(!prefs.guideline());
                } else if (dragging == R_GHOST) {
                    prefs.setGhost(!prefs.ghost());
                } else if (dragging == R_BANKS) {
                    prefs.setBanks(!prefs.banks());
                } else if (dragging == R_MARK) {
                    prefs.setMarkers(!prefs.markers());
                    listener.onMarkersChanged(prefs.markers());
                } else if (dragging == R_MIN) {
                    collapsed = true;
                    requestLayout();
                    listener.onResize();
                } else if (dragging == R_CLOSE) {
                    listener.onClose();
                } else if (dragging == -3) {
                    collapsed = !collapsed;
                    requestLayout();
                    listener.onResize();
                }

                dragging = -1;
                listener.onRefresh();
                invalidate();
                return true;
        }
        return true;
    }

    private void applySlider(int i, float x) {
        RectF r = rows[i];
        float barLeft = r.left + 78 * d;
        float barRight = r.right - 14 * d;
        float t = (x - barLeft) / (barRight - barLeft);
        t = Math.max(0f, Math.min(1f, t));

        if (i == R_LEN) prefs.setLength(0.1f + t * 0.9f);
        else if (i == R_ANG) prefs.setAngle(-45f + t * 90f);
        else prefs.setInset(0.03f + t * 0.19f);

        listener.onRefresh();
        invalidate();
    }
}