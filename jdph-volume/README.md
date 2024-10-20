# jdph-volume

```bash
curl \
--unix-socket /tmp/jdph-volume.sock \
--location 'http://localhost/VolumeDriver.Create' \
--header 'Content-Type: application/json' \
--data '{
    "Name": "Hello"
}'

curl \
--unix-socket /tmp/jdph-volume.sock \
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
