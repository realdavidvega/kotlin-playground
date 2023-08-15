import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider

val Provider<MinimalExternalModuleDependency>.asString: String
  get() = get().run { "${module.group}:${module.name}:${versionConstraint}" }
