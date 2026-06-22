package xyz.kiurchv.cull.domain;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class PHashEngine_Factory implements Factory<PHashEngine> {
  private final Provider<Context> contextProvider;

  private PHashEngine_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PHashEngine get() {
    return newInstance(contextProvider.get());
  }

  public static PHashEngine_Factory create(Provider<Context> contextProvider) {
    return new PHashEngine_Factory(contextProvider);
  }

  public static PHashEngine newInstance(Context context) {
    return new PHashEngine(context);
  }
}
