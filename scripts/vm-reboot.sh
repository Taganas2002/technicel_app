#!/bin/bash
export PATH=/usr/local/docker:/usr/local/bin:/usr/bin
if ! pgrep -x dockerd >/dev/null; then
  sudo env PATH=/usr/local/docker:/usr/local/bin:/usr/sbin:/usr/bin \
    nohup /usr/local/bin/dockerd --userland-proxy-path=/usr/local/docker/docker-proxy \
    >/tmp/dockerd.log 2>&1 &
  sleep 8
fi
cd /home/opc/technicel_app
sudo env PATH=/usr/local/docker:/usr/local/bin:$PATH /usr/local/bin/docker-compose -f docker-compose.mysql.yml --env-file .env up -d
sleep 15
bash /home/opc/technicel_app/vm-start-java.sh
