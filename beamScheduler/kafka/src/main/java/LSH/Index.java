/*
 *      _______                       _        ____ _     _
 *     |__   __|                     | |     / ____| |   | |
 *        | | __ _ _ __ ___  ___  ___| |    | (___ | |___| |
 *        | |/ _` | '__/ __|/ _ \/ __| |     \___ \|  ___  |
 *        | | (_| | |  \__ \ (_) \__ \ |____ ____) | |   | |
 *        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|   |_|
 *
 * -----------------------------------------------------------
 *
 *  TarsosLSH is developed by Joren Six.
 *
 * -----------------------------------------------------------
 *
 *  Info    : http://0110.be/tag/TarsosLSH
 *  Github  : https://github.com/JorenSix/TarsosLSH
 *  Releases: http://0110.be/releases/TarsosLSH/
 *
 */

package LSH;

import LSH.families.DistanceComparator;
import LSH.families.DistanceMeasure;
import LSH.families.HashFamily;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * The index makes it easy to store vectors and lookup queries efficiently. For
 * the moment the index is stored in memory. It holds a number of hash tables,
 * each with a couple of hashes. Together they can be used for efficient lookup
 * of nearest neighbours.
 *
 * @author Joren Six
 */
public class Index implements Serializable {

    private static final long serialVersionUID = 3757702142917691272L;

    private final static Logger LOG = Logger.getLogger(Index.class.getName());

    private HashFamily family;
    private List<HashTable> hashTable;
    private int evaluated;

    /**
     * Create a new index.
     *
     * @param family             The family of hash functions to use.
     * @param numberOfHashes     The number of hashes that are concatenated in each hash table.
     *                           More concatenated hashes means that less candidates are
     *                           selected for evaluation.
     * @param numberOfHashTables The number of hash tables in use, recall increases with the
     *                           number of hash tables. Memory use also increases. Time needed
     *                           to compute a hash also increases marginally.
     */
    public Index(HashFamily family, int numberOfHashes, int numberOfHashTables) {
        this.family = family;
        hashTable = new ArrayList<HashTable>();
        for (int i = 0; i < numberOfHashTables; i++) {
            hashTable.add(new HashTable(numberOfHashes, family));
        }
        evaluated = 0;
    }

    /**
     * Add a vector to the current index. The hashes are calculated with the
     * current hash family and added in the right place.
     *
     * @param vector The vector to add.
     */
    public void index(Vector vector) {
        for (HashTable table : hashTable) {
            table.add(vector);
        }
    }

    /**
     * The number of hash tables used in the current index.
     *
     * @return The number of hash tables used in the current index.
     */
    public int getNumberOfHashTables() {
        return hashTable.size();
    }

    /**
     * The number of hashes used in each hash table in the current index.
     *
     * @return The number of hashes used in each hash table in the current index.
     */
    public int getNumberOfHashes() {
        return hashTable.get(0).getNumberOfHashes();
    }

    /**
     * Query for the k nearest neighbours in using the current index. The
     * performance (in computing time and recall/precision) depends mainly on
     * how the current index is constructed and how the underlying data looks.
     *
     * @param query   The query vector. The center of the neighbourhood.
     * @param maxSize The maximum number of neighbours to return. Beware, the number
     *                of neighbours returned lays between zero and the chosen
     *                maximum.
     * @return A list of nearest neighbours, the number of neighbours returned
     * lays between zero and a chosen maximum.
     */
    public List<Vector> query(final Vector query, int maxSize) {
        Set<Vector> candidateSet = new HashSet<Vector>();
        for (HashTable table : hashTable) {
            List<Vector> v = table.query(query);
            candidateSet.addAll(v);
        }
        List<Vector> candidates = new ArrayList<Vector>(candidateSet);
        evaluated += candidates.size();
        DistanceMeasure measure = family.createDistanceMeasure();
        DistanceComparator dc = new DistanceComparator(query, measure);
        Collections.sort(candidates, dc);
        if (candidates.size() > maxSize) {
            candidates = candidates.subList(0, maxSize);
        }
        return candidates;
    }

    /**
     * The number of near neighbour candidates that are evaluated during the queries on this index.
     * Can be used to calculate the average evaluations per query.
     *
     * @return The number of near neighbour candidates that are evaluated during the queries on this index.
     */
    public int getTouched() {
        return evaluated;
    }
}


