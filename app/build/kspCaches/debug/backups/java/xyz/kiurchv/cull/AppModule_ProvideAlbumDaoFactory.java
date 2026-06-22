package xyz.kiurchv.cull;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import xyz.kiurchv.cull.data.db.AlbumDao;
import xyz.kiurchv.cull.data.db.CullDatabase;

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
public final class AppModule_ProvideAlbumDaoFactory implements Factory<AlbumDao> {
  private final Provider<CullDatabase> dbProvider;

  private AppModule_ProvideAlbumDaoFactory(Provider<CullDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AlbumDao get() {
    return provideAlbumDao(dbProvider.get());
  }

  public static AppModule_ProvideAlbumDaoFactory create(Provider<CullDatabase> dbProvider) {
    return new AppModule_ProvideAlbumDaoFactory(dbProvider);
  }

  public static AlbumDao provideAlbumDao(CullDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAlbumDao(db));
  }
}
