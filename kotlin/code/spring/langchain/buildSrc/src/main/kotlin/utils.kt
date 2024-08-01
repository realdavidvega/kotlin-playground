import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider;
import org.gradle.jvm.toolchain.JavaLanguageVersion

val Provider<String>.version: String
  get() = get()

operator fun Property<JavaLanguageVersion>.invoke(version: String) =
  set(JavaLanguageVersion.of(version))
