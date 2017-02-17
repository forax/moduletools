export jaotc=/usr/jdk/jdk-9/bin/jaotc
#export jaotc=./target/image/bin/jaotc

$jaotc -J-Xmx10g \
      --compile-commands java.base-excludes.txt \
      --output libjava.base.so \
      --module java.base

