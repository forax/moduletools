package fr.umlv.moduletools.transform;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

enum TransformerFactories {
  ADD_READS(args -> splitByComma(args).map(module -> (Transformer)builder -> builder.requires(module)).reduce(Transformer::compose).get()),
  ADD_EXPORTS(args -> exportsTransformer(splitByEqualThenComma(args, true))),
  ADD_OPENS(args -> opensTransformer(splitByEqualThenComma(args, true))),
  ADD_PACKAGES(args -> builder -> builder.contains(splitByComma(args).collect(toSet()))),
  ADD_USES(args -> builder -> builder.uses(args)),
  ADD_PROVIDES(args -> providesTransformer(splitByEqualThenComma(args, false)))
  ;
  
  private final String option;
  private final TransformerFactory factory;
  
  private TransformerFactories(TransformerFactory factory) {
    this.option = "--" + name().replace('_', '-').toLowerCase();
    this.factory = factory;
  }
  
  public Transformer create(String arguments) {
    return factory.create(arguments);
  }
  
  private static Transformer exportsTransformer(Entry<String, List<String>> entry) {
    return builder -> {
      String packaze = entry.getKey();
      List<String> modules = entry.getValue();
      if (modules.isEmpty()) {
        builder.exports(packaze);
        return;
      }
      builder.exports(packaze, new HashSet<>(modules));
    };
  }
  private static Transformer opensTransformer(Entry<String, List<String>> entry) {
    return builder -> {
      String packaze = entry.getKey();
      List<String> modules = entry.getValue();
      if (modules.isEmpty()) {
        builder.opens(packaze);
        return;
      }
      builder.opens(packaze, new HashSet<>(modules));
    };
  }
  private static Transformer providesTransformer(Entry<String, List<String>> entry) {
    return builder -> builder.provides(entry.getKey(), entry.getValue());
  }
  
  private static Stream<String> splitByComma(String property) {
    return Arrays.stream(property.split(","));
  }
  private static Entry<String, List<String>> splitByEqualThenComma(String property, boolean allowsSinglePackage) {
    int index = property.indexOf('=');
    if (index == -1) {
      if (!allowsSinglePackage) {
        throw new IllegalArgumentException("format should be foo=bar(,baz)*");
      }
      return Map.entry(property, List.of());
    }
    String packageName = property.substring(0, index);
    return Map.entry(packageName, splitByComma(property.substring(index + 1)).collect(toList()));
  }

  static TransformerFactory from(String option) {
    return FACTORY_MAP.getOrDefault(option, __ -> { throw new IllegalStateException("unknown option " + option); });
  } 
  
  private static final Map<String, TransformerFactory> FACTORY_MAP =
      Arrays.stream(TransformerFactories.values()).collect(Collectors.toMap(factory -> factory.option, factory -> factory.factory));
}