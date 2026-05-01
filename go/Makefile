race:
	go test -race ./...

vet:
	go vet ./...

test:
	go test -v ./...

topic:
	go test -race -v ./topics/$(T)/...

scratch:
	go run ./scratch/

.PHONY: race vet test topic scratch
