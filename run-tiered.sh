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
   -XX:AOTLibrary=./lib-tiered/libjava.base-tiered.so \
   -XX:AOTLibrary=./lib-tiered/libasm.all-tiered.so \
   -XX:AOTLibrary=./lib-tiered/libmoduletools.impl-tiered.so \
   -XX:AOTLibrary=./lib-tiered/libmoduletools.api-tiered.so \
   -XX:AOTLibrary=./lib-tiered/libmoduletools.service-tiered.so \
   -XX:AOTLibrary=./lib-tiered/libmoduletools.main-tiered.so \
   --module-path target/main/artifact:deps \
   --module moduletools.main \
   target/main/exploded/moduletools.api/module-info.class > /dev/null
