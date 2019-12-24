import com.radiocore.app.RadioCoreApp
import com.radiocore.app.di.ActivityBuilderModule
import com.radiocore.app.di.AppModule
import com.radiocore.app.di.FragmentBuilderModule
import com.radiocore.app.di.ServiceBuilderModule
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class,
    AppModule::class,
    ActivityBuilderModule::class, ServiceBuilderModule::class, FragmentBuilderModule::class])
interface AppComponent : AndroidInjector<RadioCoreApp> {

    @Component.Factory
    abstract class Factory : AndroidInjector.Factory<RadioCoreApp>

}
