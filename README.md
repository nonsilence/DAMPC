# DAMPC Framework: Real-Time Urban Flood Optimization Control System

The DAMPC (Dynamic Adaptive Model Predictive Control) Framework is a sophisticated Java-based computational system designed for real-time optimization of urban flood control operations. This framework integrates hydrologic-hydraulic modeling with multi-objective optimization algorithms to enable adaptive control of complex lake-river systems under flood conditions. By coupling the Storm Water Management Model (SWMM) with advanced optimization techniques, the system facilitates dynamic gate and pump operations to mitigate flood risks in urban watersheds.

---

## 1. System Overview
### 1.1 Framework Architecture
The DAMPC framework implements a closed-loop model predictive control (MPC) system that executes the following core processes:
- **Real-time Hydrologic-Hydraulic Simulation:**  Utilizes SWMM's dynamic wave routing for accurate flow propagation modeling
- **Multi-objective Optimization:**  Incorporates the UFHWS (Urban Flood Hazard Warning System) risk assessment methodology
- **Adaptive Control Logic:**  Implements IF-THEN-ELSE control rules based on optimization outcomes
- **Performance Evaluation:**  Quantifies flood control effectiveness through comprehensive simulation analysis
### 1.2 Technical Implementation
The system operates as a backend Java application that:
- Executes coupled hydrologic-hydraulic simulations using SWMM
- Extracts model outputs for boundary condition specification in optimization routines
- Performs multi-objective optimization calculations
- Writes optimized control parameters back to the simulation model
- Conducts real-time performance assessment of flood control measures

---

## 2. System Requirements
### 2.1 Software Dependencies
- **Hydraulic Modeling Software:**
	- EPA SWMM (Version 5.0 or higher)
- **Development Environment:**
	- IntelliJ IDEA Community Edition (Version 2024.2.2 or higher)
### 2.2 Java Environment
- **Java Runtime：** Java SE 8 (Update 202 or newer)
	- **Architeture：**  32-bit JDK required for native library compatibility
- **Essential Packages:**
	- JNA 5.10.0 (for native library interfacing)
	- SWMM5.dll (hydraulic simulation engine)
	- Jenetics 5.2.0 (genetic optimization algorithms)

---

## 3. Installation Instructions
### 3.1 Soft Installation
- **(1) Install the required modeling software**
    - Download and install EPA SWMM.
    - Install [IntelliJ IDEA Community.
- **(2) Java Environment Setup**
    - Verify installation of JDK 1.8 (32-bit architecture).
    - Configure project dependencies through Maven configuration file (`pom.xml`).
- **(3) Repository Deployment:**
    - Clone or download the project repository
    - Maintain directory structure integrity to ensure proper path resolution.

---

## 4. Computational Workflow
The framework execution is orchestrated by the master controller JdhMpc.java, which implements the following sequential workflow:
### 4.1 Environment Configuration
- **Configuration File:** `pom.xml`
    - Defines input parameters and file directories.
    - Configures simulation settings for SWMM and Java runtime environment
    - Specifies optimization algorithm parameters
### 4.2 Model Simulation Phase
- **Core Component: ** `TestDLL.java `
    - Validates SWMM model execution through JNA interface
    - Tests result extraction capabilities from simulation outputs
### 4.3 Parameter Calibration and Weighting
- **Entropy Weight Calculation:**`EntropyWeight.java`  
    - Computes UFHWS model indicator weights using entropy method
    - Processes input datasets:
        - Rainfall intensity dataset (`swmm.inp`)
        - FLool peak flow dataset (`swmm.out`)
        - Demographic and urban characteristics (`data.txt`)
- **Optimization Configuration:**`OptMethod.java` 
    - Defines decision variable boundaries and discretization intervals
    - Configures rapid simulation procedures and MPC internal models
    - Specifies objective function evaluation methodologies
    - Implements constraint formulations
    - Tunes optimization solver parameters

### 4.4 MPC Optimization Implementation
- **Main Controller:**`JdhMpc.java` 
- **Predictive Module:**
    - Updates SWMM input files with generated rainfall data
    - Executes SWMM simulations using JNA libraries
    - Extracts nodal flow and stage data from simulation outputs
    - Performs risk assessment for flood scenarios using UFHWS methodology
- **Optimization Module:**  
    - Invokes Jenetics library for objective function evaluation
    - Determines optimal gate and pump opening configurations
- **Feedback Module:**  
    - Computes flood processes under new gate openings using SWMM
    - Propagates water level deviations to subsequent time steps
### 4.5 Output Processing and Analysis
- **Result Extraction: **`TestSWMM.java` 
    - Scans directories for SWMM output files
    - Extracts time series data for specified river sections
    - Merges coordinate data with extracted time series
    - Generates CSV files for subsequent analysis
### 4.6 Data Consolidation
- Organizes non-Java output files (OUT outputs, HSF files) into designated data directories
- Ensures systematic storage and retrieval of simulation results

---

## 5. Execution Protocol
## 5.1 Pre-execution Verification
- **(1) Software Validation:**
   - Confirm installation of all required software and Java dependencies
   - Verify operational status of parent SWMM models
- **(2) Parameter Configuration:**
   - Update file paths and simulation parameters in pom.xmlas necessary
   - Validate parameter ranges and constraints in `testSWMM.java`
## 5.2 System Execution
- **(1) Launch Environment:**
	- Open IntelliJ IDEA Community Edition in project root directory
- **(2) Initiate Optimization Process:**
	-  Run `OptMethod.java`
- **(3) Monitoring and Validation:**
	- Monitor sequential execution of framework modules
	- Verify output data migration to designated Datadirectory

---

## 6. Core Component Specifications
### 6.1 Key Java Classes
- `testDLL.java` : Simple example of JNA interface mapping and usage.
- `TestSWMM.java` :Validates SWMM execution and result extraction
- `OptMethod.java` :Implements Jenetics-based optimization for hydraulic structure control
- `JdhMpc.java`: Main controller for DAMPC optimization in Jingdian-Qinting Lake watershed
- `EntropyWeight.java`:Computes UFHWS model indicator weights
- `pom.xml`:Maven Project Object Model configuration file

---

## 7. Critical Implementation Considerations
### 7.1 Architectural Constraints
- **JDK Environment:** Execution requires 32-bit JDK architecture for native library compatibility
- **Model Integration:**Framework assumes pre-validated and operational SWMM parent models
### 7.2 Data Integrity Assurance
- **Input Validation:**All parameters (rainfall intensities, land cover ranges) require verification in `testSWMM.java`  
- **Path Management:**Framework relies on relative paths defined in `OptMethod.java`; directory structure integrity is critical
### 7.3 Performance Optimization
- **Computational Efficiency:**Implements rapid simulation procedures for real-time applicability
- **Memory Management:**Optimized for handling large-scale urban watershed simulations

---

## 8. License and Contributions
This framework is developed for academic research purposes. For contribution guidelines and licensing information, please contact the development team.

---

## 9. Contact Information
For questions or further information about the DAMPC Framework, please contact at `fww@chd.edu.cn`
