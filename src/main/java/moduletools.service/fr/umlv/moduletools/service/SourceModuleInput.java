package fr.umlv.moduletools.service;

import java.io.UncheckedIOException;
import java.lang.module.ModuleDescriptor;
import java.nio.file.Paths;
import java.util.Optional;

import fr.umlv.moduletools.api.ModuleInput;
import fr.umlv.moduletools.api.internal.ModuleHelper;

public class SourceModuleInput implements ModuleInput {
  @Override
  public Optional<ModuleDescriptor> read(String name) throws UncheckedIOException {
    if (!name.endsWith(".java")) {
      return Optional.empty();
    }
    
    return ModuleHelper.sourceModuleDescriptor(Paths.get(name));
  }
}
