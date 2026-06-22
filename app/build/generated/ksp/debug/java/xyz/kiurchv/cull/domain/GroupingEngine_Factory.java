package xyz.kiurchv.cull.domain;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class GroupingEngine_Factory implements Factory<GroupingEngine> {
  @Override
  public GroupingEngine get() {
    return newInstance();
  }

  public static GroupingEngine_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static GroupingEngine newInstance() {
    return new GroupingEngine();
  }

  private static final class InstanceHolder {
    static final GroupingEngine_Factory INSTANCE = new GroupingEngine_Factory();
  }
}
