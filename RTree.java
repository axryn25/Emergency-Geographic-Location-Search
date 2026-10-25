
public class RTree {
    private int maxEntries;
    private int minEntries;
    private RTreeNode root;

    public RTree(int maxEntries) {
        this.maxEntries = maxEntries;
        this.minEntries = maxEntries / 2;
        if (this.minEntries < 1) {
            this.minEntries = 1;
        }
        this.root = new RTreeNode(true, maxEntries);
    }

    public RTreeNode getRoot() {
        return root;
    }

    // =========================================================================
    // 1. INSERTION LOGIC
    // =========================================================================

    public void insert(EmergencyFacility facility) {
        RTreeNode leaf = chooseSubtree(root, facility.getMBR());
        leaf.addFacility(facility);

        if (leaf.getCount() > maxEntries) {
            splitNode(leaf);
        } else {
            adjustMBRsUpward(leaf);
        }
    }

    // Traverses down to find the leaf node that requires minimal area expansion
    private RTreeNode chooseSubtree(RTreeNode current, Rectangle itemMBR) {
        if (current.isLeaf()) {
            return current;
        }

        RTreeNode bestChild = null;
        double minEnlargement = Double.MAX_VALUE;
        double minArea = Double.MAX_VALUE;

        for (int i = 0; i < current.getCount(); i++) {
            RTreeNode child = current.getChildren()[i];
            double enlargement = child.getMBR().enlargement(itemMBR);
            double area = child.getMBR().area();

            if (enlargement < minEnlargement) {
                minEnlargement = enlargement;
                minArea = area;
                bestChild = child;
            } else if (enlargement == minEnlargement) {
                if (area < minArea) {
                    minArea = area;
                    bestChild = child;
                }
            }
        }
        return chooseSubtree(bestChild, itemMBR);
    }

