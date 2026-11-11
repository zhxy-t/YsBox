package com.fongmi.android.tv.ui.lab;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fongmi.android.tv.ui.activity.LabActivity;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.R;

/**
 * 实验室设置页：点实验室首页标题「实验室」进入。
 * 全局代理（开关＋端口＋不走代理）、忽略电池优化、播放器实验开关、Python 环境。
 * 顶栏只留标题，走系统返回。
 */
public class LabSettingActivity extends AppCompatActivity {

    private LinearLayout body;
    private TextView batteryState;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, LabSettingActivity.class));
    }

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LabSkin.edgeToEdge(this);
        buildUi();

        // 页面进出统一渐入渐出（与 BaseActivity 一致）
        overridePendingTransition(R.anim.xc_fade_in, R.anim.xc_fade_out);
}

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.xc_fade_in, R.anim.xc_fade_out);
    }


    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private int dp(int v) {
        return LabSkin.dp(this, v);
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        // 标题行：只留标题
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        nav.setPadding(dp(18), dp(9) + LabSkin.statusBarHeight(this), dp(18), dp(20));
        TextView h1 = new TextView(this);
        h1.setText("实验室设置");
        h1.setTextSize(24);
        h1.setTextColor(LabSkin.WALL_TITLE);
        h1.getPaint().setFakeBoldText(true);
        LabSkin.halo(h1);
        h1.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        nav.addView(h1);
        root.addView(nav);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(0, 0, 0, dp(46) + LabSkin.navBarHeight(this));
        scroll.addView(body, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        LabSkin.setContentView(this, root);
    }

    private void refresh() {
        body.removeAllViews();
        body.addView(proxyCard());
        body.addView(batteryCard());
        body.addView(linkCard("播放器实验开关", "调试日志 / libass / 隧道解码 / 纹理渲染",
                () -> LabActivity.start(this)));
    }

    /** 跳转条目：标题＋说明，整卡可点。 */
    private View linkCard(String title, String hint, Runnable onClick) {
        LinearLayout card = cardBox();
        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView t = rowLabel(title);
        texts.addView(t);
        TextView h = new TextView(this);
        h.setText(hint);
        h.setTextSize(13);
        h.setTextColor(LabSkin.TEXT_ON_DARK_DIM);
        texts.addView(h);
        card.addView(texts);
        card.setOnClickListener(v -> onClick.run());
        return card;
    }

    /**
     * 全局代理卡：LabSettings 的 proxy_on/proxy_port/proxy_no 此前只有存取、
     * 没有任何界面能写（LabRunner.applyEnv 在读，用户却开不了），这里补齐入口。
     */
    private View proxyCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(LabSkin.glassDark(this));
        card.setPadding(dp(20), dp(20), dp(20), dp(20));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(18), 0, dp(18), dp(12));
        card.setLayoutParams(lp);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView label = rowLabel("全局代理");
        label.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        head.addView(label);
        head.addView(LabSkin.miniSwitch(this, LabSettings.proxyOn(this), on -> {
            LabSettings.setProxyOn(this, on);
            Notify.show(on ? "全局代理已开（新启动的服务生效）" : "全局代理已关");
        }));
        card.addView(head);

        card.addView(proxyValueRow("端口", LabSettings.proxyPort(this), () -> editProxyPort()));
        card.addView(proxyValueRow("不走代理", LabSettings.proxyNoProxy(this), () -> editProxyNoProxy()));
        return card;
    }

    private View proxyValueRow(String name, String value, Runnable onClick) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(14), 0, 0);
        TextView n = new TextView(this);
        n.setText(name);
        n.setTextSize(14);
        n.setTextColor(LabSkin.HINT_ON_DARK);
        n.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(n);
        TextView v = new TextView(this);
        v.setText(value == null || value.isEmpty() ? "未填" : value);
        v.setTextSize(14);
        v.setTextColor(LabSkin.TEXT_ON_DARK);
        row.addView(v);
        row.setOnClickListener(view -> onClick.run());
        return row;
    }

    private void editProxyPort() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        android.widget.EditText et = LabInstallDialog.darkEdit(this, "7890", LabSettings.proxyPort(this));
        et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        box.addView(et);
        LabInstallDialog.showForm(this, "代理端口", box, "保存", () -> {
            String p = et.getText() == null ? "" : et.getText().toString().trim();
            try {
                int port = Integer.parseInt(p);
                if (port < 1 || port > 65535) throw new NumberFormatException();
            } catch (Exception e) {
                Notify.show("端口得是 1 到 65535 的数字");
                return false;
            }
            LabSettings.setProxyPort(this, p);
            refresh();
            return true;
        }, null, null);
    }

    private void editProxyNoProxy() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        android.widget.EditText et = LabInstallDialog.darkEdit(this, "localhost,127.0.0.1", LabSettings.proxyNoProxy(this));
        box.addView(et);
        LabInstallDialog.showForm(this, "不走代理", box, "保存", () -> {
            LabSettings.setProxyNoProxy(this, et.getText() == null ? "" : et.getText().toString());
            refresh();
            return true;
        }, null, null);
    }

    private View batteryCard() {
        LinearLayout card = cardBox();
        TextView label = rowLabel("忽略电池优化");
        label.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        card.addView(label);
        batteryState = new TextView(this);
        batteryState.setTextSize(13);
        batteryState.getPaint().setFakeBoldText(true);
        card.addView(batteryState);
        refreshBatteryState();
        card.setOnClickListener(v -> {
            if (isIgnoringBatteryOptimizations()) {
                Notify.show("已经忽略了");
                return;
            }
            requestIgnoreBatteryOptimizations();
        });
        return card;
    }

    private void refreshBatteryState() {
        if (batteryState == null) return;
        boolean ignored = isIgnoringBatteryOptimizations();
        batteryState.setText(ignored ? "已忽略" : "未忽略");
        batteryState.setTextColor(ignored ? LabSkin.GREEN_ON_DARK : LabSkin.COUNT_YELLOW);
    }

    private boolean isIgnoringBatteryOptimizations() {
        try {
            android.os.PowerManager pm = (android.os.PowerManager) getSystemService(Context.POWER_SERVICE);
            return pm != null && pm.isIgnoringBatteryOptimizations(getPackageName());
        } catch (Exception e) {
            return false;
        }
    }

    private void requestIgnoreBatteryOptimizations() {
        try {
            startActivity(new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    .setData(android.net.Uri.parse("package:" + getPackageName())));
        } catch (Exception e) {
            try {
                startActivity(new Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
            } catch (Exception ignored) {
                Notify.show("系统没有电池优化设置页");
            }
        }
    }

    private LinearLayout cardBox() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackground(LabSkin.glassDark(this));
        card.setPadding(dp(20), dp(20), dp(20), dp(20));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(18), 0, dp(18), dp(12));
        card.setLayoutParams(lp);
        return card;
    }

    private TextView rowLabel(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(16);
        t.setTextColor(LabSkin.TEXT_ON_DARK);
        return t;
    }
}
