package xyz.kiurchv.cull;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import xyz.kiurchv.cull.data.db.CullDatabase;
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
public final class AppModule_ProvidePhotoMetadataDaoFactory implements Factory<PhotoMetadataDao> {
  private final Provider<CullDatabase> dbProvider;

  private AppModule_ProvidePhotoMetadataDaoFactory(Provider<CullDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PhotoMetadataDao get() {
    return providePhotoMetadataDao(dbProvider.get());
  }

  public static AppModule_ProvidePhotoMetadataDaoFactory create(Provider<CullDatabase> dbProvider) {
    return new AppModule_ProvidePhotoMetadataDaoFactory(dbProvider);
  }

  public static PhotoMetadataDao providePhotoMetadataDao(CullDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePhotoMetadataDao(db));
  }
}
