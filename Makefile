ifeq ($(OS),Windows_NT)
    GRADLE_CMD=gradlew
else
    GRADLE_CMD=./gradlew
endif

help:
	@echo ""
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@echo "    help               Show this help message."
	@echo "    b                  Build."
	@echo "    t                  Run all tests with default parameters."
	@echo "    cb                 Clean and build."
	@echo "    cbt                Clean, build and test."
	@echo "    bench              Benchmark using 1 warmup and 1 run."
	@echo "    bench w=i r=j      Benchmark using i warmups and j runs."
	@echo "    bench-kw           Benchmark Kevin Weiner's GifDecoder using 1 warmup and 1 run."
	@echo "    bench-kw w=i r=j   Benchmark Kevin Weiner's GifDecoder using i warmups and j runs."
	@echo ""

b:
	$(GRADLE_CMD) build -x test

t:
	$(GRADLE_CMD) test --stacktrace --tests GifDecoderOpenImagingTest

cb:
	$(GRADLE_CMD) clean build -x test

cbt:
	$(GRADLE_CMD) clean build test --tests GifDecoderOpenImagingTest

w ?= 1 # Defaults to 1
r ?= 1 # Defaults to 1
bench:
	$(GRADLE_CMD) test --tests GifDecoderOpenImagingTest.benchmark -Dwarmups=$(w) -Druns=$(r)

w ?= 1 # Defaults to 1
r ?= 1 # Defaults to 1
bench-kw:
	$(GRADLE_CMD) test --tests GifDecoderKevinWeinerTest.benchmark -Dwarmups=$(w) -Druns=$(r)
