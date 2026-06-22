package xyz.kiurchv.cull.ui.series;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import xyz.kiurchv.cull.data.HardLinkManager;
import xyz.kiurchv.cull.data.PhotoRepository;
import xyz.kiurchv.cull.data.db.AlbumDao;
import xyz.kiurchv.cull.data.db.PhotoHashDao;
import xyz.kiurchv.cull.data.db.PhotoMetadataDao;
import xyz.kiurchv.cull.domain.GroupingEngine;

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
public final class SeriesViewModel_Factory implements Factory<SeriesViewModel> {
  private final Provider<PhotoMetadataDao> photoMetadataDaoProvider;

  private final Provider<PhotoHashDao> photoHashDaoProvider;

  private final Provider<GroupingEngine> groupingEngineProvider;

  private final Provider<PhotoRepository> photoRepositoryProvider;

  private final Provider<HardLinkManager> hardLinkManagerProvider;

  private final Provider<AlbumDao> albumDaoProvider;

  private SeriesViewModel_Factory(Provider<PhotoMetadataDao> photoMetadataDaoProvider,
      Provider<PhotoHashDao> photoHashDaoProvider, Provider<GroupingEngine> groupingEngineProvider,
      Provider<PhotoRepository> photoRepositoryProvider,
      Provider<HardLinkManager> hardLinkManagerProvider, Provider<AlbumDao> albumDaoProvider) {
    this.photoMetadataDaoProvider = photoMetadataDaoProvider;
    this.photoHashDaoProvider = photoHashDaoProvider;
    this.groupingEngineProvider = groupingEngineProvider;
    this.photoRepositoryProvider = photoRepositoryProvider;
    this.hardLinkManagerProvider = hardLinkManagerProvider;
    this.albumDaoProvider = albumDaoProvider;
  }

  @Override
  public SeriesViewModel get() {
    return newInstance(photoMetadataDaoProvider.get(), photoHashDaoProvider.get(), groupingEngineProvider.get(), photoRepositoryProvider.get(), hardLinkManagerProvider.get(), albumDaoProvider.get());
  }

  public static SeriesViewModel_Factory create(Provider<PhotoMetadataDao> photoMetadataDaoProvider,
      Provider<PhotoHashDao> photoHashDaoProvider, Provider<GroupingEngine> groupingEngineProvider,
      Provider<PhotoRepository> photoRepositoryProvider,
      Provider<HardLinkManager> hardLinkManagerProvider, Provider<AlbumDao> albumDaoProvider) {
    return new SeriesViewModel_Factory(photoMetadataDaoProvider, photoHashDaoProvider, groupingEngineProvider, photoRepositoryProvider, hardLinkManagerProvider, albumDaoProvider);
  }

  public static SeriesViewModel newInstance(PhotoMetadataDao photoMetadataDao,
      PhotoHashDao photoHashDao, GroupingEngine groupingEngine, PhotoRepository photoRepository,
      HardLinkManager hardLinkManager, AlbumDao albumDao) {
    return new SeriesViewModel(photoMetadataDao, photoHashDao, groupingEngine, photoRepository, hardLinkManager, albumDao);
  }
}
