package xyz.kiurchv.cull.data;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import xyz.kiurchv.cull.data.db.CullDatabase;

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
public final class PhotoRepository_Factory implements Factory<PhotoRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<CullDatabase> dbProvider;

  private final Provider<HardLinkManager> hardLinkManagerProvider;

  private PhotoRepository_Factory(Provider<Context> contextProvider,
      Provider<CullDatabase> dbProvider, Provider<HardLinkManager> hardLinkManagerProvider) {
    this.contextProvider = contextProvider;
    this.dbProvider = dbProvider;
    this.hardLinkManagerProvider = hardLinkManagerProvider;
  }

  @Override
  public PhotoRepository get() {
    return newInstance(contextProvider.get(), dbProvider.get(), hardLinkManagerProvider.get());
  }

  public static PhotoRepository_Factory create(Provider<Context> contextProvider,
      Provider<CullDatabase> dbProvider, Provider<HardLinkManager> hardLinkManagerProvider) {
    return new PhotoRepository_Factory(contextProvider, dbProvider, hardLinkManagerProvider);
  }

  public static PhotoRepository newInstance(Context context, CullDatabase db,
      HardLinkManager hardLinkManager) {
    return new PhotoRepository(context, db, hardLinkManager);
  }
}
