package xyz.kiurchv.cull;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import xyz.kiurchv.cull.data.HardLinkManager;
import xyz.kiurchv.cull.data.IndexingStore;
import xyz.kiurchv.cull.data.PhotoRepository;
import xyz.kiurchv.cull.data.db.AlbumDao;
import xyz.kiurchv.cull.data.db.CullDatabase;
import xyz.kiurchv.cull.data.db.PhotoHashDao;
import xyz.kiurchv.cull.data.db.PhotoMetadataDao;
import xyz.kiurchv.cull.domain.GroupingEngine;
import xyz.kiurchv.cull.domain.PHashEngine;
import xyz.kiurchv.cull.ui.albums.AlbumContentViewModel;
import xyz.kiurchv.cull.ui.albums.AlbumContentViewModel_HiltModules;
import xyz.kiurchv.cull.ui.albums.AlbumContentViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import xyz.kiurchv.cull.ui.albums.AlbumContentViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import xyz.kiurchv.cull.ui.albums.AlbumsViewModel;
import xyz.kiurchv.cull.ui.albums.AlbumsViewModel_HiltModules;
import xyz.kiurchv.cull.ui.albums.AlbumsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import xyz.kiurchv.cull.ui.albums.AlbumsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import xyz.kiurchv.cull.ui.duplicates.DuplicateGroupViewModel;
import xyz.kiurchv.cull.ui.duplicates.DuplicateGroupViewModel_HiltModules;
import xyz.kiurchv.cull.ui.duplicates.DuplicateGroupViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import xyz.kiurchv.cull.ui.duplicates.DuplicateGroupViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import xyz.kiurchv.cull.ui.gallery.GalleryViewModel;
import xyz.kiurchv.cull.ui.gallery.GalleryViewModel_HiltModules;
import xyz.kiurchv.cull.ui.gallery.GalleryViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import xyz.kiurchv.cull.ui.gallery.GalleryViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import xyz.kiurchv.cull.ui.series.SeriesViewModel;
import xyz.kiurchv.cull.ui.series.SeriesViewModel_HiltModules;
import xyz.kiurchv.cull.ui.series.SeriesViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import xyz.kiurchv.cull.ui.series.SeriesViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import xyz.kiurchv.cull.ui.settings.SettingsViewModel;
import xyz.kiurchv.cull.ui.settings.SettingsViewModel_HiltModules;
import xyz.kiurchv.cull.ui.settings.SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import xyz.kiurchv.cull.ui.settings.SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import xyz.kiurchv.cull.ui.trash.TrashReviewViewModel;
import xyz.kiurchv.cull.ui.trash.TrashReviewViewModel_HiltModules;
import xyz.kiurchv.cull.ui.trash.TrashReviewViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import xyz.kiurchv.cull.ui.trash.TrashReviewViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import xyz.kiurchv.cull.ui.viewer.PhotoViewerViewModel;
import xyz.kiurchv.cull.ui.viewer.PhotoViewerViewModel_HiltModules;
import xyz.kiurchv.cull.ui.viewer.PhotoViewerViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import xyz.kiurchv.cull.ui.viewer.PhotoViewerViewModel_HiltModules_KeyModule_Provide_LazyMapKey;

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
public final class DaggerCullApplication_HiltComponents_SingletonC {
  private DaggerCullApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public CullApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements CullApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public CullApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements CullApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public CullApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements CullApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public CullApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements CullApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public CullApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements CullApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public CullApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements CullApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public CullApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements CullApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public CullApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends CullApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends CullApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    FragmentCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends CullApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends CullApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    ActivityCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(8).put(AlbumContentViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AlbumContentViewModel_HiltModules.KeyModule.provide()).put(AlbumsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AlbumsViewModel_HiltModules.KeyModule.provide()).put(DuplicateGroupViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, DuplicateGroupViewModel_HiltModules.KeyModule.provide()).put(GalleryViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, GalleryViewModel_HiltModules.KeyModule.provide()).put(PhotoViewerViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, PhotoViewerViewModel_HiltModules.KeyModule.provide()).put(SeriesViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SeriesViewModel_HiltModules.KeyModule.provide()).put(SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SettingsViewModel_HiltModules.KeyModule.provide()).put(TrashReviewViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, TrashReviewViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }
  }

  private static final class ViewModelCImpl extends CullApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    Provider<AlbumContentViewModel> albumContentViewModelProvider;

    Provider<AlbumsViewModel> albumsViewModelProvider;

    Provider<DuplicateGroupViewModel> duplicateGroupViewModelProvider;

    Provider<GalleryViewModel> galleryViewModelProvider;

    Provider<PhotoViewerViewModel> photoViewerViewModelProvider;

    Provider<SeriesViewModel> seriesViewModelProvider;

    Provider<SettingsViewModel> settingsViewModelProvider;

    Provider<TrashReviewViewModel> trashReviewViewModelProvider;

    ViewModelCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        SavedStateHandle savedStateHandleParam, ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.albumContentViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.albumsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.duplicateGroupViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.galleryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.photoViewerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.seriesViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.trashReviewViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(8).put(AlbumContentViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (albumContentViewModelProvider))).put(AlbumsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (albumsViewModelProvider))).put(DuplicateGroupViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (duplicateGroupViewModelProvider))).put(GalleryViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (galleryViewModelProvider))).put(PhotoViewerViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (photoViewerViewModelProvider))).put(SeriesViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (seriesViewModelProvider))).put(SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (settingsViewModelProvider))).put(TrashReviewViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (trashReviewViewModelProvider))).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // xyz.kiurchv.cull.ui.albums.AlbumContentViewModel
          return (T) new AlbumContentViewModel(singletonCImpl.albumDao(), singletonCImpl.hardLinkManagerProvider.get(), singletonCImpl.photoRepositoryProvider.get());

          case 1: // xyz.kiurchv.cull.ui.albums.AlbumsViewModel
          return (T) new AlbumsViewModel(singletonCImpl.albumDao(), singletonCImpl.hardLinkManagerProvider.get());

          case 2: // xyz.kiurchv.cull.ui.duplicates.DuplicateGroupViewModel
          return (T) new DuplicateGroupViewModel(singletonCImpl.photoRepositoryProvider.get(), singletonCImpl.photoMetadataDao());

          case 3: // xyz.kiurchv.cull.ui.gallery.GalleryViewModel
          return (T) new GalleryViewModel(singletonCImpl.photoMetadataDao(), singletonCImpl.photoHashDao(), singletonCImpl.photoRepositoryProvider.get(), singletonCImpl.groupingEngineProvider.get(), singletonCImpl.indexingStoreProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 4: // xyz.kiurchv.cull.ui.viewer.PhotoViewerViewModel
          return (T) new PhotoViewerViewModel(singletonCImpl.photoRepositoryProvider.get(), singletonCImpl.photoMetadataDao());

          case 5: // xyz.kiurchv.cull.ui.series.SeriesViewModel
          return (T) new SeriesViewModel(singletonCImpl.photoMetadataDao(), singletonCImpl.photoHashDao(), singletonCImpl.groupingEngineProvider.get(), singletonCImpl.photoRepositoryProvider.get(), singletonCImpl.hardLinkManagerProvider.get(), singletonCImpl.albumDao());

          case 6: // xyz.kiurchv.cull.ui.settings.SettingsViewModel
          return (T) new SettingsViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.indexingStoreProvider.get());

          case 7: // xyz.kiurchv.cull.ui.trash.TrashReviewViewModel
          return (T) new TrashReviewViewModel(singletonCImpl.photoRepositoryProvider.get(), singletonCImpl.photoMetadataDao());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends CullApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends CullApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends CullApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    Provider<CullDatabase> provideDatabaseProvider;

    Provider<HardLinkManager> hardLinkManagerProvider;

    Provider<PhotoRepository> photoRepositoryProvider;

    Provider<PHashEngine> pHashEngineProvider;

    Provider<IndexingStore> indexingStoreProvider;

    Provider<GroupingEngine> groupingEngineProvider;

    SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    AlbumDao albumDao() {
      return AppModule_ProvideAlbumDaoFactory.provideAlbumDao(provideDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<CullDatabase>(singletonCImpl, 1));
      this.hardLinkManagerProvider = DoubleCheck.provider(new SwitchingProvider<HardLinkManager>(singletonCImpl, 2));
      this.photoRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<PhotoRepository>(singletonCImpl, 0));
      this.pHashEngineProvider = DoubleCheck.provider(new SwitchingProvider<PHashEngine>(singletonCImpl, 3));
      this.indexingStoreProvider = DoubleCheck.provider(new SwitchingProvider<IndexingStore>(singletonCImpl, 4));
      this.groupingEngineProvider = DoubleCheck.provider(new SwitchingProvider<GroupingEngine>(singletonCImpl, 5));
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @Override
    public void injectCullApplication(CullApplication cullApplication) {
    }

    @Override
    public PhotoRepository photoRepository() {
      return photoRepositoryProvider.get();
    }

    @Override
    public PhotoMetadataDao photoMetadataDao() {
      return AppModule_ProvidePhotoMetadataDaoFactory.providePhotoMetadataDao(provideDatabaseProvider.get());
    }

    @Override
    public PhotoHashDao photoHashDao() {
      return AppModule_ProvidePhotoHashDaoFactory.providePhotoHashDao(provideDatabaseProvider.get());
    }

    @Override
    public PHashEngine pHashEngine() {
      return pHashEngineProvider.get();
    }

    @Override
    public IndexingStore indexingStore() {
      return indexingStoreProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // xyz.kiurchv.cull.data.PhotoRepository
          return (T) new PhotoRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideDatabaseProvider.get(), singletonCImpl.hardLinkManagerProvider.get());

          case 1: // xyz.kiurchv.cull.data.db.CullDatabase
          return (T) AppModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // xyz.kiurchv.cull.data.HardLinkManager
          return (T) new HardLinkManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.albumDao());

          case 3: // xyz.kiurchv.cull.domain.PHashEngine
          return (T) new PHashEngine(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 4: // xyz.kiurchv.cull.data.IndexingStore
          return (T) new IndexingStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 5: // xyz.kiurchv.cull.domain.GroupingEngine
          return (T) new GroupingEngine();

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
