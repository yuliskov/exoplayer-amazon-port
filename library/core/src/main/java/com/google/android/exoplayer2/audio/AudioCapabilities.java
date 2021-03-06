/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer2.audio;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.support.annotation.Nullable;
import android.provider.Settings;

import com.google.android.exoplayer2.util.Util;

import java.util.Arrays;

/** Represents the set of audio formats that a device is capable of playing. */
@TargetApi(21)
public final class AudioCapabilities {

  private static final int DEFAULT_MAX_CHANNEL_COUNT = 8;

  /** The minimum audio capabilities supported by all devices. */
  public static final AudioCapabilities DEFAULT_AUDIO_CAPABILITIES =
      new AudioCapabilities(new int[] {AudioFormat.ENCODING_PCM_16BIT}, DEFAULT_MAX_CHANNEL_COUNT);

  // AMZN_CHANGE_BEGIN
   /** For Optical output, we read this global setting to detect if dolby
     * output is enabled. If USE_EXTERNAL_SURROUND_SOUND_FLAG is not set, then
     * we fallback on the HDMI audio intent.
     */
  public static final String EXTERNAL_SURROUND_SOUND_ENABLED = "external_surround_sound_enabled";
 public static final String USE_EXTERNAL_SURROUND_SOUND_FLAG = "use_external_surround_sound_flag";
  public static final AudioCapabilities SURROUND_AUDIO_CAPABILITIES =
      new AudioCapabilities(new int[] {AudioFormat.ENCODING_PCM_16BIT,
          AudioFormat.ENCODING_AC3,
          AudioFormat.ENCODING_E_AC3 },
          6);// TODO: 6 or 8 ? Currently not used by Exo in taking decisions
  // AMZN_CHANGE_END
  /**
   * Returns the current audio capabilities for the device.
   *
   * @param context A context for obtaining the current audio capabilities.
   * @return The current audio capabilities for the device.
   */
  @SuppressWarnings("InlinedApi")
  public static AudioCapabilities getCapabilities(Context context) {
    return getCapabilities(context, // AMZN_CHANGE_ONELINE
        context.registerReceiver(null, new IntentFilter(AudioManager.ACTION_HDMI_AUDIO_PLUG)));
  }

  @SuppressLint("InlinedApi")
  /* package */ static AudioCapabilities getCapabilities(Context context, @Nullable Intent intent) {
    // AMZN_CHANGE_BEGIN
    boolean useSurroundSoundFlag = false;
    boolean isSurroundSoundEnabled = false;

    // read global surround sound amazon specific settings
    if (Util.SDK_INT >= 17) {
        ContentResolver resolver = context.getContentResolver();
        useSurroundSoundFlag = useSurroundSoundFlagV17(resolver);
        isSurroundSoundEnabled = isSurroundSoundEnabledV17(resolver);
    }

    // use surround sound enabled flag if it is
    if (useSurroundSoundFlag) {
        return isSurroundSoundEnabled ? SURROUND_AUDIO_CAPABILITIES :
                DEFAULT_AUDIO_CAPABILITIES;
    }
    // AMZN_CHANGE_END
    if (intent == null || intent.getIntExtra(AudioManager.EXTRA_AUDIO_PLUG_STATE, 0) == 0) {
      return DEFAULT_AUDIO_CAPABILITIES;
    }
    return new AudioCapabilities(
        intent.getIntArrayExtra(AudioManager.EXTRA_ENCODINGS),
        intent.getIntExtra(
            AudioManager.EXTRA_MAX_CHANNEL_COUNT, /* defaultValue= */ DEFAULT_MAX_CHANNEL_COUNT));
  }

  // AMZN_CHANGE_BEGIN
  @TargetApi(17)
  public static boolean isSurroundSoundEnabledV17(ContentResolver resolver) {
    return Settings.Global.getInt(resolver, EXTERNAL_SURROUND_SOUND_ENABLED, 0) == 1;
  }
  public static boolean useSurroundSoundFlagV17(ContentResolver
        resolver) {
    return Settings.Global.getInt(resolver, USE_EXTERNAL_SURROUND_SOUND_FLAG,
            0) == 1;
  }
  // AMZN_CHANGE_END
  private final int[] supportedEncodings;
  private final int maxChannelCount;

  /**
   * Constructs new audio capabilities based on a set of supported encodings and a maximum channel
   * count.
   *
   * <p>Applications should generally call {@link #getCapabilities(Context)} to obtain an instance
   * based on the capabilities advertised by the platform, rather than calling this constructor.
   *
   * @param supportedEncodings Supported audio encodings from {@link android.media.AudioFormat}'s
   *     {@code ENCODING_*} constants. Passing {@code null} indicates that no encodings are
   *     supported.
   * @param maxChannelCount The maximum number of audio channels that can be played simultaneously.
   */
  public AudioCapabilities(@Nullable int[] supportedEncodings, int maxChannelCount) {
    if (supportedEncodings != null) {
      this.supportedEncodings = Arrays.copyOf(supportedEncodings, supportedEncodings.length);
      Arrays.sort(this.supportedEncodings);
    } else {
      this.supportedEncodings = new int[0];
    }
    this.maxChannelCount = maxChannelCount;
  }

  /**
   * Returns whether this device supports playback of the specified audio {@code encoding}.
   *
   * @param encoding One of {@link android.media.AudioFormat}'s {@code ENCODING_*} constants.
   * @return Whether this device supports playback the specified audio {@code encoding}.
   */
  public boolean supportsEncoding(int encoding) {
    return Arrays.binarySearch(supportedEncodings, encoding) >= 0;
  }

  /**
   * Returns the maximum number of channels the device can play at the same time.
   */
  public int getMaxChannelCount() {
    return maxChannelCount;
  }

  @Override
  public boolean equals(@Nullable Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof AudioCapabilities)) {
      return false;
    }
    AudioCapabilities audioCapabilities = (AudioCapabilities) other;
    return Arrays.equals(supportedEncodings, audioCapabilities.supportedEncodings)
        && maxChannelCount == audioCapabilities.maxChannelCount;
  }

  @Override
  public int hashCode() {
    return maxChannelCount + 31 * Arrays.hashCode(supportedEncodings);
  }

  @Override
  public String toString() {
    return "AudioCapabilities[maxChannelCount=" + maxChannelCount
        + ", supportedEncodings=" + Arrays.toString(supportedEncodings) + "]";
  }

}
