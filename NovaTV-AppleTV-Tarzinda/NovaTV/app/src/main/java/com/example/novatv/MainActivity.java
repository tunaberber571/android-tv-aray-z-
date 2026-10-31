package com.example.novatv;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * NOVA TV - cinematic, remote-friendly Android TV launcher.
 * Pure Android framework UI: no network access and no third-party UI dependencies.
 */
public class MainActivity extends Activity {
    private static final int BG = Color.rgb(7, 9, 17);
    private static final int TEXT = Color.rgb(250, 250, 253);
    private static final int MUTED = Color.rgb(174, 180, 197);
    private static final int ACCENT = Color.rgb(125, 190, 255);
    private static final int GLASS = Color.argb(30, 255, 255, 255);
    private static final int GLASS_LINE = Color.argb(42, 255, 255, 255);

    private final Handler handler = new Handler();
    private TextView clockView;
    private TextView dateView;
    private TextView appCountView;
    private TextView heroTitle;
    private TextView heroDescription;
    private TextView heroEyebrow;
    private TextView heroOpenButton;
    private HorizontalScrollView appScroller;
    private LinearLayout appStrip;
    private ScrollView pageScroll;
    private View firstAppCard;
    private AppEntry selectedApp;
    private final List<AppEntry> loadedApps = new ArrayList<>();

