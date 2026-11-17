package app.cuevision;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.view.View;
import java.util.List;

public class GuidelineView extends View {

    private final Prefs prefs;
    private final float d;

    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dash = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ghostPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pocketPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);

    public GuidelineView(Context c, Prefs p) {
        super(c);
        prefs = p;
        d = c.getResources().getDisplayMetrics().density;

        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(3 * d);
        line.setColor(Color.parseColor("#E02A2A"));

        dash.setStyle(Paint.Style.STROKE);
        dash.setStrokeWidth(2 * d);
        dash.setColor(Color.parseColor("#CC4ADE80"));
        dash.setPathEffect(new DashPathEffect(new float[]{8 * d, 6 * d}, 0));

        ghostPaint.setStyle(Paint.Style.STROKE);
        ghostPaint.setStrokeWidth(2 * d);
        ghostPaint.setColor(Color.parseColor("#AAFFFFFF"));
        ghostPaint.setPathEffect(new DashPathEffect(new float[]{5 * d, 5 * d}, 0));

        pocketPaint.setStyle(Paint.Style.STROKE);
        pocketPaint.setStrokeWidth(2 * d);
        pocketPaint.setColor(Color.parseColor("#884ADE80"));

        text.setColor(Color.WHITE);
    }

    public void refresh() { invalidate(); }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!prefs.guideline()) return;
        if (prefs.ox() < 0) return;

        float w = getWidth(), h = getHeight();
        float ins = prefs.inset();
        float[] rect = {ins * w, ins * h, w - ins * w, h - ins * h};
        float radius = 11 * d;

        float[][] ps = AimGeometry.pockets(rect);
        for (float[] p : ps) canvas.drawCircle(p[0], p[1], 9 * d, pocketPaint);

        float[] cb = {prefs.cx() * w, prefs.cy() * h};
        float[] ob = {prefs.ox() * w, prefs.oy() * h};

        int best = AimGeometry.bestPocket(cb, ob, rect, radius, 80f);
        if (best < 0) {
            canvas.drawLine(cb[0], cb[1], ob[0], ob[1], line);
            return;
        }

        float[] pocket = ps[best];
        float angle = AimGeometry.cutAngle(cb, ob, pocket);

        canvas.drawCircle(pocket[0], pocket[1], 14 * d, line);

        float[] g = AimGeometry.ghostBall(ob, pocket, radius);
        canvas.drawLine(cb[0], cb[1], g[0], g[1], line);
        canvas.drawLine(ob[0], ob[1], pocket[0], pocket[1], dash);

        if (prefs.ghost()) canvas.drawCircle(g[0], g[1], radius, ghostPaint);

        if (prefs.banks()) {
            List<float[][]> segs = AimGeometry.bankSegments(ob,
                    AimGeometry.unit(ob, pocket), rect, 4);
            for (float[][] s : segs) canvas.drawLine(s[0][0], s[0][1], s[1][0], s[1][1], dash);
        }

        float len = AimGeometry.dist(cb, ob) + AimGeometry.dist(ob, pocket);
        String hud = String.format("Corte %.0f  |  %s", angle,
                AimGeometry.difficulty(angle, len, rect[2] - rect[0]));

        text.setTextSize(13 * d);
        canvas.drawText(hud, w / 2f - text.measureText(hud) / 2f, 34 * d, text);
    }
}