    // Splits an overfull node (count > maxEntries) using Quadratic Split logic
    private void splitNode(RTreeNode node) {
        RTreeNode newNode = new RTreeNode(node.isLeaf(), maxEntries);

        if (node.isLeaf()) {
            // Temporary copy of overfull elements
            int total = node.getCount();
            EmergencyFacility[] temp = new EmergencyFacility[total];
            for (int i = 0; i < total; i++) {
                temp[i] = node.getFacilities()[i];
            }

            // Reset node facilities
            clearNodeFacilities(node);

            // Pick 2 seeds that maximize wasted dead area
            int seed1 = 0, seed2 = 1;
            double maxWasted = -1.0;

            for (int i = 0; i < total; i++) {
                for (int j = i + 1; j < total; j++) {
                    Rectangle u = temp[i].getMBR().union(temp[j].getMBR());
                    double wasted = u.area() - temp[i].getMBR().area() - temp[j].getMBR().area();
                    if (wasted > maxWasted) {
                        maxWasted = wasted;
                        seed1 = i;
                        seed2 = j;
                    }
                }
            }

            node.addFacility(temp[seed1]);
            newNode.addFacility(temp[seed2]);

            // Track assigned elements using a boolean array
            boolean[] assigned = new boolean[total];
            assigned[seed1] = true;
            assigned[seed2] = true;
            int assignedCount = 2;

            // Distribute remaining facilities
            while (assignedCount < total) {
                // Ensure minimum node occupancy constraint
                if (node.getCount() + (total - assignedCount) == minEntries) {
                    for (int i = 0; i < total; i++) {
                        if (!assigned[i]) {
                            node.addFacility(temp[i]);
                            assigned[i] = true;
                            assignedCount++;
                        }
                    }
                    break;
                }
                if (newNode.getCount() + (total - assignedCount) == minEntries) {
                    for (int i = 0; i < total; i++) {
                        if (!assigned[i]) {
                            newNode.addFacility(temp[i]);
                            assigned[i] = true;
                            assignedCount++;
                        }
                    }
                    break;
                }

                int bestCandidate = -1;
                double maxDiff = -1.0;
                boolean addToNode = true;

                for (int i = 0; i < total; i++) {
                    if (!assigned[i]) {
                        double e1 = node.getMBR().enlargement(temp[i].getMBR());
                        double e2 = newNode.getMBR().enlargement(temp[i].getMBR());
                        double diff = Math.abs(e1 - e2);

                        if (diff > maxDiff) {
                            maxDiff = diff;
                            bestCandidate = i;
                            addToNode = (e1 < e2);
                        }
                    }
                }

                if (addToNode) {
                    node.addFacility(temp[bestCandidate]);
                } else {
                    newNode.addFacility(temp[bestCandidate]);
                }
                assigned[bestCandidate] = true;
                assignedCount++;
            }
        } else {
            // Internal Node Split logic
            int total = node.getCount();
            RTreeNode[] temp = new RTreeNode[total];
            for (int i = 0; i < total; i++) {
                temp[i] = node.getChildren()[i];
            }

            clearNodeChildren(node);

            int seed1 = 0, seed2 = 1;
            double maxWasted = -1.0;

            for (int i = 0; i < total; i++) {
                for (int j = i + 1; j < total; j++) {
                    Rectangle u = temp[i].getMBR().union(temp[j].getMBR());
                    double wasted = u.area() - temp[i].getMBR().area() - temp[j].getMBR().area();
                    if (wasted > maxWasted) {
                        maxWasted = wasted;
                        seed1 = i;
                        seed2 = j;
                    }
                }
            }

            node.addChild(temp[seed1]);
            newNode.addChild(temp[seed2]);

            boolean[] assigned = new boolean[total];
            assigned[seed1] = true;
            assigned[seed2] = true;
            int assignedCount = 2;

            while (assignedCount < total) {
                if (node.getCount() + (total - assignedCount) == minEntries) {
                    for (int i = 0; i < total; i++) {
                        if (!assigned[i]) {
                            node.addChild(temp[i]);
                            assigned[i] = true;
                            assignedCount++;
                        }
                    }
                    break;
                }
                if (newNode.getCount() + (total - assignedCount) == minEntries) {
                    for (int i = 0; i < total; i++) {
                        if (!assigned[i]) {
                            newNode.addChild(temp[i]);
                            assigned[i] = true;
                            assignedCount++;
                        }
                    }
                    break;
                }

                int bestCandidate = -1;
                double maxDiff = -1.0;
                boolean addToNode = true;

                for (int i = 0; i < total; i++) {
                    if (!assigned[i]) {
                        double e1 = node.getMBR().enlargement(temp[i].getMBR());
                        double e2 = newNode.getMBR().enlargement(temp[i].getMBR());
                        double diff = Math.abs(e1 - e2);

                        if (diff > maxDiff) {
                            maxDiff = diff;
                            bestCandidate = i;
                            addToNode = (e1 < e2);
                        }
                    }
                }

                if (addToNode) {
                    node.addChild(temp[bestCandidate]);
                } else {
                    newNode.addChild(temp[bestCandidate]);
                }
                assigned[bestCandidate] = true;
                assignedCount++;
            }
        }

        node.recalculateMBR();
        newNode.recalculateMBR();

        // Propagate split up the tree
        if (node == root) {
            RTreeNode newRoot = new RTreeNode(false, maxEntries);
            newRoot.addChild(node);
            newRoot.addChild(newNode);
            root = newRoot;
        } else {
            RTreeNode parent = findParent(root, node);
            parent.addChild(newNode);
            if (parent.getCount() > maxEntries) {
                splitNode(parent);
            } else {
                adjustMBRsUpward(parent);
            }
        }
    }

    private void clearNodeFacilities(RTreeNode node) {
        for (int i = 0; i < node.getFacilities().length; i++) {
            node.getFacilities()[i] = null;
        }
        node.resetCountAndMBR();
    }

    private void clearNodeChildren(RTreeNode node) {
        for (int i = 0; i < node.getChildren().length; i++) {
            node.getChildren()[i] = null;
        }
        node.resetCountAndMBR();
    }

