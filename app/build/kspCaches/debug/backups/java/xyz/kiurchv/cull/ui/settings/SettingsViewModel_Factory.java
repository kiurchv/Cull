package xyz.kiurchv.cull.ui.settings;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import xyz.kiurchv.cull.data.IndexingStore;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<IndexingStore> indexingStoreProvider;

  private SettingsViewModel_Factory(Provider<Context> contextProvider,
      Provider<IndexingStore> indexingStoreProvider) {
    this.contextProvider = contextProvider;
    this.indexingStoreProvider = indexingStoreProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(contextProvider.get(), indexingStoreProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<Context> contextProvider,
      Provider<IndexingStore> indexingStoreProvider) {
    return new SettingsViewModel_Factory(contextProvider, indexingStoreProvider);
  }

  public static SettingsViewModel newInstance(Context context, IndexingStore indexingStore) {
    return new SettingsViewModel(context, indexingStore);
  }
}
