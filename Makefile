VERSION ?= 0.0.1

create:
	docker build -t jdph-rfi .
	docker create --name jdph jdph-rfi
	mkdir -p plugin/rootfs/
	docker export jdph | tar -x -C plugin/rootfs/
	docker rm -vf jdph
	docker plugin create jdph:$(VERSION) ./plugin
	# docker rmi --force jdph-rfi

enable:
	docker plugin enable jdph:$(VERSION)

push:
	docker plugin push jdph:$(VERSION)

.PHONY: clean
clean: clean-cache

.PHONY: clean-cache
clean-cache:
	rm -fr plugin/rootfs/.dockerenv plugin/rootfs/*
	docker plugin rm -f jdph:$(VERSION)
