package fr.umlv.lto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

public class TraceReader {
  private static Optional<String> findClassName(String[] tokens) {
    switch(tokens.length) {
    case 4:
      String className = tokens[1];
      String source = tokens[3];
      if (tokens[2].equals("source:") &&     // log line do not contains a class name
          className.indexOf('/') == -1 &&    // skip lambda proxy
          !source.contains("__")) {          // skip VM defined unamed class
            return Optional.of(tokens[1]);
      }
      //$FALL-THROUGH$
    default:
    }
    System.err.println("skip " + String.join(" ", tokens));
    return Optional.empty();
  }
  
  public static void main(String[] args) throws IOException {
    Path input = Paths.get(args[0]);
    Path output = Paths.get(args[1]);
    try(Stream<String> lines = Files.lines(input)) {
      Stream<String> stream = lines
          .map(line -> line.split(" "))
          .flatMap(tokens -> findClassName(tokens).stream())
          .map(className -> "compileOnly " + className + ".*");
      Files.write(output, (Iterable<String>)stream.sorted()::iterator);
    }
  }
}
