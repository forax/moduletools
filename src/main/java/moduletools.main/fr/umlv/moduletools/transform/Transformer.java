package fr.umlv.moduletools.transform;

import java.lang.module.ModuleDescriptor;

public interface Transformer {
  void transform(ModuleDescriptor.Builder builder);
  
  default Transformer compose(Transformer transformer) {
    return builder -> {
      transform(builder);
      transformer.transform(builder);
    };
  }
  
  static Transformer empty() {
    return __ -> { /* empty */ };
  }
}