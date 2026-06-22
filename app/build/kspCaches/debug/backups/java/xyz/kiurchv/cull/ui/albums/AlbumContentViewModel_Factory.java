package xyz.kiurchv.cull.ui.albums;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import xyz.kiurchv.cull.data.HardLinkManager;
import xyz.kiurchv.cull.data.PhotoRepository;
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
public final class AlbumContentViewModel_Factory implements Factory<AlbumContentViewModel> {
  private final Provider<AlbumDao> albumDaoProvider;

  private final Provider<HardLinkManager> hardLinkManagerProvider;

  private final Provider<PhotoRepository> photoRepositoryProvider;

  private AlbumContentViewModel_Factory(Provider<AlbumDao> albumDaoProvider,
      Provider<HardLinkManager> hardLinkManagerProvider,
      Provider<PhotoRepository> photoRepositoryProvider) {
    this.albumDaoProvider = albumDaoProvider;
    this.hardLinkManagerProvider = hardLinkManagerProvider;
    this.photoRepositoryProvider = photoRepositoryProvider;
  }

  @Override
  public AlbumContentViewModel get() {
    return newInstance(albumDaoProvider.get(), hardLinkManagerProvider.get(), photoRepositoryProvider.get());
  }

  public static AlbumContentViewModel_Factory create(Provider<AlbumDao> albumDaoProvider,
      Provider<HardLinkManager> hardLinkManagerProvider,
      Provider<PhotoRepository> photoRepositoryProvider) {
    return new AlbumContentViewModel_Factory(albumDaoProvider, hardLinkManagerProvider, photoRepositoryProvider);
  }

  public static AlbumContentViewModel newInstance(AlbumDao albumDao,
      HardLinkManager hardLinkManager, PhotoRepository photoRepository) {
    return new AlbumContentViewModel(albumDao, hardLinkManager, photoRepository);
  }
}
