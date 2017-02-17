module moduletools.impl {
  requires jdk.compiler;
  requires org.objectweb.asm.all.debug;
  
  exports fr.umlv.moduletools.api.internal
    to moduletools.service, moduletools.api; 
}