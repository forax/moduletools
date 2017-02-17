# trace used classes
../target/image/bin/java \
   -Xbatch \
   -XX:-TieredCompilation \
   -XX:CompileThreshold=1 \
   -XX:+PrintCompilation \
   --module-path ../target/main/artifact:deps \
   -m moduletools.main/fr.umlv.moduletools.main.Main \
   ../target/main/exploded/moduletools.api/module-info.class module-info.log > trace-jit.txt

../target/image/bin/java \
  --module-path target/main/artifact \
  -m fr.umlv.lto/fr.umlv.lto.JITTraceReader trace-jit.txt lto.txt

