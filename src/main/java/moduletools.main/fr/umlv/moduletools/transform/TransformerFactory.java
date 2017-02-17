package fr.umlv.moduletools.transform;

public interface TransformerFactory {
  Transformer create(String arguments);
  
  static TransformerFactory from(String option) {
    return TransformerFactories.from(option); 
  }
}