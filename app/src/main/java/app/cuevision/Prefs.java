package app.cuevision;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {

    private final SharedPreferences sp;

    public Prefs(Context c) {
        sp = c.getSharedPreferences("cv", Context.MODE_PRIVATE);
    }

    public String key() { return sp.getString("key", ""); }
    public void setKey(String v) { sp.edit().putString("key", v).apply(); }

    public boolean guideline() { return sp.getBoolean("gl", true); }
    public void setGuideline(boolean v) { sp.edit().putBoolean("gl", v).apply(); }

    public float length() { return sp.getFloat("len", 0.45f); }
    public void setLength(float v) { sp.edit().putFloat("len", v).apply(); }

    public float angle() { return sp.getFloat("ang", 0f); }
    public void setAngle(float v) { sp.edit().putFloat("ang", v).apply(); }

    public float inset() { return sp.getFloat("ins", 0.10f); }
    public void setInset(float v) { sp.edit().putFloat("ins", v).apply(); }

    public float cx() { return sp.getFloat("cx", 0.25f); }
    public void setCx(float v) { sp.edit().putFloat("cx", v).apply(); }

    public float cy() { return sp.getFloat("cy", 0.75f); }
    public void setCy(float v) { sp.edit().putFloat("cy", v).apply(); }

    public boolean markers() { return sp.getBoolean("mk", true); }
    public void setMarkers(boolean v) { sp.edit().putBoolean("mk", v).apply(); }
}