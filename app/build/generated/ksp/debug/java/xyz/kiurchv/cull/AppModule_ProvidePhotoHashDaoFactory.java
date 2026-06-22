package xyz.kiurchv.cull;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import xyz.kiurchv.cull.data.db.CullDatabase;
import xyz.kiurchv.cull.data.db.PhotoHashDao;

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
public final class AppModule_ProvidePhotoHashDaoFactory implements Factory<PhotoHashDao> {
  private final Provider<CullDatabase> dbProvider;

  private AppModule_ProvidePhotoHashDaoFactory(Provider<CullDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PhotoHashDao get() {
    return providePhotoHashDao(dbProvider.get());
  }

  public static AppModule_ProvidePhotoHashDaoFactory create(Provider<CullDatabase> dbProvider) {
    return new AppModule_ProvidePhotoHashDaoFactory(dbProvider);
  }

  public static PhotoHashDao providePhotoHashDao(CullDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePhotoHashDao(db));
  }
}
