export jaotc=/usr/jdk/jdk-9/bin/jaotc
#export jaotc=./target/image/bin/jaotc

$jaotc -J-Xmx10g \
      --compile-for-tiered \
      --compile-commands java.base-excludes.txt \
      --output libjava.base-tiered.so \
      --module java.base

