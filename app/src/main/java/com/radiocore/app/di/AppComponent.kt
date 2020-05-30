import com.radiocore.app.RadioCoreApp
import com.radiocore.app.di.modules.*
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class,
    AppModule::class,
    ActivityModule::class,
    ServiceModule::class,
    ViewModelModule::class,
    FragmentModule::class])
interface AppComponent : AndroidInjector<RadioCoreApp> {

    @Component.Factory
    abstract class Factory : AndroidInjector.Factory<RadioCoreApp>

}
