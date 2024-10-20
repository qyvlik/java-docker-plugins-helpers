# jdph-volume

## debugging

```bash
sudo runc --root /run/docker/runtime-runc/plugins.moby list
sudo runc --root /run/docker/runtime-runc/plugins.moby exec 54630a1eaa99dee9d6e138e13ac9d60face9667c027d0755979526bea7c82145 cat /var/log/plugin.log
sudo runc --root /run/docker/runtime-runc/plugins.moby exec -t 54630a1eaa99dee9d6e138e13ac9d60face9667c027d0755979526bea7c82145 sh

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
    "Opts": null
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

## search

- https://github.com/search?q=%2FVolumeDriver.Create&type=code&p=2
- https://github.com/intesar/SampleDockerVolumePlugin