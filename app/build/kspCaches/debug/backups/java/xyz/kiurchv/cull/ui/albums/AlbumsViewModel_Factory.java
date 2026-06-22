package xyz.kiurchv.cull.ui.albums;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import xyz.kiurchv.cull.data.HardLinkManager;
import xyz.kiurchv.cull.data.db.AlbumDao;

@ScopeMetadata
@QualifierMetadata
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
public final class AlbumsViewModel_Factory implements Factory<AlbumsViewModel> {
  private final Provider<AlbumDao> albumDaoProvider;

  private final Provider<HardLinkManager> hardLinkManagerProvider;

  private AlbumsViewModel_Factory(Provider<AlbumDao> albumDaoProvider,
      Provider<HardLinkManager> hardLinkManagerProvider) {
    this.albumDaoProvider = albumDaoProvider;
    this.hardLinkManagerProvider = hardLinkManagerProvider;
  }

  @Override
  public AlbumsViewModel get() {
    return newInstance(albumDaoProvider.get(), hardLinkManagerProvider.get());
  }

  public static AlbumsViewModel_Factory create(Provider<AlbumDao> albumDaoProvider,
      Provider<HardLinkManager> hardLinkManagerProvider) {
    return new AlbumsViewModel_Factory(albumDaoProvider, hardLinkManagerProvider);
  }

  public static AlbumsViewModel newInstance(AlbumDao albumDao, HardLinkManager hardLinkManager) {
    return new AlbumsViewModel(albumDao, hardLinkManager);
  }
}
