import static com.github.forax.pro.Pro.*;
import static com.github.forax.pro.builder.Builders.*;

pro.loglevel("verbose")

compiler.
  sourceRelease(9)

packager.
  modules(
    "moduletools.api@1.0",
    "moduletools.service@1.0",
    "moduletools.impl@1.0",
    "moduletools.main@1.0/fr.umlv.moduletools.main.Main" )

linker.
  rootModules(
    "moduletools.main",
    "moduletools.service"
  ).
  launchers("moduletools=moduletools.main")

run(compiler, packager, linker)

/exit errorCode()