    private final Runnable clockTick = new Runnable() {
        @Override public void run() {
            if (clockView != null) {
                clockView.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
            }
            if (dateView != null) {
                dateView.setText(new SimpleDateFormat("EEEE, d MMMM", new Locale("tr", "TR")).format(new Date()));
            }
            handler.postDelayed(this, 60_000L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemBars();
        getWindow().setBackgroundDrawableResource(android.R.color.black);
        buildScreen();
        refreshApps();
        handler.post(clockTick);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemBars();
        if (appStrip != null) appStrip.post(this::refreshApps);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(clockTick);
        super.onDestroy();
    }

    private void hideSystemBars() {
        Window window = getWindow();
        View decor = window.getDecorView();
        decor.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    private void buildScreen() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG);
        root.addView(new AmbientBackgroundView(this), new FrameLayout.LayoutParams(-1, -1));

        pageScroll = new ScrollView(this);
        pageScroll.setFillViewport(true);
        pageScroll.setClipToPadding(false);
        pageScroll.setVerticalScrollBarEnabled(false);
        pageScroll.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(54), dp(29), dp(54), dp(24));
        pageScroll.addView(page, new ScrollView.LayoutParams(-1, -2));

        // Top navigation: light, compact and easy to use with a TV remote.
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setOrientation(LinearLayout.HORIZONTAL);

        TextView logo = text("NOVA", 27, TEXT, true);
        logo.setLetterSpacing(0.16f);
        top.addView(logo, margin(0, 0, 30, 0));

        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        TextView homeNav = navButton("Ana Sayfa", true);
        TextView appsNav = navButton("Uygulamalar", false);
        TextView settingsNav = navButton("Ayarlar", false);
        nav.addView(homeNav, margin(0, 0, 8, 0));
        nav.addView(appsNav, margin(0, 0, 8, 0));
        nav.addView(settingsNav);
        top.addView(nav, new LinearLayout.LayoutParams(0, -2, 1f));

        LinearLayout time = new LinearLayout(this);
        time.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        time.setOrientation(LinearLayout.VERTICAL);
        clockView = text("--:--", 21, TEXT, true);
        clockView.setGravity(Gravity.END);
        dateView = text("", 11, MUTED, false);
        dateView.setGravity(Gravity.END);
        time.addView(clockView);
        time.addView(dateView);
        top.addView(time, new LinearLayout.LayoutParams(-2, -2));
        page.addView(top, margin(0, 0, 0, 23));

        homeNav.setOnClickListener(v -> pageScroll.smoothScrollTo(0, 0));
        appsNav.setOnClickListener(v -> focusFirstApp());
        settingsNav.setOnClickListener(v -> openSettings());

        // Large cinematic featured panel.
        FrameLayout hero = new FrameLayout(this);
        hero.setFocusable(false);
        GradientDrawable heroShape = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(21, 30, 58), Color.rgb(32, 24, 54), Color.rgb(15, 28, 47)}
        );
        heroShape.setCornerRadius(dp(29));
        hero.setBackground(heroShape);
        if (Build.VERSION.SDK_INT >= 21) hero.setClipToOutline(true);
        hero.addView(new HeroArtworkView(this), new FrameLayout.LayoutParams(-1, -1));

        LinearLayout heroContent = new LinearLayout(this);
        heroContent.setOrientation(LinearLayout.VERTICAL);
        heroContent.setGravity(Gravity.CENTER_VERTICAL);
        heroContent.setPadding(dp(34), dp(22), dp(24), dp(22));
        FrameLayout.LayoutParams heroContentParams = new FrameLayout.LayoutParams(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.57f), -1, Gravity.LEFT | Gravity.CENTER_VERTICAL
        );

        heroEyebrow = text("NOVA  •  EĞLENCE MERKEZİ", 11, Color.rgb(183, 211, 255), true);
        heroEyebrow.setLetterSpacing(0.14f);
        heroContent.addView(heroEyebrow, margin(0, 0, 0, 12));

        heroTitle = text("Eğlenceye\ntekrar hoş geldin.", 35, TEXT, true);
        heroTitle.setLineSpacing(dp(1), 1f);
        heroContent.addView(heroTitle, margin(0, 0, 0, 8));

        heroDescription = text("Tüm uygulamaların, tek ve sade bir ekranda.", 15, Color.rgb(214, 219, 233), false);
        heroDescription.setMaxLines(2);
        heroContent.addView(heroDescription, margin(0, 0, 0, 19));

        LinearLayout heroActions = new LinearLayout(this);
        heroActions.setGravity(Gravity.CENTER_VERTICAL);
        heroActions.setOrientation(LinearLayout.HORIZONTAL);
        heroOpenButton = pillButton("Uygulamaları keşfet", true);
        heroOpenButton.setOnClickListener(v -> focusFirstApp());
        TextView heroSettings = pillButton("TV ayarları", false);
        heroSettings.setOnClickListener(v -> openSettings());
        heroActions.addView(heroOpenButton, margin(0, 0, 10, 0));
        heroActions.addView(heroSettings);
        heroContent.addView(heroActions);
        hero.addView(heroContent, heroContentParams);
        page.addView(hero, new LinearLayout.LayoutParams(-1, dp(292)));

        // App shelf.
        LinearLayout shelfHeader = new LinearLayout(this);
        shelfHeader.setGravity(Gravity.CENTER_VERTICAL);
        TextView appsTitle = text("Uygulamalar", 22, TEXT, true);
        shelfHeader.addView(appsTitle, new LinearLayout.LayoutParams(0, -2, 1f));
        appCountView = text("", 12, MUTED, false);
        shelfHeader.addView(appCountView);
        page.addView(shelfHeader, margin(0, 25, 0, 5));
        TextView shelfCaption = text("Devam etmek için bir uygulama seç", 12, MUTED, false);
        page.addView(shelfCaption, margin(0, 0, 0, 11));

        appScroller = new HorizontalScrollView(this);
        appScroller.setHorizontalScrollBarEnabled(false);
        appScroller.setClipToPadding(false);
        appScroller.setFillViewport(false);
        appScroller.setOverScrollMode(View.OVER_SCROLL_NEVER);
        appStrip = new LinearLayout(this);
        appStrip.setOrientation(LinearLayout.HORIZONTAL);
        appStrip.setGravity(Gravity.TOP | Gravity.START);
        appScroller.addView(appStrip, new HorizontalScrollView.LayoutParams(-2, -2));
        page.addView(appScroller, new LinearLayout.LayoutParams(-1, dp(169)));

        TextView hint = text("◀  ▶  Gezin     OK  Aç     HOME  Ana ekran", 11, Color.rgb(126, 136, 159), false);
        page.addView(hint, margin(2, 7, 0, 0));

        root.addView(pageScroll, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
        homeNav.requestFocus();
    }

    private void refreshApps() {
        if (appStrip == null) return;
        appStrip.removeAllViews();
        loadedApps.clear();
        loadedApps.addAll(loadApps());
        appCountView.setText(loadedApps.size() + " uygulama");

        if (loadedApps.isEmpty()) {
            TextView empty = text("Başlatılabilir uygulama bulunamadı.", 15, MUTED, false);
            empty.setPadding(dp(8), dp(22), dp(8), dp(22));
            appStrip.addView(empty);
            firstAppCard = null;
            return;
        }

        for (int i = 0; i < loadedApps.size(); i++) {
            AppEntry app = loadedApps.get(i);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(136), dp(156));
            params.rightMargin = dp(i == loadedApps.size() - 1 ? 0 : 17);
            View card = createAppCard(app, i);
            appStrip.addView(card, params);
            if (i == 0) firstAppCard = card;
        }
    }

    private List<AppEntry> loadApps() {
        PackageManager pm = getPackageManager();
        List<AppEntry> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        String[] categories = new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER};

        for (String category : categories) {
            Intent query = new Intent(Intent.ACTION_MAIN);
            query.addCategory(category);
            List<ResolveInfo> matches;
            if (Build.VERSION.SDK_INT >= 33) {
                matches = pm.queryIntentActivities(query, PackageManager.ResolveInfoFlags.of(0));
            } else {
                //noinspection deprecation
                matches = pm.queryIntentActivities(query, 0);
            }
            for (ResolveInfo info : matches) {
                if (info.activityInfo == null) continue;
                String packageName = info.activityInfo.packageName;
                if (packageName == null || packageName.equals(getPackageName()) || seen.contains(packageName)) continue;
                try {
                    CharSequence label = info.loadLabel(pm);
                    Drawable icon = info.loadIcon(pm);
                    result.add(new AppEntry(packageName, label == null ? packageName : label.toString(), icon));
                    seen.add(packageName);
                } catch (RuntimeException ignored) {
                    // Ignore apps removed while scanning.
                }
            }
        }
        Collator collator = Collator.getInstance(new Locale("tr", "TR"));
        Collections.sort(result, (a, b) -> collator.compare(a.label, b.label));
        return result;
    }

    private View createAppCard(AppEntry app, int index) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(7), dp(5), dp(7), dp(5));
        card.setFocusable(true);
        card.setClickable(true);
        card.setFocusableInTouchMode(true);
        card.setBackground(appCardBackground(false));
        card.setContentDescription(app.label + ", uygulamayı aç");

        FrameLayout iconFrame = new FrameLayout(this);
        GradientDrawable iconBg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.argb(48, 255, 255, 255), Color.argb(17, 255, 255, 255)});
        iconBg.setCornerRadius(dp(23));
        iconBg.setStroke(dp(1), Color.argb(34, 255, 255, 255));
        iconFrame.setBackground(iconBg);
        ImageView icon = new ImageView(this);
        icon.setImageDrawable(app.icon);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setPadding(dp(13), dp(13), dp(13), dp(13));
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(dp(91), dp(91), Gravity.CENTER);
        iconFrame.addView(icon, iconParams);
        LinearLayout.LayoutParams iconFrameParams = new LinearLayout.LayoutParams(dp(104), dp(104));
        iconFrameParams.bottomMargin = dp(9);
        card.addView(iconFrame, iconFrameParams);

        TextView label = text(app.label, 13, TEXT, true);
        label.setGravity(Gravity.CENTER);
        label.setMaxLines(2);
        label.setEllipsize(android.text.TextUtils.TruncateAt.END);
        card.addView(label, new LinearLayout.LayoutParams(-1, dp(36)));

        card.setOnFocusChangeListener((view, focused) -> {
            view.setBackground(appCardBackground(focused));
            view.animate().scaleX(focused ? 1.075f : 1f).scaleY(focused ? 1.075f : 1f)
                    .setDuration(145).start();
            view.setElevation(focused ? dp(12) : 0);
            if (focused) showSelectedApp(app);
        });
        card.setOnClickListener(v -> launchApp(app.packageName));
        card.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == android.view.KeyEvent.ACTION_UP
                    && (keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER
                    || keyCode == android.view.KeyEvent.KEYCODE_ENTER)) {
                launchApp(app.packageName);
                return true;
            }
            return false;
        });
        return card;
    }

    private void showSelectedApp(AppEntry app) {
        selectedApp = app;
        heroEyebrow.setText("UYGULAMA  •  NOVA TV");
        heroTitle.setText(app.label);
        heroDescription.setText("Açmak için kumandandaki OK tuşuna bas.");
        heroOpenButton.setText("Uygulamayı aç");
        heroOpenButton.setOnClickListener(v -> launchApp(app.packageName));
    }

    private void focusFirstApp() {
        if (firstAppCard != null) {
            pageScroll.post(() -> {
                pageScroll.smoothScrollTo(0, Math.max(0, appScroller.getTop() - dp(180)));
                firstAppCard.requestFocus();
            });
        } else {
            Toast.makeText(this, "Yüklü uygulama bulunamadı.", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchApp(String packageName) {
        PackageManager pm = getPackageManager();
        Intent launch = pm.getLeanbackLaunchIntentForPackage(packageName);
        if (launch == null) launch = pm.getLaunchIntentForPackage(packageName);
        if (launch == null) {
            Toast.makeText(this, "Bu uygulama başlatılamıyor.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            startActivity(launch);
        } catch (Exception e) {
            Toast.makeText(this, "Uygulama açılamadı.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        } catch (Exception e) {
            Toast.makeText(this, "Ayarlar açılamadı.", Toast.LENGTH_SHORT).show();
        }
    }

    private TextView navButton(String title, boolean selected) {
        TextView button = text(title, 14, selected ? TEXT : MUTED, true);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(14), dp(10), dp(14), dp(10));
        button.setFocusable(true);
        button.setClickable(true);
        button.setBackground(pillBackground(selected, false));
        button.setOnFocusChangeListener((v, focused) -> {
            v.setBackground(pillBackground(selected, focused));
            v.animate().scaleX(focused ? 1.045f : 1f).scaleY(focused ? 1.045f : 1f).setDuration(120).start();
        });
        return button;
    }

    private TextView pillButton(String title, boolean primary) {
        TextView button = text(title, 13, primary ? Color.rgb(12, 22, 37) : TEXT, true);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(19), dp(12), dp(19), dp(12));
        button.setFocusable(true);
        button.setClickable(true);
        button.setBackground(actionBackground(primary, false));
        button.setOnFocusChangeListener((v, focused) -> {
            v.setBackground(actionBackground(primary, focused));
            v.animate().scaleX(focused ? 1.04f : 1f).scaleY(focused ? 1.04f : 1f).setDuration(120).start();
        });
        return button;
    }

    private Drawable pillBackground(boolean selected, boolean focused) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(dp(24));
        shape.setColor(focused ? Color.argb(48, 255, 255, 255) : (selected ? Color.argb(28, 255, 255, 255) : Color.TRANSPARENT));
        if (focused) shape.setStroke(dp(1), Color.argb(100, 255, 255, 255));
        return shape;
    }

    private Drawable actionBackground(boolean primary, boolean focused) {
        GradientDrawable shape = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                primary
                        ? new int[]{focused ? Color.rgb(218, 237, 255) : Color.rgb(190, 220, 255), focused ? Color.rgb(177, 203, 255) : Color.rgb(163, 194, 255)}
                        : new int[]{Color.argb(focused ? 70 : 42, 255, 255, 255), Color.argb(focused ? 54 : 27, 255, 255, 255)});
        shape.setCornerRadius(dp(25));
        if (!primary || focused) shape.setStroke(dp(1), Color.argb(80, 255, 255, 255));
        return shape;
    }

    private Drawable appCardBackground(boolean focused) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(focused ? Color.argb(56, 255, 255, 255) : Color.TRANSPARENT);
        shape.setCornerRadius(dp(23));
        if (focused) shape.setStroke(dp(2), Color.argb(160, 215, 231, 255));
        return shape;
    }

    private TextView text(String value, float sizeSp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        view.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        view.setIncludeFontPadding(true);
        return view;
    }

    private LinearLayout.LayoutParams margin(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class AppEntry {
        final String packageName;
        final String label;
        final Drawable icon;
        AppEntry(String packageName, String label, Drawable icon) {
            this.packageName = packageName;
            this.label = label;
            this.icon = icon;
        }
    }

    /** Soft ambient light behind the content; drawn locally, no downloaded wallpaper. */
    private static class AmbientBackgroundView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        AmbientBackgroundView(Activity activity) { super(activity); }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth(), h = getHeight();
            paint.setShader(new RadialGradient(w * 0.78f, h * 0.23f, w * 0.62f,
                    new int[]{Color.argb(64, 61, 91, 156), Color.argb(0, 19, 26, 54)}, null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, paint);
            paint.setShader(new RadialGradient(w * 0.14f, h * 0.86f, w * 0.52f,
                    new int[]{Color.argb(34, 83, 50, 116), Color.argb(0, 15, 13, 28)}, null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, paint);
            paint.setShader(null);
        }
    }

    /** Decorative cinematic artwork for the featured panel. */
    private static class HeroArtworkView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        HeroArtworkView(Activity activity) { super(activity); setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth(), h = getHeight();
            paint.setShader(new RadialGradient(w * 0.78f, h * 0.42f, h * 0.95f,
                    new int[]{Color.argb(180, 74, 105, 193), Color.argb(65, 105, 76, 160), Color.argb(0, 26, 31, 70)},
                    new float[]{0f, 0.46f, 1f}, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, paint);
            paint.setShader(null);

            // Floating translucent panels, deliberately kept on the right half of the hero.
            canvas.save();
            canvas.rotate(-11f, w * 0.77f, h * 0.52f);
            rect.set(w * 0.69f, h * 0.12f, w * 0.91f, h * 0.89f);
            paint.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                    new int[]{Color.argb(190, 95, 139, 225), Color.argb(105, 65, 63, 145), Color.argb(155, 39, 47, 90)},
                    null, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rect, 26f, 26f, paint);
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2f);
            paint.setColor(Color.argb(95, 235, 243, 255));
            canvas.drawRoundRect(rect, 26f, 26f, paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.restore();

            canvas.save();
            canvas.rotate(12f, w * 0.87f, h * 0.50f);
            rect.set(w * 0.83f, h * 0.20f, w * 1.04f, h * 0.82f);
            paint.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                    new int[]{Color.argb(170, 229, 157, 185), Color.argb(110, 111, 91, 167), Color.argb(150, 43, 52, 99)},
                    null, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rect, 24f, 24f, paint);
            paint.setShader(null);
            canvas.restore();

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.5f);
            paint.setColor(Color.argb(100, 220, 232, 255));
            canvas.drawCircle(w * 0.79f, h * 0.52f, h * 0.23f, paint);
            canvas.drawCircle(w * 0.79f, h * 0.52f, h * 0.31f, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new RadialGradient(w * 0.79f, h * 0.52f, h * 0.20f,
                    new int[]{Color.argb(160, 203, 225, 255), Color.argb(30, 141, 161, 233), Color.argb(0, 90, 110, 190)},
                    null, Shader.TileMode.CLAMP));
            canvas.drawCircle(w * 0.79f, h * 0.52f, h * 0.20f, paint);
            paint.setShader(null);
        }
    }
}
