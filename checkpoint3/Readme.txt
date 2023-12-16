
# Blockchain Project

## Description
This blockchain project is designed to handle cryptocurrency transactions, manage wallets, and ensure secure, decentralized data management. It includes several key components such as a blockchain, transaction pool, validator, metronome, and a main class to tie everything together.

## Getting Started

### Dependencies
- Java JDK (Java Development Kit) 8 or higher.
- Maven for project dependency management.
- Linux-based operating system (for the provided setup commands).

### Installing and Running the Application

1. **Update System and Install Maven**:
   ```sh
   sudo apt-get update
   sudo apt-get upgrade
   sudo apt install maven
   ```

2. **Clone the Repository**:
   ```sh
   git clone [https://github.com/datasys-classrooms/cs550-fall2023-project-hackstr]
   ```

3. **Navigate to the Project Directory**:
   ```sh
   cd [project-directory]/dsc
   ```

4. **Build the Project Using the Makefile**:
   - To build the project, run:
     ```sh
     make install
     ```
   - To update the system and install Maven (requires sudo):
     ```sh
     make update-system
     make install-maven
     ```

5. **Run the Components Using the Makefile**:
   - To start the blockchain:
     ```sh
     make run-blockchain
     ```
   - To start the pool:
     ```sh
     make run-pool
     ```
   - To start the metronome:
     ```sh
     make run-metronome
     ```
   - To start the validator:
     ```sh
     make run-validator
     ```
   - To run the main class:
     ```sh
     make run-main
     ```

## Usage

Each component of the blockchain system can be started individually using the Makefile. The Makefile simplifies the process of building and running the application, as well as managing system dependencies.

---

Remember to replace placeholders like `[repository-url]`, `[project-directory]`, `Your Name`, and `Your Email` with your actual project details. This README provides a comprehensive guide for users to get started with your blockchain project, including the use of the Makefile for easy setup and execution.