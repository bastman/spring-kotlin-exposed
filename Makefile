GRADLE_VERSION=5.2.1

print-%: ; @echo $*=$($*)
guard-%:
	@test ${${*}} || (echo "FAILED! Environment variable $* not set " && exit 1)
	@echo "-> use env var $* = ${${*}}";

.PHONY : help
help : Makefile
	@sed -n 's/^##//p' $<

## idea-start:   : start intellij
idea-start:
	open -a /Applications/IntelliJ\ IDEA.app

## gradle-wrapper:   : install gradle wrapper
gradle-wrapper:
	./gradlew --version
	./gradlew wrapper --gradle-version=$(GRADLE_VERSION)
	./gradlew --version
