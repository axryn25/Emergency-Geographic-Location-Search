package caseStudy;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // Initializing R-Tree with node capacity maxEntries = 4
        RTree rtree = new RTree(4);
        
        System.out.println("=================================================");
        System.out.println(" EMERGENCY GEOGRAPHIC SEARCH & SPATIAL INDEXING ");
        System.out.println("       Menu-Driven DSA Case Study Engine        ");
        System.out.println("=================================================");

        boolean exit = false;

        while (!exit) {
            System.out.println("\n---------------- MAIN MENU ----------------");
            System.out.println("1. Seed Sample Emergency Facilities");
            System.out.println("2. Insert a Custom Facility");
            System.out.println("3. Perform Spatial Range Search");
            System.out.println("4. Perform Filtered Range Search (by Type)");
            System.out.println("5. Find K-Nearest Facilities (k-NN)");
            System.out.println("6. Delete a Facility by ID");
            System.out.println("7. Run Performance Benchmark (R-Tree vs Linear)");
            System.out.println("8. Exit Application");
            System.out.print("Select an option (1-8): ");

            int choice = -1;
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // Clear newline buffer
            } else {
                scanner.nextLine(); // Clear invalid input
                System.out.println("Invalid input! Please enter a number between 1 and 8.");
                continue;
            }

            switch (choice) {
                case 1:
                    seedSampleData(rtree);
                    break;

                case 2:
                    System.out.print("Enter Facility ID (e.g., H1): ");
                    String id = scanner.nextLine();
                    System.out.print("Enter Facility Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter Type (HOSPITAL / FIRE_STATION / POLICE / AMBULANCE): ");
                    String type = scanner.nextLine().toUpperCase();
                    System.out.print("Enter X Coordinate: ");
                    double x = scanner.nextDouble();
                    System.out.print("Enter Y Coordinate: ");
                    double y = scanner.nextDouble();
                    scanner.nextLine(); // Clear buffer

                    EmergencyFacility customFacility = new EmergencyFacility(id, name, type, x, y);
                    rtree.insert(customFacility);
                    System.out.print("[SUCCESS] Inserted: ");
                    customFacility.printInfo();
                    break;

                case 3:
                    System.out.println("Define Spatial Range Query Window:");
                    System.out.print("Min X: ");
                    double minX = scanner.nextDouble();
                    System.out.print("Min Y: ");
                    double minY = scanner.nextDouble();
                    System.out.print("Max X: ");
                    double maxX = scanner.nextDouble();
                    System.out.print("Max Y: ");
                    double maxY = scanner.nextDouble();
                    scanner.nextLine();

                    Rectangle searchWindow = new Rectangle(minX, minY, maxX, maxY);
                    EmergencyFacility[] results = rtree.searchRange(searchWindow);

                    System.out.println("\n--- Search Results (" + results.length + " matched) ---");
                    for (int i = 0; i < results.length; i++) {
                        System.out.print(" -> ");
                        results[i].printInfo();
                    }
                    break;

                case 4:
                    System.out.println("Define Spatial Range Query Window:");
                    System.out.print("Min X: ");
                    double fMinX = scanner.nextDouble();
                    System.out.print("Min Y: ");
                    double fMinY = scanner.nextDouble();
                    System.out.print("Max X: ");
                    double fMaxX = scanner.nextDouble();
                    System.out.print("Max Y: ");
                    double fMaxY = scanner.nextDouble();
                    scanner.nextLine();

                    System.out.print("Enter Facility Type Filter: ");
                    String filterType = scanner.nextLine();

                    Rectangle fWindow = new Rectangle(fMinX, fMinY, fMaxX, fMaxY);
                    EmergencyFacility[] filteredResults = rtree.searchRangeWithType(fWindow, filterType);

                    System.out.println("\n--- Filtered Search Results (" + filteredResults.length + " matched) ---");
                    for (int i = 0; i < filteredResults.length; i++) {
                        System.out.print(" -> ");
                        filteredResults[i].printInfo();
                    }
                    break;

                case 5:
                    System.out.print("Enter Target X Coordinate: ");
                    double targetX = scanner.nextDouble();
                    System.out.print("Enter Target Y Coordinate: ");
                    double targetY = scanner.nextDouble();
                    System.out.print("Enter K (number of nearest neighbors): ");
                    int k = scanner.nextInt();
                    scanner.nextLine();

                    EmergencyFacility[] nearest = rtree.searchNearest(targetX, targetY, k);
                    System.out.println("\n--- Top " + nearest.length + " Nearest Facilities ---");
                    for (int i = 0; i < nearest.length; i++) {
                        System.out.printf(" %d. %s at (%.1f, %.1f) - Distance: %.2f units\n",
                                (i + 1), nearest[i].getName(), nearest[i].getX(), nearest[i].getY(),
                                nearest[i].distanceTo(targetX, targetY));
                    }
                    break;

                case 6:
                    System.out.print("Enter Facility ID to Delete: ");
                    String deleteId = scanner.nextLine();
                    boolean isDeleted = rtree.delete(deleteId);

                    if (isDeleted) {
                        System.out.println("[SUCCESS] Facility '" + deleteId + "' was deleted and tree was condensed.");
                    } else {
                        System.out.println("[FAILED] Facility ID '" + deleteId + "' not found in the R-Tree.");
                    }
                    break;

                case 7:
                    runBenchmark();
                    break;

                case 8:
                    exit = true;
                    System.out.println("Exiting Application. . . .");
                    break;

                default:
                    System.out.println("Invalid option! Please select between 1 and 8.");
            }
        }
        scanner.close();
    }

    private static void seedSampleData(RTree rtree) {
        System.out.println("\nSeeding 6 default emergency facilities...");
        rtree.insert(new EmergencyFacility("H1", "City Central Hospital", "HOSPITAL", 10.0, 15.0));
        rtree.insert(new EmergencyFacility("H2", "Metro Care Clinic", "HOSPITAL", 25.0, 30.0));
        rtree.insert(new EmergencyFacility("F1", "Downtown Fire Station", "FIRE_STATION", 12.0, 18.0));
        rtree.insert(new EmergencyFacility("F2", "Westside Fire HQ", "FIRE_STATION", 45.0, 50.0));
        rtree.insert(new EmergencyFacility("P1", "Central Police Precinct", "POLICE", 15.0, 10.0));
        rtree.insert(new EmergencyFacility("A1", "North Ambulance Post", "AMBULANCE", 30.0, 40.0));
        System.out.println("[SUCCESS] Sample facilities inserted into R-Tree.");
    }

    private static void runBenchmark() {
        System.out.println("\n=================================================");
        System.out.println("  BENCHMARK: R-TREE vs. LINEAR SCAN (ARRAY-BASED)");
        System.out.println("=================================================");

        int datasetSize = 1000;
        RTree benchmarkTree = new RTree(4);
        EmergencyFacility[] benchmarkArray = new EmergencyFacility[datasetSize];

        for (int i = 0; i < datasetSize; i++) {
            double x = (i * 17) % 1000;
            double y = (i * 31) % 1000;
            EmergencyFacility f = new EmergencyFacility("B" + i, "Facility " + i, "HOSPITAL", x, y);
            benchmarkTree.insert(f);
            benchmarkArray[i] = f;
        }

        Rectangle searchWindow = new Rectangle(100.0, 100.0, 250.0, 250.0);
        int iterations = 1000;

        // Linear Scan Execution
        long startLinear = System.nanoTime();
        int linearMatches = 0;
        for (int iter = 0; iter < iterations; iter++) {
            linearMatches = 0;
            for (int i = 0; i < datasetSize; i++) {
                if (searchWindow.containsPoint(benchmarkArray[i].getX(), benchmarkArray[i].getY())) {
                    linearMatches++;
                }
            }
        }
        long linearTime = System.nanoTime() - startLinear;

        // R-Tree Search Execution
        long startRTree = System.nanoTime();
        int rtreeMatches = 0;
        for (int iter = 0; iter < iterations; iter++) {
            rtreeMatches = benchmarkTree.searchRange(searchWindow).length;
        }
        long rtreeTime = System.nanoTime() - startRTree;

        System.out.println(" Benchmark Results (" + iterations + " query iterations over " + datasetSize + " facilities):");
        System.out.println(" Facilities Matched: " + rtreeMatches + " (Verification: " + (rtreeMatches == linearMatches ? "PASSED" : "FAILED") + ")");
        System.out.printf(" Linear Scan Total Time : %.2f ms\n", linearTime / 1e6);
        System.out.printf(" R-Tree Query Total Time: %.2f ms\n", rtreeTime / 1e6);
        if (rtreeTime > 0) {
            System.out.printf(" Speedup Factor          : %.2fx faster\n", (double) linearTime / rtreeTime);
        }
    }
}