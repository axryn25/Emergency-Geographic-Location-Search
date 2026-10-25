

public class RTreeNode {
    private boolean isLeaf;
    private RTreeNode[] children;           // Used if internal node
    private EmergencyFacility[] facilities; // Used if leaf node
    private int count;                      // Current number of entries stored
    private Rectangle mbr;                  // Minimum Bounding Rectangle enclosing all entries

    public RTreeNode(boolean isLeaf, int maxEntries) {
        this.isLeaf = isLeaf;
        this.count = 0;
        this.mbr = null;

        // Allocate maxEntries + 1 so we can hold an overflow entry during splits
        if (isLeaf) {
            this.facilities = new EmergencyFacility[maxEntries + 1];
            this.children = null;
        } else {
            this.children = new RTreeNode[maxEntries + 1];
            this.facilities = null;
        }
    }

    public boolean isLeaf() { return isLeaf; }
    public int getCount() { return count; }
    public Rectangle getMBR() { return mbr; }
    public RTreeNode[] getChildren() { return children; }
    public EmergencyFacility[] getFacilities() { return facilities; }

    // Adds a facility to a leaf node
    public void addFacility(EmergencyFacility facility) {
        facilities[count] = facility;
        count++;
        recalculateMBR();
    }

    // Adds a child node reference to an internal node
    public void addChild(RTreeNode child) {
        children[count] = child;
        count++;
        recalculateMBR();
    }

    // Removes facility at a specific array index
    public void removeFacilityAt(int index) {
        for (int i = index; i < count - 1; i++) {
            facilities[i] = facilities[i + 1];
        }
        facilities[count - 1] = null;
        count--;
        recalculateMBR();
    }

    // Removes child node at a specific array index
    public void removeChildAt(int index) {
        for (int i = index; i < count - 1; i++) {
            children[i] = children[i + 1];
        }
        children[count - 1] = null;
        count--;
        recalculateMBR();
    }
    
    public void resetCountAndMBR() {
        this.count = 0;
        this.mbr = null;
    }
    // Recalculates the smallest bounding rectangle that encloses all entries
    public void recalculateMBR() {
        if (count == 0) {
            mbr = null;
            return;
        }

        if (isLeaf) {
            Rectangle temp = facilities[0].getMBR();
            for (int i = 1; i < count; i++) {
                temp = temp.union(facilities[i].getMBR());
            }
            this.mbr = temp;
        } else {
            Rectangle temp = children[0].getMBR();
            for (int i = 1; i < count; i++) {
                temp = temp.union(children[i].getMBR());
            }
            this.mbr = temp;
        }
    }
}
