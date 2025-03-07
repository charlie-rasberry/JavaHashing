package cw1a;

import java.util.ArrayList;
import java.util.LinkedList;

public class ContactsHashChained implements IContactDB {
    private final int initialTableCapacity = 1009;
    private ArrayList<LinkedList<Contact>> table;
    private int numEntries;
    private int totalVisited = 0;

    //  required
    public int getNumEntries(){return numEntries;}
    public void resetTotalVisited() {totalVisited = 0;}
    public int getTotalVisited() {return totalVisited;}


    public ContactsHashChained() {
        System.out.println("Hash Table with chaining");
        table = new ArrayList<>(initialTableCapacity);

        // initialize all chains
        for (int i = 0; i < initialTableCapacity; i++) {
            table.add(new LinkedList<>());
        }
        numEntries = 0;
    }

    /**
     * Empties the database.
     *
     * @pre true
     */
    public void clearDB() {
        for (LinkedList<Contact> chain : table) {
            chain.clear();
        }
        numEntries = 0;
    }

    private int hash(String s) {
        assert s != null && !s.trim().equals("");

        int hash = 0;
        //  prime number needed as multiplier
        int MULTIPLIER = 19;
        for (int i = 0; i < s.length(); i++) {
            hash = (hash * MULTIPLIER + s.charAt(i)) % table.size();
        }
        return Math.abs(hash);
        //  Original -- return Math.abs((s.charAt(0)) % table.length);
    }

    public boolean containsName(String name) {
        assert name != null && !name.equals("");
        return get(name) != null;
    }

    public Contact get(String name) {
        assert name != null && !name.trim().equals("");

        int hashValue = hash(name);
        LinkedList<Contact> chain = table.get(hashValue);
        int numVisited = 1;

        System.out.println("finding " + hashValue + ": " + name);
        //  search through chain for matching name
        for (Contact contact : chain) {
            System.out.println("Visiting chain entry: " + contact);
            numVisited++;

            if (name.equals(contact.getName())) {
                System.out.println("number of entries visited = " + numVisited);
                totalVisited += numVisited;
                return contact;
            }
        }

        System.out.println("number of entries visited = " + numVisited);
        totalVisited += numVisited;
        return null;    //  If not found
    }

    public Contact put(Contact contact) {
        assert contact != null;
        String name = contact.getName();
        assert name != null && !name.trim().equals("");

        int hashValue = hash(name);
        LinkedList<Contact> chain = table.get(hashValue);
        int numVisited = 1;

        System.out.println("finding " + hashValue + ": " + name);

        //  look for existing entry to replace
        for (int i = 0; i < chain.size(); i++) {
            Contact existing = chain.get(i);
            numVisited++;

            if (name.equals(existing.getName())) {
                //  Replace existing
                Contact previous = chain.set(i, contact);
                System.out.println(hashValue + " Entries Visited = " + numVisited);
                totalVisited += numVisited;
                return previous;
            }
        }

        // If not found add new entry
        chain.add(contact);
        numEntries++;
        System.out.println(hashValue + " number of entries visited = " + numVisited);
        totalVisited += numVisited;
        return null;
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

    public void displayDB() {
        System.out.println("capacity " + table.size() + " size " + numEntries);

        //  First show the chains
        for (int i = 0; i < table.size(); i++) {
            LinkedList<Contact> chain = table.get(i);
            if (!chain.isEmpty()) {
                System.out.println("Bucket " + i + " (" + chain.size() + " items):");
                for (Contact contact : chain) {
                    System.out.println("  " + contact.toString());
                }
            }
        }

        //  show sorted view same as
        ArrayList<Contact> allContacts = new ArrayList<>(numEntries);
        for (LinkedList<Contact> chain : table) {
            allContacts.addAll(chain);
        }

        Contact[] toBeSortedArray = allContacts.toArray(new Contact[0]);
        quicksort(toBeSortedArray, 0, toBeSortedArray.length - 1);

        System.out.println("\nSorted contacts:");
        for (int i = 0; i < toBeSortedArray.length; i++) {
            System.out.println(i + " " + toBeSortedArray[i].toString());
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

    public Contact remove(String name) {
        System.out.println("remove not implemented");
        return null;
    }
}




