package xyz.kiurchv.cull.data;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import xyz.kiurchv.cull.data.db.AlbumDao;

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
public final class HardLinkManager_Factory implements Factory<HardLinkManager> {
  private final Provider<Context> contextProvider;

  private final Provider<AlbumDao> albumDaoProvider;

  private HardLinkManager_Factory(Provider<Context> contextProvider,
      Provider<AlbumDao> albumDaoProvider) {
    this.contextProvider = contextProvider;
    this.albumDaoProvider = albumDaoProvider;
  }

  @Override
  public HardLinkManager get() {
    return newInstance(contextProvider.get(), albumDaoProvider.get());
  }

  public static HardLinkManager_Factory create(Provider<Context> contextProvider,
      Provider<AlbumDao> albumDaoProvider) {
    return new HardLinkManager_Factory(contextProvider, albumDaoProvider);
  }

  public static HardLinkManager newInstance(Context context, AlbumDao albumDao) {
    return new HardLinkManager(context, albumDao);
  }
}
