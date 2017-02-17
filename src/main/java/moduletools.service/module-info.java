module moduletools.service {
  requires moduletools.api;
  requires moduletools.impl;
  
  provides fr.umlv.moduletools.api.ModuleInput
    with fr.umlv.moduletools.service.SourceModuleInput,
         fr.umlv.moduletools.service.ClassModuleInput;
  provides fr.umlv.moduletools.api.ModuleOutput
    with fr.umlv.moduletools.service.SourceModuleOutput/*,
         fr.umlv.moduletools.service.ClassModuleOutput*/;
}