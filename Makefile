VERSION ?= 0.1.0

create:
	docker build -t jdph-volume-rfi .
	docker create --name jdph-volume jdph-volume-rfi
	mkdir -p plugin/rootfs/
	docker export jdph-volume | tar -x -C jdph-volume/plugin/rootfs/
	docker rm -vf jdph-volume
	docker plugin create jdph-volume:$(VERSION) ./jdph-volume/plugin
	# docker rmi --force jdph-volume-rfi

enable:
	docker plugin enable jdph-volume:$(VERSION)

push:
	docker plugin push jdph-volume:$(VERSION)

.PHONY: clean
clean: clean-cache

.PHONY: clean-cache
clean-cache:
	rm -fr ./jdph-volume/plugin/rootfs/*
	docker plugin rm -f jdph-volume:$(VERSION)
