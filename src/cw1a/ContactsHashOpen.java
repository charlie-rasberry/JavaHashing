package cw1a;

/**
 *
 * @author DL 2025-01
 */
public class ContactsHashOpen implements IContactDB {  
    private final int initialTableCapacity = 1009;

    private Contact[] table;
    private int tableCapacity;
    private int numEntries;
    private int totalVisited = 0;
    

    private static final double maxLoadFactor = 50.0;   //  changed from 70 to 50 for quadratic probing
            
    public int getNumEntries(){return numEntries;}
    public void resetTotalVisited() {totalVisited = 0;}
    public int getTotalVisited() {return totalVisited;}

    public ContactsHashOpen() {
        System.out.println("Hash Table with open addressing");
        this.tableCapacity = initialTableCapacity;
        table = new Contact[tableCapacity];
        clearDB();
    }

    /**
     * Empties the database.
     *
     * @pre true
     */
    public void clearDB() {
        for (int i = 0; i != table.length; i++) {
            table[i] = null;
        }
        numEntries = 0;
    }

    private int hash(String s) {
        assert  s != null && !s.trim().equals(""); 

        int hash = 0;
        //  prime number needed as multiplier
        int MULTIPLIER = 19;
        for (int i = 0; i < s.length(); i++) {
            hash = (hash * MULTIPLIER + s.charAt(i)) % table.length;
        }
        return Math.abs(hash);
        //  Original -- return Math.abs((s.charAt(0)) % table.length);
        
    }

    private double loadFactor() {   //  ignore deleted markers
        int actualEntries = 0;
        for (Contact contact : table) {
            if (contact != null && contact != DELETED) {
                actualEntries++;
            }
        }
        return (double) actualEntries / (double) table.length * 100.0;
        // note need for cast to double
    }

    private int findPos(String name) {
        assert name != null && !name.trim().equals(""); //  Check empty

        int pos = hash(name);
        int numVisited = 1;
        int offset = 1;
        int firstDeletedIndex = -1;

        System.out.println("finding " + pos + ": " + name );

        while (table[pos] != null && !name.equals(table[pos].getName())) {
            System.out.println("Visiting bucket " + pos + ": " + table[pos]);

            if (table[pos] == DELETED && firstDeletedIndex == -1) {
                firstDeletedIndex = pos;
            }

            //  check if name

            pos = (pos + offset) % table.length;     // quadratic probing
            offset += 2;
            numVisited++;

            if (numVisited > table.length) {
                break;
            }
        }
        System.out.println("number of  buckets visited = " + numVisited);
        totalVisited += numVisited;
      
        return firstDeletedIndex != -1 ? firstDeletedIndex : pos;
    }

