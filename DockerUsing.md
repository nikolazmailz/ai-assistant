## docker

```bash

sudo mkdir -p /opt/postgres/{data,backups}
sudo chown -R 999:999 /opt/postgres/data /opt/postgres/backups
sudo chmod 700 /opt/postgres/data


docker inspect postgres16   --format 'Mounts: {{range .Mounts}}{{println .Source "->" .Destination}}{{end}}'


``