package app.cuevision;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import androidx.core.app.NotificationCompat;

public class OverlayService extends Service implements PanelView.Listener, MarkerView.Listener {

    private WindowManager wm;
    private GuidelineView guide;
    private PanelView panel;
    private MarkerView cue, obj;
    private WindowManager.LayoutParams gParams, pParams, cParams, oParams;
    private Prefs prefs;
    private int sw, sh;
    private float half;

    @Override
    public IBinder onBind(Intent i) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        prefs = new Prefs(this);
        startForeground(1, buildNotification());

        DisplayMetrics m = getResources().getDisplayMetrics();
        sw = m.widthPixels;
        sh = m.heightPixels;
        half = 28 * m.density;

        gParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        gParams.gravity = Gravity.TOP | Gravity.START;

        guide = new GuidelineView(this, prefs);
        wm.addView(guide, gParams);

        pParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        pParams.gravity = Gravity.TOP | Gravity.START;
        pParams.x = 40;
        pParams.y = 40;

        panel = new PanelView(this, prefs, this);
        panel.winX = 40;
        panel.winY = 40;
        wm.addView(panel, pParams);

        if (prefs.markers()) addMarkers();
    }

    private void addMarkers() {
        if (cue != null) return;

        cParams = markerParams(prefs.cx(), prefs.cy());
        cue = new MarkerView(this, 0xFFFFFFFF, this);
        wm.addView(cue, cParams);

        oParams = markerParams(prefs.ox() < 0 ? 0.5f : prefs.ox(),
                prefs.oy() < 0 ? 0.5f : prefs.oy());
        obj = new MarkerView(this, 0xFFF5C542, this);
        wm.addView(obj, oParams);
    }

    private WindowManager.LayoutParams markerParams(float fx, float fy) {
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                (int) (half * 2), (int) (half * 2),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.START;
        p.x = (int) (fx * sw - half);
        p.y = (int) (fy * sh - half);
        return p;
    }

    private void removeMarkers() {
        if (cue != null) try { wm.removeView(cue); } catch (Exception ignored) { }
        if (obj != null) try { wm.removeView(obj); } catch (Exception ignored) { }
        cue = null;
        obj = null;
    }

    @Override
    public void onDrag(MarkerView v, float rawX, float rawY) {
        WindowManager.LayoutParams p = (v == cue) ? cParams : oParams;
        p.x = (int) (rawX - half);
        p.y = (int) (rawY - half);
        try { wm.updateViewLayout(v, p); } catch (Exception ignored) { }
    }

    @Override
    public void onDrop(MarkerView v) {
        WindowManager.LayoutParams p = (v == cue) ? cParams : oParams;
        float fx = (p.x + half) / sw;
        float fy = (p.y + half) / sh;

        if (v == cue) { prefs.setCx(fx); prefs.setCy(fy); }
        else { prefs.setOx(fx); prefs.setOy(fy); }

        guide.refresh();
    }

    @Override
    public void onMarkersChanged(boolean on) {
        if (on) addMarkers(); else removeMarkers();
        guide.refresh();
    }

    @Override
    public void onMove(int x, int y) {
        pParams.x = x;
        pParams.y = y;
        panel.winX = x;
        panel.winY = y;
        try { wm.updateViewLayout(panel, pParams); } catch (Exception ignored) { }
    }

    @Override
    public void onRefresh() { guide.refresh(); }

    @Override
    public void onResize() {
        try { wm.updateViewLayout(panel, pParams); } catch (Exception ignored) { }
    }

    @Override
    public void onClose() { stopSelf(); }

    private Notification buildNotification() {
        String ch = "cv";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(
                    new NotificationChannel(ch, "CueVision", NotificationManager.IMPORTANCE_LOW));
        }
        return new NotificationCompat.Builder(this, ch)
                .setContentTitle("CueVision activo")
                .setSmallIcon(R.drawable.ic_cv)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        removeMarkers();
        if (guide != null) try { wm.removeView(guide); } catch (Exception ignored) { }
        if (panel != null) try { wm.removeView(panel); } catch (Exception ignored) { }
        guide = null;
        panel = null;
        super.onDestroy();
    }
}