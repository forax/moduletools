package fr.umlv.lto;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class JITTraceReader {
  private static Optional<String> findClassAndMethodName(String line) {
    if (line.endsWith("made not entrant")) {  // discard zombie code messages
      System.err.println("skip not entrant " + line);
      return Optional.empty();
    }
    
    int index = line.indexOf("::");
    int firstIndex = line.lastIndexOf(' ', index - 1);
    int lastIndex = line.indexOf(' ', index + 2);
    if (index == -1 || firstIndex == -1 || lastIndex == -1) {
      System.err.println("skip unknown format " + line);
      return Optional.empty();
    }
    
    String name = line.substring(firstIndex + 1, lastIndex)
        .replace("::", ".");
    
    if (name.indexOf('/') != -1) {
      System.err.println("skip anonymous class " + line);
      return Optional.empty();
    }
    
    /*if (name.indexOf('(') == -1) {  // descriptor less
      name += "(*)*";
    }*/
    return Optional.of(name);
  }
  
  private static Map<String, Set<String>> readClassAsEntry(InputStream input) {
    HashMap<String, Set<String>> map = new HashMap<>(); 
    try {
      ClassReader reader = new ClassReader(input);
      String className = reader.getClassName().replace('/', '.');
      reader.accept(new ClassVisitor(Opcodes.ASM6) {
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
          String methodName = className + '.' + name;
          map.computeIfAbsent(methodName, __ -> new HashSet<>()).add(methodName + desc);
          return null;
        }
      }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
    } catch(IOException e) {
      throw new UncheckedIOException(e);
    }
    return map;
  }
  
  @SuppressWarnings("resource")
  private static Stream<Map<String, Set<String>>> signatures(ModuleReference ref) {
    ModuleReader reader;
    try {
      reader = ref.open();
      return reader.list()
          .filter(name -> name.endsWith(".class"))
          .map(name -> {
            InputStream input;
            try {
              input = reader.open(name).get();
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
            return readClassAsEntry(input); 
          })
          .onClose(() -> {
            try {
              reader.close();
            } catch(IOException e) {
              throw new UncheckedIOException(e);
            }
          });
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    
  }
  
  private static Map<String, Set<String>> indexMap() {
    return ModuleFinder.ofSystem()
      .findAll()
      .stream()
      .flatMap(JITTraceReader::signatures)
      .reduce((m1, m2) -> { m1.putAll(m2); return m1; })
      .orElse(Map.of());
  }
  
  public static void main(String[] args) throws IOException {
    Path input = Paths.get(args[0]);
    Path output = Paths.get(args[1]);
    Map<String, Set<String>> indexMap = indexMap();
    try(Stream<String> lines = Files.lines(input)) {
      Stream<String> stream = lines
          .flatMap(tokens -> findClassAndMethodName(tokens).stream())
          .flatMap(name -> {
            Set<String> sigs = indexMap.get(name);
            //System.out.println("sigs " + sigs + " name " + name); 
            return (sigs == null)? Stream.of(name): sigs.stream();
          })
          .map(methodName -> "compileOnly " + methodName);
      Files.write(output, (Iterable<String>)stream.sorted().distinct()::iterator);
    }
  }
}
