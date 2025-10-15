package test;

import iwhr.swmm.element.Link;
import iwhr.swmm.element.Node;
import iwhr.swmm.element.SWMM;

import java.io.File;
import java.io.IOException;
/**
 * @author fww
 * Test SWMM execution and result retrieval via Java
 */
public class TestSWMM {

    public static void main(String[] args) throws IOException {

        // Get SWMM file path
        File swmmFile = new File(framework_directory+"/swmm.inp");

        // Create a SWMM model calculation instance
        SWMM swmm = new SWMM();

        // Initialize SWMM running conditions including boundary conditions, 
        // initial state and simulation parameters, finally generate model structure file SWMM.INP
        swmm.initialize(swmmFile);

        // Perform SWMM simulation
        swmm.simulate(swmmFile);

        // After simulation, results are stored in binary OUT file and cannot be read directly
        // According to SWMM model structure: catchments, nodes, conduits, and system,
        // create methods to read results for each part
        // For simplicity, only one node needs to be initialized to get all calculation results

        // Example: get current water level of Bayi Reservoir
        // Bayi Reservoir is modeled as a storage unit (a type of node), so use Node for instantiation
        Node R1 = new Node();
        R1.initialize(swmmFile, R1, "R1"); // Three parameters: 
                                          // 1. Folder containing OUT file
                                          // 2. Node to initialize
                                          // 3. Corresponding ID in the model
        // Model element type correspondence in swmmmodel/mpc/elements-complete.xlsx

        // Now the node Bayi Reservoir has all calculation results
        // Node results include 6 types: depth, head, volume, lateral inflow, 
        // total inflow and flooding volume, with getter/setter methods in initialize
        // Model execution and result output
        System.out.println();
//        System.out.println("Bayi Reservoir depth: " + R1.getDepth());
        System.out.println("Bayi Reservoir water level: " + R1.getHead());
//        System.out.println("Bayi Reservoir storage volume: " + R1.getVolume());
//        System.out.println("Bayi Reservoir lateral inflow: " + R1.getLateralFlow());
//        System.out.println("Bayi Reservoir total inflow: " + R1.getTotalInflow());
//        System.out.println("Bayi Reservoir flooding volume: " + R1.getFlooding());

        // Conduit flow is similar to nodes, example: get flow from Jin'an River to Guangming Port
        // Jin'an River is modeled as conduit (a type of link in SWMM), so use Link for instantiation
        Link C13 = new Link();
        C13.initialize(swmmFile, C13, "C13"); // Three parameters:
                                              // 1. Folder containing OUT file
                                              // 2. Conduit to initialize
                                              // 3. Corresponding ID in the model
        // Model element type correspondence in swmmmodel/mpc/elements-complete.xlsx

        // Now the link Jin'an River has all calculation results
        // Link results include 6 types: flow, flow depth, velocity, volume and capacity,
        // with getter/setter methods in initialize
        // Model execution and result output
        System.out.println();
        System.out.println("Jin'an River flow to Guangming Port: " + C13.getFlow());
//        System.out.println("Jin'an River flow depth: " + C13.getDepth());
//        System.out.println("Jin'an River flow velocity: " + C13.getVelocity());
//        System.out.println("Jin'an River flow volume: " + C13.getVolume());
//        System.out.println("Jin'an River conduit capacity: " + C13.getCapacity());

    }
}