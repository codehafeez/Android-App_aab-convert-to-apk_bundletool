/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.tools.build.bundletool.model;

import com.android.tools.build.bundletool.model.BundleModule.SpecialModuleEntry;
import com.google.auto.value.AutoValue;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.io.MoreFiles;
import com.google.errorprone.annotations.Immutable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents an entry in an App Bundle's module.
 *
 * <p>All subclasses should be immutable, and we assume that they are as long as the data source
 * backing this entry remains unchanged.
 */
@Immutable
@AutoValue
@AutoValue.CopyAnnotations
@SuppressWarnings("Immutable")
public abstract class ModuleEntry {

  /** Path of the entry inside the module. */
  public abstract ZipPath getPath();

  /** Returns whether entry should always be left uncompressed in generated archives. */
  public abstract boolean getForceUncompressed();

  /** Returns whether entry is an embedded APK that should be signed by the output APK key. */
  public abstract boolean getShouldSign();

  /** Returns data source for this entry. */
  public abstract ByteSource getContent();

  /** @deprecated Use {@link #getContent()} instead. */
  @Deprecated
  public InputStreamSupplier getContentSupplier() {
    return InputStreamSupplier.fromByteSource(getContent());
  }

  /** @deprecated Use {@link #getContent()} instead. */
  @Deprecated
  public ByteSource asByteSource() {
    return getContent();
  }

  /** Checks whether the given entries are identical. */
  @Override
  public final boolean equals(Object obj2) {
    if (!(obj2 instanceof ModuleEntry)) {
      return false;
    }

    ModuleEntry entry1 = this;
    ModuleEntry entry2 = (ModuleEntry) obj2;

    if (entry1 == entry2) {
      return true;
    }

    if (!entry1.getPath().equals(entry2.getPath())) {
      return false;
    }

    if (entry1.getForceUncompressed() != entry2.getForceUncompressed()) {
      return false;
    }

    if (entry1.getShouldSign() != entry2.getShouldSign()) {
      return false;
    }

    try {
      return entry1.getContent().contentEquals(entry2.getContent());
    } catch (IOException e) {
      throw new UncheckedIOException(
          String.format(
              "Failed to compare contents of module entries '%s' and '%s'.", entry1, entry2),
          e);
    }
  }

  @Override
  public final int hashCode() {
    // Deliberately omit the content for performance.
    return Objects.hash(getPath(), getForceUncompressed());
  }

  public boolean isSpecialEntry() {
    return SpecialModuleEntry.getSpecialEntry(getPath()).isPresent();
  }

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_ModuleEntry.Builder().setForceUncompressed(false).setShouldSign(false);
  }

  /** Builder for {@code ModuleEntry}. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setPath(ZipPath path);

    public abstract Builder setForceUncompressed(boolean forcedUncompressed);

    public abstract Builder setShouldSign(boolean shouldSign);

    public abstract Builder setContent(ByteSource content);

    public Builder setContent(Path path) {
      return setContent(MoreFiles.asByteSource(path));
    }

    public Builder setContent(File file) {
      return setContent(Files.asByteSource(file));
    }

    /** @deprecated Use {@link #setContent(ByteSource)} instead. */
    @Deprecated
    public Builder setContentSupplier(InputStreamSupplier contentSupplier) {
      return setContent(contentSupplier.asByteSource());
    }

    public abstract ModuleEntry build();
  }
}
