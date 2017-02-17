package fr.umlv.moduletools.api;

import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.lang.module.ModuleDescriptor;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import fr.umlv.moduletools.api.internal.ModuleHelper;

@FunctionalInterface
public interface ModuleOutput {
  enum Result { OK }
  
  Optional<Result> write(String name, ModuleDescriptor moduleDescriptor) throws UncheckedIOException;
  
  default ModuleOutput or(ModuleOutput output) {
    return (name, desc) -> write(name, desc).or(() -> output.write(name, desc));
  }
  
  static ModuleOutput availableOutputs() {
    ServiceLoader<ModuleOutput> loader = ServiceLoader.load(ModuleOutput.class, ModuleOutput.class.getClassLoader());
    return StreamSupport.stream(loader.spliterator(), false)
        .reduce((_1, _2) -> Optional.empty(), ModuleOutput::or);
  }
  
  static ModuleOutput createPrintModuleOutput(PrintStream out) {
    return (name, desc) -> {
      out.println(ModuleHelper.moduleDescriptorToSource(desc));
      return Optional.of(Result.OK);
    };
  }
}