    private RTreeNode findParent(RTreeNode current, RTreeNode targetChild) {
        if (current.isLeaf()) return null;

        for (int i = 0; i < current.getCount(); i++) {
            RTreeNode child = current.getChildren()[i];
            if (child == targetChild) {
                return current;
            }
            RTreeNode res = findParent(child, targetChild);
            if (res != null) return res;
        }
        return null;
    }

    private void adjustMBRsUpward(RTreeNode node) {
        RTreeNode current = node;
        while (current != null) {
            current.recalculateMBR();
            current = findParent(root, current);
        }
    }

    // =========================================================================
    // 2. RANGE SEARCHING
    // =========================================================================

    public EmergencyFacility[] searchRange(Rectangle searchWindow) {
        EmergencyFacility[] tempBuffer = new EmergencyFacility[1000];
        int[] resultCount = new int[1]; // Pass integer reference as single-element array
        searchRangeHelper(root, searchWindow, null, tempBuffer, resultCount);

        // Copy exact matching results to a trimmed array
        EmergencyFacility[] result = new EmergencyFacility[resultCount[0]];
        for (int i = 0; i < resultCount[0]; i++) {
            result[i] = tempBuffer[i];
        }
        return result;
    }

    public EmergencyFacility[] searchRangeWithType(Rectangle searchWindow, String typeFilter) {
        EmergencyFacility[] tempBuffer = new EmergencyFacility[1000];
        int[] resultCount = new int[1];
        searchRangeHelper(root, searchWindow, typeFilter, tempBuffer, resultCount);

        EmergencyFacility[] result = new EmergencyFacility[resultCount[0]];
        for (int i = 0; i < resultCount[0]; i++) {
            result[i] = tempBuffer[i];
        }
        return result;
    }

    private void searchRangeHelper(RTreeNode current, Rectangle window, String typeFilter,
                                   EmergencyFacility[] buffer, int[] countRef) {
        if (current.getMBR() == null || !current.getMBR().intersects(window)) {
            return; // PRUNING: Bypasses non-overlapping subtrees
        }

        if (current.isLeaf()) {
            for (int i = 0; i < current.getCount(); i++) {
                EmergencyFacility f = current.getFacilities()[i];
                if (window.containsPoint(f.getX(), f.getY())) {
                    if (typeFilter == null || f.getType().equalsIgnoreCase(typeFilter)) {
                        buffer[countRef[0]] = f;
                        countRef[0]++;
                    }
                }
            }
        } else {
            for (int i = 0; i < current.getCount(); i++) {
                searchRangeHelper(current.getChildren()[i], window, typeFilter, buffer, countRef);
            }
        }
    }

    // =========================================================================
    // 3. NEAREST NEIGHBOR (k-NN) SEARCH USING BASIC ARRAY SORTING
    // =========================================================================

    public EmergencyFacility[] searchNearest(double x, double y, int k) {
        EmergencyFacility[] allFacilities = new EmergencyFacility[1000];
        int[] countRef = new int[1];
        collectAllFacilities(root, allFacilities, countRef);

        int total = countRef[0];
        if (total == 0) return new EmergencyFacility[0];

        // Bubble sort facilities by Euclidean distance
        for (int i = 0; i < total - 1; i++) {
            for (int j = 0; j < total - i - 1; j++) {
                double d1 = allFacilities[j].distanceTo(x, y);
                double d2 = allFacilities[j + 1].distanceTo(x, y);
                if (d1 > d2) {
                    EmergencyFacility temp = allFacilities[j];
                    allFacilities[j] = allFacilities[j + 1];
                    allFacilities[j + 1] = temp;
                }
            }
        }

        int resultSetSize = Math.min(k, total);
        EmergencyFacility[] result = new EmergencyFacility[resultSetSize];
        for (int i = 0; i < resultSetSize; i++) {
            result[i] = allFacilities[i];
        }
        return result;
    }

