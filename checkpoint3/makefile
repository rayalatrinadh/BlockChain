# Define variables for commands to avoid repetition
MVN=mvn
JAVA=java
JAR_PATH=target/dsc-1.0-SNAPSHOT-jar-with-dependencies.jar
PACKAGE=com.aos

# Define a default target
all: install

# Install dependencies and build the project
install:
	cd dsc && $(MVN) clean install

# Run the Blockchain component
run-blockchain:
	$(JAVA) -cp $(JAR_PATH) $(PACKAGE).BlockChain

# Run the Pool component
run-pool:
	$(JAVA) -cp $(JAR_PATH) $(PACKAGE).Pool

# Run the Metronome component
run-metronome:
	$(JAVA) -cp $(JAR_PATH) $(PACKAGE).Metronome

# Run the Validator component
run-validator:
	$(JAVA) -cp $(JAR_PATH) $(PACKAGE).Validator

# Run the Main class
run-main:
	$(JAVA) -cp $(JAR_PATH) $(PACKAGE).Main

# Define a target for system updates (requires sudo)
update-system:
	sudo apt-get update
	sudo apt-get upgrade

# Define a target for installing Maven (requires sudo)
install-maven:
	sudo apt install maven

.PHONY: install run-blockchain run-pool run-metronome run-validator run-main update-system install-maven
