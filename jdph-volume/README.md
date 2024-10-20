# jdph-volume

## create volume

```bash
docker volume create --driver=jdph-volume Hello
```

## over via unix domain sock

```bash
curl \
--unix-socket ./jdph-volume.sock \
--location 'http://localhost/VolumeDriver.Create' \
--header 'Content-Type: application/json' \
--data '{
    "Name": "Hello"
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
