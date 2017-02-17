#export jaotc=/usr/jdk/jdk-9/bin/jaotc
export jaotc=./target/image/bin/jaotc

$jaotc -J-Xmx4g \
      --output libmoduletools.impl.so \
      --module moduletools.impl

$jaotc -J-Xmx4g \
      --output libmoduletools.service.so \
      --module moduletools.service
      
$jaotc -J-Xmx4g \
      --output libmoduletools.api.so \
      --module moduletools.api

$jaotc -J-Xmx4g \
      --output libmoduletools.main.so \
      --module moduletools.main

$jaotc -J-Xmx4g \
      --output libasm.all.so \
      --module org.objectweb.asm.all.debug

$jaotc -J-Xmx4g \
      --output libjdk.compiler.so \
      --module jdk.compiler

#$jaotc -J-Xmx4g \
#      --compile-commands java.base-excludes.txt \
#      --output libjava.base.so \
#      --module java.base

