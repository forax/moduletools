# trace used classes
../target/image/bin/java \
   -verbose:class \
   --module-path ../target/main/artifact:deps \
   -m moduletools.main/fr.umlv.moduletools.main.Main \
   ../target/main/exploded/moduletools.api/module-info.class module-info.log > trace.txt
   
../target/image/bin/java \
  --module-path target/main/artifact \
  -m fr.umlv.lto/fr.umlv.lto.TraceReader trace.txt lto.txt


