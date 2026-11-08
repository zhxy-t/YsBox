
import android.app.Activity;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.setting.PlayerSetting;
import com.fongmi.android.tv.utils.ResUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * 屏显设置：标题、分辨率、时间、进度、网速、迷你、播放参数，支持重置。
 */
public final class PlayerOsdDialog {

    private PlayerOsdDialog() {
    }

    public static void show(Activity activity, Runnable onChanged) {
        String[] items = ResUtil.getStringArray(R.array.select_player_osd);
        boolean[] checked = getChecked();
        AlertDialog dialog = DialogThemeHelper.builder(activity)
                .setTitle(R.string.player_osd)
                .setMultiChoiceItems(items, checked, (d, which, isChecked) -> {
                    checked[which] = isChecked;
                    setChecked(checked);
                    if (onChanged != null) onChanged.run();
                })
                .setNeutralButton(R.string.player_osd_reset, null)
                .setNegativeButton(R.string.dialog_negative, null)
                .create();
        dialog.show();
        // 重置只刷新勾选状态，不关闭弹窗
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
            PlayerSetting.resetOsd();
            boolean[] fresh = getChecked();
            ListView list = dialog.getListView();
            for (int i = 0; i < fresh.length; i++) list.setItemChecked(i, fresh[i]);
            if (onChanged != null) onChanged.run();
        });
    }

    public static boolean[] getChecked() {
        return new boolean[]{
                PlayerSetting.isOsdTitle(),
                PlayerSetting.isOsdResolution(),
                PlayerSetting.isOsdTime(),
                PlayerSetting.isOsdProgress(),
                PlayerSetting.isOsdTraffic(),
                PlayerSetting.isOsdMini(),
                PlayerSetting.isOsdDiagnostics()
        };
    }

    public static void setChecked(boolean[] checked) {
        PlayerSetting.putOsdTitle(checked[0]);
        PlayerSetting.putOsdResolution(checked[1]);
        PlayerSetting.putOsdTime(checked[2]);
        PlayerSetting.putOsdProgress(checked[3]);
        PlayerSetting.putOsdTraffic(checked[4]);
        PlayerSetting.putOsdMini(checked[5]);
        PlayerSetting.putOsdDiagnostics(checked[6]);
    }
}
