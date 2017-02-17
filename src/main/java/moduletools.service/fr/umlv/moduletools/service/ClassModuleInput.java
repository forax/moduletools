package fr.umlv.moduletools.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.module.ModuleDescriptor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import fr.umlv.moduletools.api.ModuleInput;

public class ClassModuleInput implements ModuleInput {
  @Override
  public Optional<ModuleDescriptor> read(String name) throws UncheckedIOException {
    if (!name.endsWith(".class")) {
      return Optional.empty();
    }
    
    try(InputStream input = Files.newInputStream(Paths.get(name))) {
      return Optional.of(ModuleDescriptor.read(input));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