    private void collectAllFacilities(RTreeNode current, EmergencyFacility[] buffer, int[] countRef) {
        if (current == null) return;

        if (current.isLeaf()) {
            for (int i = 0; i < current.getCount(); i++) {
                buffer[countRef[0]] = current.getFacilities()[i];
                countRef[0]++;
            }
        } else {
            for (int i = 0; i < current.getCount(); i++) {
                collectAllFacilities(current.getChildren()[i], buffer, countRef);
            }
        }
    }

    // =========================================================================
    // 4. DELETION LOGIC
    // =========================================================================

    public boolean delete(String facilityId) {
        EmergencyFacility target = findFacilityById(root, facilityId);
        if (target == null) return false;

        RTreeNode leaf = findLeafWithFacility(root, target);
        if (leaf == null) return false;

        // Find index and remove
        for (int i = 0; i < leaf.getCount(); i++) {
            if (leaf.getFacilities()[i] == target) {
                leaf.removeFacilityAt(i);
                break;
            }
        }

        condenseTree(leaf);

        // Collapse root if reduced to a single child
        if (!root.isLeaf() && root.getCount() == 1) {
            root = root.getChildren()[0];
        } else if (!root.isLeaf() && root.getCount() == 0) {
            root = new RTreeNode(true, maxEntries);
        }
        return true;
    }

    private EmergencyFacility findFacilityById(RTreeNode current, String id) {
        if (current.isLeaf()) {
            for (int i = 0; i < current.getCount(); i++) {
                EmergencyFacility f = current.getFacilities()[i];
                if (f.getId().equalsIgnoreCase(id)) return f;
            }
        } else {
            for (int i = 0; i < current.getCount(); i++) {
                EmergencyFacility f = findFacilityById(current.getChildren()[i], id);
                if (f != null) return f;
            }
        }
        return null;
    }

    private RTreeNode findLeafWithFacility(RTreeNode current, EmergencyFacility target) {
        if (current.isLeaf()) {
            for (int i = 0; i < current.getCount(); i++) {
                if (current.getFacilities()[i] == target) return current;
            }
        } else {
            for (int i = 0; i < current.getCount(); i++) {
                RTreeNode child = current.getChildren()[i];
                if (child.getMBR() != null && child.getMBR().containsPoint(target.getX(), target.getY())) {
                    RTreeNode res = findLeafWithFacility(child, target);
                    if (res != null) return res;
                }
            }
        }
        return null;
    }

    private void condenseTree(RTreeNode node) {
        RTreeNode current = node;
        EmergencyFacility[] orphanedFacilities = new EmergencyFacility[100];
        int orphanCount = 0;

        while (current != root) {
            RTreeNode parent = findParent(root, current);
            if (current.getCount() < minEntries) {
                // Remove underfull child node from parent
                for (int i = 0; i < parent.getCount(); i++) {
                    if (parent.getChildren()[i] == current) {
                        parent.removeChildAt(i);
                        break;
                    }
                }
                // Collect entries to reinsert
                orphanCount = collectOrphans(current, orphanedFacilities, orphanCount);
            } else {
                current.recalculateMBR();
            }
            current = parent;
        }
        root.recalculateMBR();

        // Reinsert orphaned facilities back into the tree
        for (int i = 0; i < orphanCount; i++) {
            insert(orphanedFacilities[i]);
        }
    }

    private int collectOrphans(RTreeNode node, EmergencyFacility[] orphans, int currentOrphanCount) {
        if (node.isLeaf()) {
            for (int i = 0; i < node.getCount(); i++) {
                orphans[currentOrphanCount] = node.getFacilities()[i];
                currentOrphanCount++;
            }
        } else {
            for (int i = 0; i < node.getCount(); i++) {
                currentOrphanCount = collectOrphans(node.getChildren()[i], orphans, currentOrphanCount);
            }
        }
        return currentOrphanCount;
    }
}