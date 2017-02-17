module moduletools.api {
  requires moduletools.impl;
  
  exports fr.umlv.moduletools.api; 
  
  uses fr.umlv.moduletools.api.ModuleInput;
  uses fr.umlv.moduletools.api.ModuleOutput;
}