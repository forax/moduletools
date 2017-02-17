package fr.umlv.moduletools.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.module.ModuleDescriptor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import fr.umlv.moduletools.api.ModuleOutput;
import fr.umlv.moduletools.api.internal.ModuleHelper;

public class SourceModuleOutput implements ModuleOutput {
  @Override
  public Optional<Result> write(String name, ModuleDescriptor moduleDescriptor) throws UncheckedIOException {
    if (!name.endsWith(".java")) {
      return Optional.empty();
    }
    
    try {
      Files.write(Paths.get(name), List.of(ModuleHelper.moduleDescriptorToSource(moduleDescriptor)));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return Optional.of(Result.OK);
  }
}
