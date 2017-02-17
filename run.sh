#to run with jlink image:
#./target/image/bin/moduletools target/main/exploded/moduletools.api/module-info.class > /dev/null

#to run with jdk9 image:
#/usr/jdk/jdk-9/bin/java \
#  --module-path target/main/artifact:deps \
#  --module moduletools.main \
#  target/main/exploded/moduletools.api/module-info.class > /dev/null

#with AOT:
# -XX:+PrintAOT \
time ./target/image/bin/java \
   -XX:+UseAOT \
   -XX:AOTLibrary=./lib/libjava.base.so \
   -XX:AOTLibrary=./lib/libasm.all.so \
   -XX:AOTLibrary=./lib/libmoduletools.impl.so \
   -XX:AOTLibrary=./lib/libmoduletools.api.so \
   -XX:AOTLibrary=./lib/libmoduletools.service.so \
   -XX:AOTLibrary=./lib/libmoduletools.main.so \
   -m moduletools.main/fr.umlv.moduletools.main.Main \
   target/main/exploded/moduletools.api/module-info.class > /dev/null
