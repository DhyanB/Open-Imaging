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
	@echo "    help           Show this help message."
	@echo "    b              Build."
	@echo "    t              Test."
	@echo "    cb             Clean and build."
	@echo "    bt             Build and test."
	@echo "    cbt            Clean, build and test."
	@echo ""

b:
	$(GRADLE_CMD) build -x test

t:
	$(GRADLE_CMD) test --stacktrace

cb:
	$(GRADLE_CMD) clean build -x test

bt:
	$(GRADLE_CMD) build

cbt:
	$(GRADLE_CMD) clean build
