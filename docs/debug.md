# over unix domain sock

```bash
curl \
--unix-socket ./jdph.sock \
--location 'http://localhost/VolumeDriver.Create' \
--header 'Content-Type: application/json' \
--data '{
"Name": "Hello"
}'
```

```bash
curl \
--unix-socket ./jdph.sock \
--location 'http://localhost/VolumeDriver.Get' \
--header 'Content-Type: application/json' \
--data '{
"Name": "Hello"
}'
```

```bash
curl --unix-socket /tmp/jdph.sock \
--location 'http://localhost/VolumeDriver.Unmount' \
--header 'Content-Type: application/json' \
--data '{
"Name": "Hello"
}'
```