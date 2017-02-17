#export jaotc=/usr/jdk/jdk-9/bin/jaotc
export jaotc=./target/image/bin/jaotc

$jaotc -J-Xmx4g \
      --compile-for-tiered \
      --output libmoduletools.impl-tiered.so \
      --module moduletools.impl

$jaotc -J-Xmx4g \
      --compile-for-tiered \
      --output libmoduletools.service-tiered.so \
      --module moduletools.service
      
$jaotc -J-Xmx4g \
      --compile-for-tiered \
      --output libmoduletools.api-tiered.so \
      --module moduletools.api

$jaotc -J-Xmx4g \
      --compile-for-tiered \
      --output libmoduletools.main-tiered.so \
      --module moduletools.main

$jaotc -J-Xmx4g \
      --compile-for-tiered \
      --output libasm.all-tiered.so \
      --module org.objectweb.asm.all.debug

$jaotc -J-Xmx4g \
      --compile-for-tiered \
      --output libjdk.compiler-tiered.so \
      --module jdk.compiler

#$jaotc -J-Xmx4g \
#      --compile-for-tiered \
#      --compile-commands java.base-excludes.txt \
#      --output libjava.base-tiered.so \
#      --module java.base

