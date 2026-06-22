package xyz.kiurchv.cull.ui.trash;

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
public final class TrashReviewViewModel_Factory implements Factory<TrashReviewViewModel> {
  private final Provider<PhotoRepository> photoRepositoryProvider;

  private final Provider<PhotoMetadataDao> photoMetadataDaoProvider;

  private TrashReviewViewModel_Factory(Provider<PhotoRepository> photoRepositoryProvider,
      Provider<PhotoMetadataDao> photoMetadataDaoProvider) {
    this.photoRepositoryProvider = photoRepositoryProvider;
    this.photoMetadataDaoProvider = photoMetadataDaoProvider;
  }

  @Override
  public TrashReviewViewModel get() {
    return newInstance(photoRepositoryProvider.get(), photoMetadataDaoProvider.get());
  }

  public static TrashReviewViewModel_Factory create(
      Provider<PhotoRepository> photoRepositoryProvider,
      Provider<PhotoMetadataDao> photoMetadataDaoProvider) {
    return new TrashReviewViewModel_Factory(photoRepositoryProvider, photoMetadataDaoProvider);
  }

  public static TrashReviewViewModel newInstance(PhotoRepository photoRepository,
      PhotoMetadataDao photoMetadataDao) {
    return new TrashReviewViewModel(photoRepository, photoMetadataDao);
  }
}
