package com.fongmi.android.tv.player.engine;

import static com.fongmi.android.tv.player.engine.PlayerEngine.Type.EXO;
import static com.fongmi.android.tv.player.engine.PlayerEngine.Type.MPV;

import androidx.media3.common.Player;

import com.fongmi.android.tv.player.exo.ExoPlayerEngine;
import com.fongmi.android.tv.player.media.PlaySpec;
import com.fongmi.android.tv.setting.PlayerSetting;

public final class PlayerEngineFactory {

    /** 进程内强制回退 EXO（不改用户全局设置），由播放失败自动触发。 */
    private static volatile boolean forceExo;

    public static void setForceExo(boolean value) {
        forceExo = value;
    }

    public static boolean isForceExo() {
        return forceExo;
    }

    public static PlayerEngine create(int decode, Player.Listener listener) {
        return create(decode, resolve(), listener);
    }

    public static PlayerEngine create(int decode, PlaySpec spec, Player.Listener listener) {
        return create(decode, resolve(spec), listener);
    }

    public static PlayerEngine create(int decode, PlayerEngine.Type type, Player.Listener listener) {
        return switch (type) {
            case EXO -> new ExoPlayerEngine(decode, listener);
            case MPV -> new MpvPlayerEngine(decode, listener);
        };
    }

    public static boolean matches(PlayerEngine engine, PlaySpec spec) {
        return engine != null && engine.getType() == resolve(spec);
    }

    private static PlayerEngine.Type resolve(PlaySpec spec) {
        return requiresExo(spec) ? EXO : resolve();
    }

    private static PlayerEngine.Type resolve() {
        if (forceExo) return EXO;
        return PlayerSetting.isMpv() && MpvPlayerEngine.isAvailable() ? MPV : EXO;
    }

    private static boolean requiresExo(PlaySpec spec) {
        return spec != null && spec.getDrm() != null;
    }
}
