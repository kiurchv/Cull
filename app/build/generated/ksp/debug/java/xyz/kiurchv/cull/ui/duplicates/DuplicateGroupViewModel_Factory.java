package xyz.kiurchv.cull.ui.duplicates;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import xyz.kiurchv.cull.data.PhotoRepository;
import xyz.kiurchv.cull.data.db.PhotoMetadataDao;

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
public final class DuplicateGroupViewModel_Factory implements Factory<DuplicateGroupViewModel> {
  private final Provider<PhotoRepository> photoRepositoryProvider;

  private final Provider<PhotoMetadataDao> photoMetadataDaoProvider;

  private DuplicateGroupViewModel_Factory(Provider<PhotoRepository> photoRepositoryProvider,
      Provider<PhotoMetadataDao> photoMetadataDaoProvider) {
    this.photoRepositoryProvider = photoRepositoryProvider;
    this.photoMetadataDaoProvider = photoMetadataDaoProvider;
  }

  @Override
  public DuplicateGroupViewModel get() {
    return newInstance(photoRepositoryProvider.get(), photoMetadataDaoProvider.get());
  }

  public static DuplicateGroupViewModel_Factory create(
      Provider<PhotoRepository> photoRepositoryProvider,
      Provider<PhotoMetadataDao> photoMetadataDaoProvider) {
    return new DuplicateGroupViewModel_Factory(photoRepositoryProvider, photoMetadataDaoProvider);
  }

  public static DuplicateGroupViewModel newInstance(PhotoRepository photoRepository,
      PhotoMetadataDao photoMetadataDao) {
    return new DuplicateGroupViewModel(photoRepository, photoMetadataDao);
  }
}
