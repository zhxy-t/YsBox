package com.fongmi.android.tv.player.effect.audio;

/**
 * Downmix helpers used by {@link AudioEffectProcessor}.
 *
 * <p>The gain tables mirror the matrices previously exported by the bundled media3 fork so
 * stereo/mono conversion keeps the same output levels.
 */
final class AudioChannelMix {

    private static final float POWER_GAIN = 0.7071f;
    private static final float LOW_FREQUENCY_GAIN = 0.5f;

    private static final float[][] STEREO_LEFT = {
            {POWER_GAIN},
            {1.0f, 0.0f},
            {1.0f, 0.0f, POWER_GAIN},
            {1.0f, 0.0f, POWER_GAIN, 0.0f},
            {1.0f, 0.0f, POWER_GAIN, POWER_GAIN, 0.0f},
            {1.0f, 0.0f, POWER_GAIN, LOW_FREQUENCY_GAIN, POWER_GAIN, 0.0f},
            {1.0f, 0.0f, POWER_GAIN, LOW_FREQUENCY_GAIN, POWER_GAIN, 0.0f, POWER_GAIN},
            {1.0f, 0.0f, POWER_GAIN, LOW_FREQUENCY_GAIN, POWER_GAIN, 0.0f, POWER_GAIN, 0.0f}
    };

    private static final float[][] STEREO_RIGHT = {
            {POWER_GAIN},
            {0.0f, 1.0f},
            {0.0f, 1.0f, POWER_GAIN},
            {0.0f, 1.0f, 0.0f, POWER_GAIN},
            {0.0f, 1.0f, POWER_GAIN, 0.0f, POWER_GAIN},
            {0.0f, 1.0f, POWER_GAIN, LOW_FREQUENCY_GAIN, 0.0f, POWER_GAIN},
            {0.0f, 1.0f, POWER_GAIN, LOW_FREQUENCY_GAIN, 0.0f, POWER_GAIN, POWER_GAIN},
            {0.0f, 1.0f, POWER_GAIN, LOW_FREQUENCY_GAIN, 0.0f, POWER_GAIN, 0.0f, POWER_GAIN}
    };

    private static final float[][] MONO = {
            {1.0f},
            {POWER_GAIN, POWER_GAIN},
            {POWER_GAIN, POWER_GAIN, 1.0f},
            {POWER_GAIN, POWER_GAIN, LOW_FREQUENCY_GAIN, LOW_FREQUENCY_GAIN},
            {POWER_GAIN, POWER_GAIN, 1.0f, LOW_FREQUENCY_GAIN, LOW_FREQUENCY_GAIN},
            {POWER_GAIN, POWER_GAIN, 1.0f, POWER_GAIN, LOW_FREQUENCY_GAIN, LOW_FREQUENCY_GAIN},
            {POWER_GAIN, POWER_GAIN, 1.0f, POWER_GAIN, LOW_FREQUENCY_GAIN, LOW_FREQUENCY_GAIN, LOW_FREQUENCY_GAIN},
            {POWER_GAIN, POWER_GAIN, 1.0f, POWER_GAIN, LOW_FREQUENCY_GAIN, LOW_FREQUENCY_GAIN, LOW_FREQUENCY_GAIN, LOW_FREQUENCY_GAIN}
    };

    private AudioChannelMix() {
    }

    static float mixStereoLeft(float[] samples) {
        return mix(samples, STEREO_LEFT[Math.clamp(samples.length, 1, 8) - 1]);
    }

    static float mixStereoRight(float[] samples) {
        return mix(samples, STEREO_RIGHT[Math.clamp(samples.length, 1, 8) - 1]);
    }

    static float mixMono(float[] samples) {
        return mix(samples, MONO[Math.clamp(samples.length, 1, 8) - 1]);
    }

    private static float mix(float[] samples, float[] gains) {
        float value = 0.0f;
        for (int i = 0; i < Math.min(samples.length, gains.length); i++) value += samples[i] * gains[i];
        return value;
    }
}
