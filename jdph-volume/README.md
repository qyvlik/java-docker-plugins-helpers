# jdph-volume

## debugging

```bash
sudo runc --root /run/docker/runtime-runc/plugins.moby list
sudo runc --root /run/docker/runtime-runc/plugins.moby exec 6c6f64a28808513952eafb3ff46dfa921cea12538c6a038fd0d9a474fcc89012 cat /var/log/plugin.log
sudo runc --root /run/docker/runtime-runc/plugins.moby exec -t 6c6f64a28808513952eafb3ff46dfa921cea12538c6a038fd0d9a474fcc89012 sh

```

## create volume

```bash
docker volume create --driver=jdph-volume:0.1.0 Hello
```

## over via unix domain sock

```bash
curl \
--unix-socket ./jdph-volume.sock \
--location 'http://localhost/VolumeDriver.Create' \
--header 'Content-Type: application/json' \
--data '{
    "Name": "Hello",
    "Options": {}
}'

curl \
--unix-socket ./jdph-volume.sock \
--location 'http://localhost/VolumeDriver.Get' \
--header 'Content-Type: application/json' \
--data '{
    "Name": "Hello"
}'

curl --unix-socket /tmp/jdph-volume.sock \
--location 'http://localhost/VolumeDriver.Unmount' \
--header 'Content-Type: application/json' \
--data '{
    "Name": "Hello"
}'
```
