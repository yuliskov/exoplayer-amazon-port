/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.google.android.exoplayer2.source.smoothstreaming.offline;

import android.net.Uri;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.testutil.FakeDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Unit test to verify creation of a SmoothStreaming {@link DownloadHelper}. */
@RunWith(RobolectricTestRunner.class)
public final class DownloadHelperTest {

  @Test
  public void staticDownloadHelperForSmoothStreaming_doesNotThrow() {
    DownloadHelper.forSmoothStreaming(
        Uri.parse("http://uri"),
        new FakeDataSource.Factory(),
        (handler, videoListener, audioListener, text, metadata, drm) -> new Renderer[0]);
    DownloadHelper.forSmoothStreaming(
        Uri.parse("http://uri"),
        new FakeDataSource.Factory(),
        (handler, videoListener, audioListener, text, metadata, drm) -> new Renderer[0],
        /* drmSessionManager= */ null,
        DownloadHelper.DEFAULT_TRACK_SELECTOR_PARAMETERS);
  }
}
