#!/bin/sh
set -eux

echo "*** Installing Kafka and dependencies"
echo "http://dl-4.alpinelinux.org/alpine/edge/testing" >> /etc/apk/repositories
apk add --update runit

# download and cherry-pick zookeeper binaries
curl -SL http://mirrors.sonic.net/apache/zookeeper/zookeeper-$ZOOKEEPER_VERSION/zookeeper-$ZOOKEEPER_VERSION.tar.gz | tar xz
mkdir zookeeper
mv zookeeper-$ZOOKEEPER_VERSION/lib zookeeper/
mv zookeeper-$ZOOKEEPER_VERSION/zookeeper-$ZOOKEEPER_VERSION.jar zookeeper/lib
mv zookeeper-$ZOOKEEPER_VERSION/conf zookeeper/

# create runit config
mkdir -p /etc/service/zookeeper
cat > /etc/service/zookeeper/run <<-EOF
#!/bin/sh
exec java -Dzookeeper.log.dir=/kafka/zookeeper -Dzookeeper.root.logger=INFO,CONSOLE -cp /kafka/zookeeper/lib/*:/kafka/zookeeper/conf org.apache.zookeeper.server.quorum.QuorumPeerMain /kafka/zookeeper/conf/zoo_sample.cfg
EOF
chmod +x /etc/service/zookeeper/run

# download kafka binaries
curl -SL http://apache.mirrors.spacedump.net/kafka/$KAFKA_VERSION/kafka_$SCALA_VERSION-$KAFKA_VERSION.tgz | tar xz
mv kafka_$SCALA_VERSION-$KAFKA_VERSION/* .

# Set explicit, basic configuration
cat > config/server.properties <<-EOF
broker.id=0
port=9092
zookeeper.connect=127.0.0.1:2181
replica.socket.timeout.ms=1500
log.dirs=/kafka/logs
auto.create.topics.enable=true
EOF

# create runit config, dependent on zookeeper, that advertises the container ip
mkdir -p /etc/service/kafka
cat > /etc/service/kafka/run <<-EOF
#!/bin/sh
sv start zookeeper || exit 1
echo advertised.host.name=$(route -n | awk '/UG[ \t]/{print $2}') >> /kafka/config/server.properties
exec sh /kafka/bin/kafka-run-class.sh -name kafkaServer -loggc kafka.Kafka /kafka/config/server.properties
EOF
chmod +x /etc/service/kafka/run

echo "*** Cleaning Up"
rm -rf rm -rf /var/cache/apk/*
rm -rf zookeeper-$ZOOKEEPER_VERSION

echo "*** Image build complete"
