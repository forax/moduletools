package fr.umlv.moduletools.api;

import java.io.UncheckedIOException;
import java.lang.module.ModuleDescriptor;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

@FunctionalInterface
public interface ModuleInput {
  Optional<ModuleDescriptor> read(String name) throws UncheckedIOException;
  
  default ModuleInput or(ModuleInput input) {
    return name -> read(name).or(() -> input.read(name));
  }
  
  static ModuleInput availableInputs() {
    ServiceLoader<ModuleInput> loader = ServiceLoader.load(ModuleInput.class, ModuleInput.class.getClassLoader());
    return StreamSupport.stream(loader.spliterator(), false)
        .reduce(__ -> Optional.empty(), ModuleInput::or);
  }
}

