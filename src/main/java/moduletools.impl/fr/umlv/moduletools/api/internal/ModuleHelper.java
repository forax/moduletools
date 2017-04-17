package fr.umlv.moduletools.api.internal;

import static java.util.stream.Collectors.toSet;
import static org.objectweb.asm.Opcodes.ACC_MANDATED;
import static org.objectweb.asm.Opcodes.ACC_MODULE;
import static org.objectweb.asm.Opcodes.ACC_OPEN;
import static org.objectweb.asm.Opcodes.ACC_STATIC_PHASE;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ACC_TRANSITIVE;
import static org.objectweb.asm.Opcodes.V1_9;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Exports;
import java.lang.module.ModuleDescriptor.Opens;
import java.lang.module.ModuleDescriptor.Provides;
import java.lang.module.ModuleDescriptor.Requires;
import java.lang.module.ModuleDescriptor.Version;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.tree.ModuleNode;
import org.objectweb.asm.tree.ModuleRequireNode;

import fr.umlv.moduletools.api.internal.JavacModuleParser.ModuleClassVisitor;

public class ModuleHelper {
  private ModuleHelper() {
    throw new AssertionError(); 
  }

  private static Set<Requires.Modifier> requireModifiers(int modifiers) {
    return Map.of(
        ACC_MANDATED, Requires.Modifier.MANDATED,
        ACC_SYNTHETIC, Requires.Modifier.SYNTHETIC,
        ACC_TRANSITIVE, Requires.Modifier.TRANSITIVE,
        ACC_STATIC_PHASE, Requires.Modifier.STATIC)
      .entrySet()
      .stream()
      .map(entry -> (modifiers & entry.getKey()) != 0? entry.getValue(): null)
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());
  }
  
  private static void parseModule(Path moduleInfoPath, ModuleClassVisitor visitor) {
    try {
      JavacModuleParser.parse(moduleInfoPath, visitor);
    } catch(IOException e) {
      throw new UncheckedIOException(e);
    }
  }
  
  private static Optional<ModuleNode> sourceModuleInfo(Path moduleInfoPath) {
    class Visitor implements  ModuleClassVisitor {
      ModuleNode moduleNode;
      
      @Override
      public ModuleVisitor visitModule(String name, int flags, String version) {
        return moduleNode = new ModuleNode(name, flags, version);
      }
    }
    
    Visitor visitor = new Visitor();
    parseModule(moduleInfoPath, visitor);
    ModuleNode moduleNode = visitor.moduleNode;
    if (moduleNode == null) {
      return Optional.empty();
    }
    moduleNode.requires = fixNull(moduleNode.requires);
    if (moduleNode.requires.stream().noneMatch(require -> require.module.equals("java.base"))) {
      moduleNode.requires.add(new ModuleRequireNode("java.base", ACC_MANDATED, null));
    }
    moduleNode.exports = fixNull(moduleNode.exports);
    moduleNode.opens = fixNull(moduleNode.opens);
    moduleNode.uses = fixNull(moduleNode.uses);
    moduleNode.provides = fixNull(moduleNode.provides);
    return Optional.of(moduleNode);
  }
  
  private static <T> List<T> fixNull(List<T> list) {
    return list == null? new ArrayList<>(): list;
  }
  
  private static Set<String> findJavaPackages(Path moduleDirectory) {
    try(Stream<Path> stream = Files.walk(moduleDirectory)) {
      return stream
          .filter(path -> path.getFileName().toString().endsWith(".java"))
          .map(path -> moduleDirectory.relativize(path))
          .filter(path -> path.getParent() != null)
          .map(path -> path.getParent().toString().replace('/', '.').replace('\\', '.'))
          .collect(Collectors.toSet());
    } catch(IOException e) {
      throw new UncheckedIOException(e);
    }
  }
  
  public static Optional<ModuleDescriptor> sourceModuleDescriptor(Path moduleInfoPath) {
    return sourceModuleInfo(moduleInfoPath).map(moduleNode -> createModuleDescriptor(moduleNode, moduleInfoPath));
  }
  
  private static ModuleDescriptor createModuleDescriptor(ModuleNode moduleNode, Path moduleInfoPath) {
    boolean isOpen = (moduleNode.access & ACC_OPEN) != 0;
    ModuleDescriptor.Builder builder = isOpen?
        ModuleDescriptor.newOpenModule(moduleNode.name):
        ModuleDescriptor.newModule(moduleNode.name);
    
    moduleNode.requires.forEach(require -> builder.requires(requireModifiers(require.access), require.module));
    moduleNode.exports.forEach(export -> {
      if (export.modules.isEmpty()) {
        builder.exports(export.packaze);
      } else {
        builder.exports(export.packaze, export.modules.stream().collect(toSet()));
      }
    });
    moduleNode.opens.forEach(open -> {
      if (open.modules.isEmpty()) {
        builder.opens(open.packaze);
      } else {
        builder.opens(open.packaze, open.modules.stream().collect(toSet()));
      }
    });
    moduleNode.uses.forEach(builder::uses);
    moduleNode.provides.forEach(provide -> builder.provides(provide.service, provide.providers));

    Path moduleDirectory = moduleInfoPath.getParent();
    Set<String> javaPackages = findJavaPackages(moduleDirectory);
    javaPackages.removeAll(moduleNode.exports.stream().map(export -> export.packaze).collect(Collectors.toList()));
    javaPackages.removeAll(moduleNode.opens.stream().map(export -> export.packaze).collect(Collectors.toList()));
    builder.packages(javaPackages);

    ModuleDescriptor descriptor = builder.build();
    //System.out.println(descriptor.name() + " " + descriptor.packages());

    return descriptor;
  }
  
  public static String moduleDescriptorToSource(ModuleDescriptor descriptor) {
    class Generator {
      private final ArrayList<Stream<String>> streams = new ArrayList<>();
      
      Generator $(String text) {
        streams.add(Stream.of(text)); return this;
      }
      Generator $(String text, Function<ModuleDescriptor, ? extends String> mapper) {
        $(String.format(text, mapper.apply(descriptor))); return this;
      }
      <T> Generator $(String text, Function<ModuleDescriptor, ? extends Collection<? extends T>> elementMapper, Function<T, String> mapper) {
        streams.add(elementMapper.apply(descriptor).stream().map(e -> String.format(text, mapper.apply(e)))); return this;
      }
      <T> Generator $(String text, Function<ModuleDescriptor, ? extends Collection<? extends T>> elementMapper, Function<? super T, String> mapper, String text2, Function<? super T, Collection<? extends String>> mapper2) {
        streams.add(elementMapper.apply(descriptor).stream().map(e -> {
            Collection<? extends String> values = mapper2.apply(e);
            String format = values.isEmpty()? text: String.format(text, "%s " + text2);
            return String.format(format, mapper.apply(e), values.stream().collect(Collectors.joining(",")));
          }));
        return this;
      }
      String join() {
        return streams.stream().flatMap(x -> x).collect(Collectors.joining("\n"));
      }
    }
    
    return new Generator()
          .$("%s",             desc -> desc.isOpen()? "open":"")
          .$("module %s {",    ModuleDescriptor::name)
          .$("  requires %s;", ModuleDescriptor::requires, Requires::name)
          .$("  exports %s;",  ModuleDescriptor::exports,  Exports::source,   "to %s", Exports::targets)
          .$("  opens %s;",    ModuleDescriptor::opens,    Opens::source,     "to %s", Opens::targets)
          .$("")
          .$("  uses %s;",     ModuleDescriptor::uses,     Function.identity())
          .$("  provides %s;", ModuleDescriptor::provides, Provides::service, "with %s", Provides::providers)
          .$("}\n")
          .join();
  }

  public static byte[] moduleDescriptorToBinary(ModuleDescriptor descriptor) {
    ClassWriter classWriter = new ClassWriter(0);
    classWriter.visit(V1_9, ACC_MODULE, "module-info", null, null, null);
    int moduleFlags = (descriptor.isOpen()? ACC_OPEN: 0) | ACC_SYNTHETIC;   // mark all generated module-info.class as synthetic    
    String moduleVersion = descriptor.version().map(Version::toString).orElse(null);
    org.objectweb.asm.ModuleVisitor mv = classWriter.visitModule(descriptor.name(), moduleFlags, moduleVersion);
    descriptor.packages().forEach(packaze -> mv.visitPackage(packaze.replace('.', '/')));
    
    descriptor.mainClass().ifPresent(mainClass -> mv.visitMainClass(mainClass.replace('.', '/')));
    
    descriptor.requires().forEach(require -> {
      int modifiers = require.modifiers().stream().mapToInt(ModuleHelper::modifierToInt).reduce(0, (a, b) -> a | b);
      mv.visitRequire(require.name(), modifiers, null);
    });
    descriptor.exports().forEach(export -> {
      int modifiers = export.modifiers().stream().mapToInt(ModuleHelper::modifierToInt).reduce(0, (a, b) -> a | b);
      mv.visitExport(export.source().replace('.', '/'), modifiers, export.targets().toArray(new String[0]));
    });
    descriptor.opens().forEach(open -> {
      int modifiers = open.modifiers().stream().mapToInt(ModuleHelper::modifierToInt).reduce(0, (a, b) -> a | b);
      mv.visitExport(open.source().replace('.', '/'), modifiers, open.targets().toArray(new String[0]));
    });
    descriptor.uses().forEach(service -> mv.visitUse(service));
    descriptor.provides().forEach(provide -> {
      mv.visitProvide(provide.service().replace('.', '/'), provide.providers().stream().map(name -> name.replace('.', '/')).toArray(String[]::new));
    });
    mv.visitEnd();
    classWriter.visitEnd();
    return classWriter.toByteArray();
  }
  
  private static int modifierToInt(Requires.Modifier modifier) {
    switch(modifier) {
    case MANDATED:
      return ACC_MANDATED;
    case SYNTHETIC:
      return ACC_SYNTHETIC;
    case STATIC:
      return ACC_STATIC_PHASE;
    case TRANSITIVE:
      return ACC_TRANSITIVE;
    default:
      throw new IllegalStateException("unknown modifier " + modifier);
    }
  }
  
  private static int modifierToInt(Exports.Modifier modifier) {
    switch(modifier) {
    case MANDATED:
      return ACC_MANDATED;
    case SYNTHETIC:
      return ACC_SYNTHETIC;
    default:
      throw new IllegalStateException("unknown modifier " + modifier);
    }
  }
  
  private static int modifierToInt(Opens.Modifier modifier) {
    switch(modifier) {
    case MANDATED:
      return ACC_MANDATED;
    case SYNTHETIC:
      return ACC_SYNTHETIC;
    default:
      throw new IllegalStateException("unknown modifier " + modifier);
    }
  }
}