    private boolean isPrime(int n) {
        if (n <=1) return false;
        if (n <= 3) return true;

        //  Check from 2 to sqrt of n
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    private int nextPrime(int n) {
        while (!isPrime(n)) {
            n++;
        }
        return n;
    }
    /**
     * Determines whether a Contact name exists as a key inside the database
     *
     * @pre name not null or empty string
     * @param name the Contact name (key) to locate
     * @return true iff the name exists as a key in the database
     */
    public boolean containsName(String name) {
        assert name != null && !name.equals("");
        int pos = findPos(name);
        return get(name) != null;
    }

    /**
     * Returns a Contact object mapped to the supplied name.
     *
     * @pre name not null or empty string
     * @param name The Contact name (key) to locate
     * @return the Contact object mapped to the key name if the name exists as
     * key in the database, otherwise null
     */
    @Override
    public Contact get(String name) {
        assert name != null && !name.trim().equals("");
        Contact result;
        int pos = findPos(name);       
        if (table[pos] == null) {
            result = null; // not found
        } else {
            result = table[pos];
        }
        return result;
    }

    /**
     * Returns the number of contacts in the database
     *
     * @pre true
     * @return number of contacts in the database. 0 if empty
     */
    public int size() {return numEntries; }

    /**
     * Determines if the database is empty or not.
     *
     * @pre true
     * @return true iff the database is empty
     */
    @Override
    public boolean isEmpty() {return numEntries == 0; }

    
    private Contact putWithoutResizing(Contact contact) {
        String name = contact.getName();
        int pos = findPos(name);
        Contact previous;
        //  check if empty or deleted
        if (table[pos] == null || table[pos] == DELETED) {
            table[pos] = contact;
            if (table[pos] != DELETED) {    //  increment if it's not an overwritten entry
                numEntries++;
            } else {
                System.out.println("Replaced DELETED Marker");
            }
            previous = null;
        } else {    //  overwrite existing entry
            previous = table[pos];
            table[pos] = contact;
            System.out.println("Overwriting Entry");
        }
        return previous;
    }
    
    /**
     * Inserts a contact object into the database, with the key of the supplied
     * contact's name. Note: If the name already exists as a key, then then the
     * original entry is overwritten. This method should return the previous
     * associated value if one exists, otherwise null
     *
     * @pre contact not null or empty string
     */
    public Contact put(Contact contact) {
        assert contact != null;
        Contact previous;
        String name = contact.getName();
        assert name != null && !name.trim().equals("");

        previous =  putWithoutResizing(contact);

        if (previous == null && loadFactor() > maxLoadFactor) resizeTable();
        return previous;
    }

    /**
     * Removes and returns a contact from the database, with the key the
     * supplied name.
     *
     * @param name The name (key) to remove.
     * @pre name not null or empty string
     * @return the removed contact object mapped to the name, or null if the
     * name does not exist.
     */
    //  identify deleted entries by marker
    public static final Contact DELETED = new Contact("DELETED", "MARKER");
    public Contact remove(String name) {
        assert name != null && !name.trim().equals("");

        int pos = findPos(name);

        if (table[pos] == null) {
            return null;
        }

        Contact removedContact = table[pos];
        table[pos] = DELETED;

        numEntries--;
        
        return removedContact;
    }

    /**
     * Prints the names and IDs of all the contacts in the database in
     * alphabetic order.
     *
     * @pre true
     */
    public void displayDB() {
        // not yet ordered
        System.out.println("capacity " + table.length + " size " + numEntries
                + " Load factor " + loadFactor() + "%");
        for (int i = 0; i != table.length; i++) {
            if (table[i] != null && table[i] != DELETED)
                System.out.println(i + " " + table[i].toString());
            else if (table[i] == DELETED)
                System.out.println(i + " DELETED");
            else
                 System.out.println(i + " " + "_____");
            }
        
        
        Contact[] toBeSortedTable = new Contact[tableCapacity];  // OK to use Array.sort
        int j = 0;
        for (int i = 0; i != table.length; i++) {
            if (table[i] != null) {
                toBeSortedTable[j] = table[i];
                j++;
            }
        }
        quicksort(toBeSortedTable, 0, j - 1);
        for (int i = 0; i != j; i++) {
            System.out.println(i + " " + " " + toBeSortedTable[i].toString());
        }
    }

    private void quicksort(Contact[] a, int low, int high) {
        assert a != null && 0 <= low && low <= high && high < a.length;
        int i = low, j = high;
        Contact temp;
        if (high >= 0) { // can't get pivot for empty sequence
            String pivot = a[(low + high) / 2].getName();
            while (i <= j) {
                while (a[i].getName().compareTo(pivot) < 0) i++;
                while (a[j].getName().compareTo(pivot) > 0) j--;
                // forall k :low ..i -1: a[k] < pivot && 
                // forall k: j+1 .. high: a[k] > pivot &&
                // a[i] >= pivot && a[j] <= pivot
                if (i <= j) {
                    temp = a[i]; a[i] = a[j]; a[j] = temp;
                    i++; j--;
                }
                if (low < j) quicksort(a, low, j); // recursive call 
                if (i < high) quicksort(a, i, high); // recursive call 
            }
        }
    }

    private void resizeTable() { // mkae a new table of greater capacity and rehashes old values into it
        System.out.println("RESIZING");
        Contact[] oldTable = table; // copy the reference
        int oldTableCapacity = tableCapacity;

        //  increase to the following primes
        tableCapacity = nextPrime(oldTableCapacity * 2);

        System.out.println("resizing to " + tableCapacity);
        table = new Contact[tableCapacity]; // make a new tyable

        clearDB();
        numEntries = 0;

        for (int i = 0; i < oldTableCapacity; i++) {
            if (oldTable[i] != null) { // dleted vakues not hashed across
                putWithoutResizing(oldTable[i]);
            }
        }
        
    }
} 

