/*
 * Copyright (C) 2020 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.tools.build.bundletool.model.utils;

import static com.google.common.truth.Truth.assertThat;

import com.android.tools.build.bundletool.model.ConfigurationSizes;
import com.android.tools.build.bundletool.model.SizeConfiguration;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class ConfigurationSizesMergerTest {

  @Test
  public void merge_defaultConfiguration() throws Exception {
    ConfigurationSizes configurationSizes1 =
        ConfigurationSizes.create(
            ImmutableMap.of(SizeConfiguration.getDefaultInstance(), 10L),
            ImmutableMap.of(SizeConfiguration.getDefaultInstance(), 20L));
    ConfigurationSizes configurationSizes2 =
        ConfigurationSizes.create(
            ImmutableMap.of(SizeConfiguration.getDefaultInstance(), 30L),
            ImmutableMap.of(SizeConfiguration.getDefaultInstance(), 40L));
    ConfigurationSizes mergedConfigurationSizes =
        ConfigurationSizesMerger.merge(configurationSizes1, configurationSizes2);
    assertThat(mergedConfigurationSizes.getMinSizeConfigurationMap())
        .containsExactly(SizeConfiguration.getDefaultInstance(), 40L);
    assertThat(mergedConfigurationSizes.getMaxSizeConfigurationMap())
        .containsExactly(SizeConfiguration.getDefaultInstance(), 60L);
  }

  @Test
  public void merge_withEmptyConfiguration() throws Exception {
    ConfigurationSizes configurationSizes1 =
        ConfigurationSizes.create(
            ImmutableMap.of(SizeConfiguration.builder().setAbi("x86").build(), 10L),
            ImmutableMap.of(SizeConfiguration.builder().setAbi("x86").build(), 20L));
    ConfigurationSizes configurationSizes2 =
        ConfigurationSizes.create(
            ImmutableMap.of(SizeConfiguration.getDefaultInstance(), 0L),
            ImmutableMap.of(SizeConfiguration.getDefaultInstance(), 0L));

    ConfigurationSizes mergedConfigurationSizes =
        ConfigurationSizesMerger.merge(configurationSizes1, configurationSizes2);

    assertThat(mergedConfigurationSizes.getMinSizeConfigurationMap())
        .isEqualTo(configurationSizes1.getMinSizeConfigurationMap());
    assertThat(mergedConfigurationSizes.getMaxSizeConfigurationMap())
        .isEqualTo(configurationSizes1.getMaxSizeConfigurationMap());
  }

  @Test
  public void merge_withSubsetOfConfiguration() throws Exception {
    ConfigurationSizes configurationSizes1 =
        ConfigurationSizes.create(
            ImmutableMap.of(
                SizeConfiguration.builder().setAbi("x86").setSdkVersion("21-").build(), 10L,
                SizeConfiguration.builder().setAbi("x86_64").setSdkVersion("21-").build(), 15L),
            ImmutableMap.of(
                SizeConfiguration.builder().setAbi("x86").setSdkVersion("21-").build(), 20L,
                SizeConfiguration.builder().setAbi("x86_64").setSdkVersion("21-").build(), 25L));
    ConfigurationSizes configurationSizes2 =
        ConfigurationSizes.create(
            ImmutableMap.of(SizeConfiguration.builder().setSdkVersion("21-").build(), 0L),
            ImmutableMap.of(SizeConfiguration.builder().setSdkVersion("21-").build(), 0L));

    ConfigurationSizes mergedConfigurationSizes =
        ConfigurationSizesMerger.merge(configurationSizes1, configurationSizes2);

    assertThat(mergedConfigurationSizes.getMinSizeConfigurationMap())
        .isEqualTo(configurationSizes1.getMinSizeConfigurationMap());
    assertThat(mergedConfigurationSizes.getMaxSizeConfigurationMap())
        .isEqualTo(configurationSizes1.getMaxSizeConfigurationMap());
  }

  @Test
  public void merge_disjointDimensions() throws Exception {
    final long config1X86Min = 1 << 0;
    final long config1MipsMin = 1 << 1;
    final long config1X86Max = 1 << 2;
    final long config1MipsMax = 1 << 3;
    final long config2HdpiMin = 1 << 4;
    final long config2XhdpiMin = 1 << 5;
    final long config2HdpiMax = 1 << 6;
    final long config2XhdpiMax = 1 << 7;
    ConfigurationSizes configurationSizes1 =
        ConfigurationSizes.create(
            ImmutableMap.of(
                SizeConfiguration.builder().setAbi("x86").setSdkVersion("21-").build(),
                config1X86Min,
                SizeConfiguration.builder().setAbi("mips").setSdkVersion("21-").build(),
                config1MipsMin),
            ImmutableMap.of(
                SizeConfiguration.builder().setAbi("x86").setSdkVersion("21-").build(),
                config1X86Max,
                SizeConfiguration.builder().setAbi("mips").setSdkVersion("21-").build(),
                config1MipsMax));
    ConfigurationSizes configurationSizes2 =
        ConfigurationSizes.create(
            ImmutableMap.of(
                SizeConfiguration.builder().setScreenDensity("hdpi").setSdkVersion("21-").build(),
                config2HdpiMin,
                SizeConfiguration.builder().setScreenDensity("xhdpi").setSdkVersion("21-").build(),
                config2XhdpiMin),
            ImmutableMap.of(
                SizeConfiguration.builder().setScreenDensity("hdpi").setSdkVersion("21-").build(),
                config2HdpiMax,
                SizeConfiguration.builder().setScreenDensity("xhdpi").setSdkVersion("21-").build(),
                config2XhdpiMax));

    // All combinations of ABIs and screen densities should be generated.
    ConfigurationSizes expectedMergedConfigurationSizes =
        ConfigurationSizes.create(
            ImmutableMap.of(
                SizeConfiguration.builder()
                    .setAbi("x86")
                    .setScreenDensity("hdpi")
                    .setSdkVersion("21-")
                    .build(),
                config1X86Min + config2HdpiMin,
                SizeConfiguration.builder()
                    .setAbi("mips")
                    .setScreenDensity("hdpi")
                    .setSdkVersion("21-")
                    .build(),
                config1MipsMin + config2HdpiMin,
                SizeConfiguration.builder()
                    .setAbi("x86")
                    .setScreenDensity("xhdpi")
                    .setSdkVersion("21-")
                    .build(),
                config1X86Min + config2XhdpiMin,
                SizeConfiguration.builder()
                    .setAbi("mips")
                    .setScreenDensity("xhdpi")
                    .setSdkVersion("21-")
                    .build(),
                config1MipsMin + config2XhdpiMin),
            ImmutableMap.of(
                SizeConfiguration.builder()
                    .setAbi("x86")
                    .setScreenDensity("hdpi")
                    .setSdkVersion("21-")
                    .build(),
                config1X86Max + config2HdpiMax,
                SizeConfiguration.builder()
                    .setAbi("mips")
                    .setScreenDensity("hdpi")
                    .setSdkVersion("21-")
                    .build(),
                config1MipsMax + config2HdpiMax,
                SizeConfiguration.builder()
                    .setAbi("x86")
                    .setScreenDensity("xhdpi")
                    .setSdkVersion("21-")
                    .build(),
                config1X86Max + config2XhdpiMax,
                SizeConfiguration.builder()
                    .setAbi("mips")
                    .setScreenDensity("xhdpi")
                    .setSdkVersion("21-")
                    .build(),
                config1MipsMax + config2XhdpiMax));

    ConfigurationSizes actualMergedConfigurationSizes =
        ConfigurationSizesMerger.merge(configurationSizes1, configurationSizes2);

    assertThat(actualMergedConfigurationSizes.getMinSizeConfigurationMap())
        .isEqualTo(expectedMergedConfigurationSizes.getMinSizeConfigurationMap());
    assertThat(actualMergedConfigurationSizes.getMaxSizeConfigurationMap())
        .isEqualTo(expectedMergedConfigurationSizes.getMaxSizeConfigurationMap());
  }
}
