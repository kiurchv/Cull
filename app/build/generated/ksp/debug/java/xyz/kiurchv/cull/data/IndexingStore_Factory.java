package xyz.kiurchv.cull.data;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class IndexingStore_Factory implements Factory<IndexingStore> {
  private final Provider<Context> contextProvider;

  private IndexingStore_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public IndexingStore get() {
    return newInstance(contextProvider.get());
  }

  public static IndexingStore_Factory create(Provider<Context> contextProvider) {
    return new IndexingStore_Factory(contextProvider);
  }

  public static IndexingStore newInstance(Context context) {
    return new IndexingStore(context);
  }
}
