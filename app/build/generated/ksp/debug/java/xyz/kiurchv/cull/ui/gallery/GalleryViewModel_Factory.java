package xyz.kiurchv.cull.ui.gallery;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import xyz.kiurchv.cull.data.IndexingStore;
import xyz.kiurchv.cull.data.PhotoRepository;
import xyz.kiurchv.cull.data.db.PhotoHashDao;
import xyz.kiurchv.cull.data.db.PhotoMetadataDao;
import xyz.kiurchv.cull.domain.GroupingEngine;

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
public final class GalleryViewModel_Factory implements Factory<GalleryViewModel> {
  private final Provider<PhotoMetadataDao> photoMetadataDaoProvider;

  private final Provider<PhotoHashDao> photoHashDaoProvider;

  private final Provider<PhotoRepository> photoRepositoryProvider;

  private final Provider<GroupingEngine> groupingEngineProvider;

  private final Provider<IndexingStore> indexingStoreProvider;

  private final Provider<Context> contextProvider;

  private GalleryViewModel_Factory(Provider<PhotoMetadataDao> photoMetadataDaoProvider,
      Provider<PhotoHashDao> photoHashDaoProvider,
      Provider<PhotoRepository> photoRepositoryProvider,
      Provider<GroupingEngine> groupingEngineProvider,
      Provider<IndexingStore> indexingStoreProvider, Provider<Context> contextProvider) {
    this.photoMetadataDaoProvider = photoMetadataDaoProvider;
    this.photoHashDaoProvider = photoHashDaoProvider;
    this.photoRepositoryProvider = photoRepositoryProvider;
    this.groupingEngineProvider = groupingEngineProvider;
    this.indexingStoreProvider = indexingStoreProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public GalleryViewModel get() {
    return newInstance(photoMetadataDaoProvider.get(), photoHashDaoProvider.get(), photoRepositoryProvider.get(), groupingEngineProvider.get(), indexingStoreProvider.get(), contextProvider.get());
  }

  public static GalleryViewModel_Factory create(Provider<PhotoMetadataDao> photoMetadataDaoProvider,
      Provider<PhotoHashDao> photoHashDaoProvider,
      Provider<PhotoRepository> photoRepositoryProvider,
      Provider<GroupingEngine> groupingEngineProvider,
      Provider<IndexingStore> indexingStoreProvider, Provider<Context> contextProvider) {
    return new GalleryViewModel_Factory(photoMetadataDaoProvider, photoHashDaoProvider, photoRepositoryProvider, groupingEngineProvider, indexingStoreProvider, contextProvider);
  }

  public static GalleryViewModel newInstance(PhotoMetadataDao photoMetadataDao,
      PhotoHashDao photoHashDao, PhotoRepository photoRepository, GroupingEngine groupingEngine,
      IndexingStore indexingStore, Context context) {
    return new GalleryViewModel(photoMetadataDao, photoHashDao, photoRepository, groupingEngine, indexingStore, context);
  }
}
