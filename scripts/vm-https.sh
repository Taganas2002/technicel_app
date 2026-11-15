#!/bin/bash
set -eu
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=443/tcp
sudo firewall-cmd --reload
if [ ! -x /usr/local/bin/caddy ]; then
  curl -fsSL -o /tmp/caddy.tgz https://github.com/caddyserver/caddy/releases/download/v2.8.4/caddy_2.8.4_linux_amd64.tar.gz
  tar -xzf /tmp/caddy.tgz -C /tmp caddy
  sudo mv /tmp/caddy /usr/local/bin/caddy
  sudo chmod 755 /usr/local/bin/caddy
fi
sudo mkdir -p /etc/caddy
sudo tee /etc/caddy/Caddyfile >/dev/null <<'EOF'
droptech.duckdns.org {
	reverse_proxy 127.0.0.1:8080
}
EOF
sudo tee /etc/systemd/system/caddy.service >/dev/null <<'EOF'
[Unit]
Description=Caddy
After=network.target

[Service]
ExecStart=/usr/local/bin/caddy run --config /etc/caddy/Caddyfile
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF
sudo systemctl daemon-reload
sudo systemctl enable --now caddy
sleep 3
sudo systemctl is-active caddy || true
sudo journalctl -u caddy -n 20 --no-pager || true