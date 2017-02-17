package fr.umlv.moduletools.main;

import java.io.UncheckedIOException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Builder;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

import fr.umlv.moduletools.transform.Transformer;

public class Main {
  private static void help() {
    String help =
      "module-utils source [destination] [transformers]              \n" +
      "  source:      module.java|module.class|module.name           \n" +
      "  destination: module.java|module.class                       \n" +
      "                                                              \n" +
      "  transformer:                                                \n" +
      "    --add-reads <target-module>(,<target-module>)*            \n" +
      "      updates to read <target-module>                         \n" +
      "    --add-exports <package>=<target-module>(,<target-module>)*\n" +
      "      updates to export <package> to <target-module>          \n" +
      "    --add-opens <package>=<target-module>(,<target-module>)*  \n" +
      "      updates to open <package> to <target-module>            \n" + 
      "    --add-packages <package>(,<packages>)*                    \n" +
      "      updates to add non exported packages                    \n" + 
      "    --add-uses <interface>                                    \n" +
      "      updates to uses service <interface>                     \n" +
      "    --add-provides <interface>=<class>(,<class>)         \n" +
      "      updates to provide service <interface> with <class>     \n"; 
    System.err.println(help);
  }
  
  private static Builder toBuilder(ModuleDescriptor descriptor) {
    Builder builder;
    String name = descriptor.name();
    if (descriptor.isAutomatic()) {
      builder = ModuleDescriptor.automaticModule(name);
    } else {
      if (descriptor.isOpen()) {
        builder = ModuleDescriptor.openModule(name);
      } else {
        builder = ModuleDescriptor.module(name);
      }
    }
    
    descriptor.requires().forEach(builder::requires);
    descriptor.exports().forEach(builder::exports);
    descriptor.opens().forEach(builder::opens);
    
    HashSet<String> packages = new HashSet<>(descriptor.packages());
    descriptor.exports().forEach(export -> packages.remove(export.source()));
    descriptor.opens().forEach(open -> packages.remove(open.source()));
    packages.forEach(builder::contains);
    
    descriptor.uses().forEach(builder::uses);
    descriptor.provides().forEach(builder::provides);
    
    return builder;
  }
  
  public static void main(String[] args) {
    CommandLine commandLine;
    try {
      commandLine = CommandLine.parse(args);
    } catch(InvalidCommandLineException e) {
      System.err.println(e.getMessage());
      help();
      return;
    }
    
    Supplier<ModuleDescriptor> reader = commandLine.getReader();
    Consumer<ModuleDescriptor> writer = commandLine.getWriter();
    Transformer transformer = commandLine.getTransformer();
    
    try {
      ModuleDescriptor descriptor = reader.get();
      Builder builder = toBuilder(descriptor);
      transformer.transform(builder);
      writer.accept(builder.build());
    } catch(UncheckedIOException e) {
      System.err.println(e.getCause().getMessage());
    }
  }
}
