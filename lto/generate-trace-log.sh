../target/image/bin/java \
   -noverify \
   -Xlog:all \
   -XX:+UseAOT \
   -XX:AOTLibrary=./lib/libjava.base.so \
   -XX:AOTLibrary=./lib/libasm.all.so \
   -XX:AOTLibrary=./lib/libmoduletools.impl.so \
   -XX:AOTLibrary=./lib/libmoduletools.api.so \
   -XX:AOTLibrary=./lib/libmoduletools.service.so \
   -XX:AOTLibrary=./lib/libmoduletools.main.so \
   -m moduletools.main/fr.umlv.moduletools.main.Main \
   ../target/main/exploded/moduletools.api/module-info.class

