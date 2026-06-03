import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

// ══════════════════════════════════════════════════════════════════════════════
//  Song  –  immutable data model for a single track
// ══════════════════════════════════════════════════════════════════════════════
class Song {
    private final String title;
    private final String artist;
    private final String album;
    private final int    durationSeconds;

    public Song(String title, String artist, String album, int durationSeconds) {
        if (title  == null || title.isBlank())  throw new IllegalArgumentException("Title cannot be empty.");
        if (artist == null || artist.isBlank()) throw new IllegalArgumentException("Artist cannot be empty.");
        if (durationSeconds <= 0)               throw new IllegalArgumentException("Duration must be positive.");

        this.title           = title.trim();
        this.artist          = artist.trim();
        this.album           = (album != null && !album.isBlank()) ? album.trim() : "Unknown Album";
        this.durationSeconds = durationSeconds;
    }

    public String getTitle()           { return title;           }
    public String getArtist()          { return artist;          }
    public String getAlbum()           { return album;           }
    public int    getDurationSeconds() { return durationSeconds; }

    public String getFormattedDuration() {
        return String.format("%d:%02d", durationSeconds / 60, durationSeconds % 60);
    }

    @Override
    public String toString() {
        return String.format("%-35s %-25s %-25s %s",
                "\"" + title + "\"", artist, album, getFormattedDuration());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Song other)) return false;
        return title.equalsIgnoreCase(other.title) &&
               artist.equalsIgnoreCase(other.artist);
    }

    @Override
    public int hashCode() {
        return title.toLowerCase().hashCode() * 31 + artist.toLowerCase().hashCode();
    }
}


// ══════════════════════════════════════════════════════════════════════════════
//  Node  –  doubly-linked node used by PlaylistLinkedList
// ══════════════════════════════════════════════════════════════════════════════
class Node {
    Song song;
    Node next;
    Node prev;

    Node(Song song) { this.song = song; }
}


// ══════════════════════════════════════════════════════════════════════════════
//  PlaylistLinkedList  –  custom doubly-linked list (ordered playback store)
// ══════════════════════════════════════════════════════════════════════════════
class PlaylistLinkedList implements Iterable<Song> {

    private Node head;
    private Node tail;
    private int  size;

    // ── Insert ────────────────────────────────────────────────────────────────
    public void addLast(Song song) {
        requireNonNull(song);
        Node n = new Node(song);
        if (isEmpty()) { head = tail = n; }
        else           { tail.next = n; n.prev = tail; tail = n; }
        size++;
    }

    public void addFirst(Song song) {
        requireNonNull(song);
        Node n = new Node(song);
        if (isEmpty()) { head = tail = n; }
        else           { n.next = head; head.prev = n; head = n; }
        size++;
    }

    public void addAt(int index, Song song) {
        checkInsertIndex(index);
        if (index == 0)    { addFirst(song); return; }
        if (index == size) { addLast(song);  return; }
        Node cur = nodeAt(index - 1);
        Node n   = new Node(song);
        n.next   = cur.next;
        n.prev   = cur;
        if (cur.next != null) cur.next.prev = n;
        cur.next = n;
        size++;
    }

    // ── Remove ────────────────────────────────────────────────────────────────
    public boolean removeByTitle(String title) {
        for (Node cur = head; cur != null; cur = cur.next) {
            if (cur.song.getTitle().equalsIgnoreCase(title)) { unlink(cur); return true; }
        }
        return false;
    }

    public Song removeAt(int index) {
        checkAccessIndex(index);
        Node target = nodeAt(index);
        unlink(target);
        return target.song;
    }

    public Song removeFirst() {
        if (isEmpty()) throw new NoSuchElementException("Playlist is empty.");
        return removeAt(0);
    }

    public Song removeLast() {
        if (isEmpty()) throw new NoSuchElementException("Playlist is empty.");
        return removeAt(size - 1);
    }

    // ── Search (linear – used for artist lookup) ──────────────────────────────
    public Song searchByArtist(String artist) {
        for (Node cur = head; cur != null; cur = cur.next)
            if (cur.song.getArtist().equalsIgnoreCase(artist)) return cur.song;
        return null;
    }

    // ── Random access & utilities ─────────────────────────────────────────────
    public Song    get(int index)   { checkAccessIndex(index); return nodeAt(index).song; }
    public int     size()           { return size; }
    public boolean isEmpty()        { return size == 0; }
    public void    clear()          { head = tail = null; size = 0; }

    public boolean contains(Song song) {
        for (Song s : this) if (s.equals(song)) return true;
        return false;
    }

    public int totalDurationSeconds() {
        int total = 0;
        for (Song s : this) total += s.getDurationSeconds();
        return total;
    }

    public String formattedTotalDuration() {
        int t = totalDurationSeconds();
        int h = t / 3600, m = (t % 3600) / 60, s = t % 60;
        return h > 0 ? String.format("%dh %dm %ds", h, m, s) : String.format("%dm %ds", m, s);
    }

    @Override
    public Iterator<Song> iterator() {
        return new Iterator<>() {
            Node cur = head;
            public boolean hasNext() { return cur != null; }
            public Song    next()    {
                if (!hasNext()) throw new NoSuchElementException();
                Song s = cur.song; cur = cur.next; return s;
            }
        };
    }

    // ── Private helpers ───────────────────────────────────────────────────────
    private Node nodeAt(int index) {
        Node cur;
        if (index < size / 2) {
            cur = head; for (int i = 0; i < index; i++) cur = cur.next;
        } else {
            cur = tail; for (int i = size - 1; i > index; i--) cur = cur.prev;
        }
        return cur;
    }

    private void unlink(Node n) {
        if (n.prev != null) n.prev.next = n.next; else head = n.next;
        if (n.next != null) n.next.prev = n.prev; else tail = n.prev;
        n.prev = n.next = null;
        size--;
    }

    private void requireNonNull(Song s) {
        if (s == null) throw new IllegalArgumentException("Song cannot be null.");
    }

    private void checkAccessIndex(int i) {
        if (i < 0 || i >= size)
            throw new IndexOutOfBoundsException("Index " + i + " out of bounds for size " + size);
    }

    private void checkInsertIndex(int i) {
        if (i < 0 || i > size)
            throw new IndexOutOfBoundsException("Insert index " + i + " out of bounds for size " + size);
    }
}


// ══════════════════════════════════════════════════════════════════════════════
//  BSTNode  –  node in the Binary Search Tree, keyed on song title (lowercase)
// ══════════════════════════════════════════════════════════════════════════════
class BSTNode {
    String  key;        // normalised (lowercase) title – the BST ordering key
    Song    song;       // payload
    BSTNode left;
    BSTNode right;

    BSTNode(Song song) {
        this.key  = song.getTitle().toLowerCase();
        this.song = song;
    }
}


// ══════════════════════════════════════════════════════════════════════════════
//  SongBST  –  Binary Search Tree keyed on song title
//
//  Why a BST here?
//  ┌─────────────────────┬──────────────┬──────────────┐
//  │ Operation           │ Linked list  │ BST (avg)    │
//  ├─────────────────────┼──────────────┼──────────────┤
//  │ searchByTitle       │   O(n)       │   O(log n)   │
//  │ insert              │   O(1) tail  │   O(log n)   │
//  │ delete              │   O(n)       │   O(log n)   │
//  │ in-order (sorted)   │   O(n log n) │   O(n)       │
//  └─────────────────────┴──────────────┴──────────────┘
//
//  The linked list keeps playback ORDER; the BST accelerates title LOOKUP.
//  Both structures stay in sync through Playlist.addSong / removeSong.
// ══════════════════════════════════════════════════════════════════════════════
class SongBST {

    private BSTNode root;
    private int     size;

    // ── Insert ────────────────────────────────────────────────────────────────
    /**
     * Inserts a song into the BST.
     * Duplicate titles (same key) overwrite the existing node's song payload
     * so the tree never holds stale data after an update.
     */
    public void insert(Song song) {
        root = insertRec(root, song);
    }

    private BSTNode insertRec(BSTNode node, Song song) {
        if (node == null) { size++; return new BSTNode(song); }
        int cmp = song.getTitle().compareToIgnoreCase(node.song.getTitle());
        if      (cmp < 0) node.left  = insertRec(node.left,  song);
        else if (cmp > 0) node.right = insertRec(node.right, song);
        else              node.song  = song;   // same title → update payload
        return node;
    }

    // ── Search by exact title  O(log n) average ───────────────────────────────
    /**
     * Searches the BST for a song whose title matches exactly (case-insensitive).
     * Average: O(log n)  |  Worst (degenerate tree): O(n)
     */
    public Song searchByTitle(String title) {
        return searchRec(root, title.toLowerCase());
    }

    private Song searchRec(BSTNode node, String key) {
        if (node == null) return null;
        int cmp = key.compareTo(node.key);
        if      (cmp < 0) return searchRec(node.left,  key);
        else if (cmp > 0) return searchRec(node.right, key);
        else              return node.song;            // exact match
    }

    // ── Prefix / autocomplete search  O(n) ───────────────────────────────────
    /**
     * Returns every song whose title STARTS WITH the given prefix.
     * Useful for autocomplete. Traverses only the relevant sub-tree first,
     * then collects all matches in that subtree – still O(n) worst-case but
     * prunes irrelevant branches early.
     */
    public List<Song> searchByPrefix(String prefix) {
        List<Song> results = new ArrayList<>();
        prefixRec(root, prefix.toLowerCase(), results);
        return results;
    }

    private void prefixRec(BSTNode node, String prefix, List<Song> results) {
        if (node == null) return;
        int cmp = node.key.compareTo(prefix);

        // This node's key is >= prefix: left subtree may still have matches
        if (cmp >= 0) prefixRec(node.left, prefix, results);

        // Collect this node if its key starts with the prefix
        if (node.key.startsWith(prefix)) {
            results.add(node.song);
            // Right subtree may have more prefix matches only if key == prefix so far
            prefixRec(node.right, prefix, results);
        } else if (cmp < 0) {
            // This node's key < prefix, so only the right subtree can match
            prefixRec(node.right, prefix, results);
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────
    /**
     * Removes the song with the given title from the BST.
     * Uses the in-order successor (smallest node in right subtree) strategy
     * for nodes with two children, preserving BST invariants.
     */
    public boolean delete(String title) {
        int before = size;
        root = deleteRec(root, title.toLowerCase());
        return size < before;
    }

    private BSTNode deleteRec(BSTNode node, String key) {
        if (node == null) return null;           // not found
        int cmp = key.compareTo(node.key);
        if      (cmp < 0) { node.left  = deleteRec(node.left,  key); }
        else if (cmp > 0) { node.right = deleteRec(node.right, key); }
        else {
            // ── Found the node to delete ──────────────────────────────────
            size--;
            if (node.left  == null) return node.right;  // 0 or 1 child
            if (node.right == null) return node.left;   // 1 child

            // 2 children: replace with in-order successor (min of right subtree)
            BSTNode successor = findMin(node.right);
            node.key  = successor.key;
            node.song = successor.song;
            node.right = deleteRec(node.right, successor.key);
        }
        return node;
    }

    private BSTNode findMin(BSTNode node) {
        while (node.left != null) node = node.left;
        return node;
    }

    // ── In-order traversal (alphabetical by title) ────────────────────────────
    /**
     * Returns all songs sorted alphabetically by title via in-order traversal.
     * This is a free O(n) operation – no separate sort needed.
     */
    public List<Song> inOrderSorted() {
        List<Song> sorted = new ArrayList<>();
        inOrderRec(root, sorted);
        return sorted;
    }

    private void inOrderRec(BSTNode node, List<Song> list) {
        if (node == null) return;
        inOrderRec(node.left,  list);
        list.add(node.song);
        inOrderRec(node.right, list);
    }

    // ── Tree shape visualiser (for debugging / demo) ──────────────────────────
    /**
     * Prints a sideways representation of the BST (right subtree on top).
     * Each level is indented by 4 spaces; useful to verify tree balance.
     *
     *   Example output (right is "up"):
     *       "Stairway to Heaven"
     *   "Imagine"
     *           "Hotel California"
     *       "Bohemian Rhapsody"
     */
    public void printTree() {
        if (root == null) { System.out.println("  (empty BST)"); return; }
        printTreeRec(root, 0);
    }

    private void printTreeRec(BSTNode node, int depth) {
        if (node == null) return;
        printTreeRec(node.right, depth + 1);
        System.out.println("    ".repeat(depth) + "\"" + node.song.getTitle() + "\"");
        printTreeRec(node.left,  depth + 1);
    }

    // ── Utility ───────────────────────────────────────────────────────────────
    public int     size()    { return size;    }
    public boolean isEmpty() { return size == 0; }
    public void    clear()   { root = null; size = 0; }
}


// ══════════════════════════════════════════════════════════════════════════════
//  Playlist  –  high-level facade that keeps the linked list and BST in sync
//
//  Linked list  →  preserves insertion / playback ORDER
//  BST          →  accelerates title SEARCH & provides sorted traversal
// ══════════════════════════════════════════════════════════════════════════════
class Playlist {

    private final String             name;
    private final PlaylistLinkedList songs;   // ordered store
    private final SongBST            titleBST; // O(log n) title lookup

    public Playlist(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Playlist name cannot be empty.");
        this.name     = name.trim();
        this.songs    = new PlaylistLinkedList();
        this.titleBST = new SongBST();
    }

    // ── Mutation (keeps both structures in sync) ──────────────────────────────
    public void addSong(Song song) {
        songs.addLast(song);
        titleBST.insert(song);
    }

    public void addSongFirst(Song song) {
        songs.addFirst(song);
        titleBST.insert(song);
    }

    public void addSongAt(int index, Song song) {
        songs.addAt(index, song);
        titleBST.insert(song);
    }

    public boolean removeSongByTitle(String title) {
        boolean removedFromList = songs.removeByTitle(title);
        if (removedFromList) titleBST.delete(title);   // keep BST in sync
        return removedFromList;
    }

    public Song removeSongAt(int index) {
        Song removed = songs.removeAt(index);
        titleBST.delete(removed.getTitle());
        return removed;
    }

    public Song removeFirstSong() {
        Song removed = songs.removeFirst();
        titleBST.delete(removed.getTitle());
        return removed;
    }

    public Song removeLastSong() {
        Song removed = songs.removeLast();
        titleBST.delete(removed.getTitle());
        return removed;
    }

    public void clear() {
        songs.clear();
        titleBST.clear();
    }

    // ── Search ────────────────────────────────────────────────────────────────

    /**
     * Searches by exact title using the BST → O(log n) average.
     */
    public Song searchByTitle(String title) {
        return titleBST.searchByTitle(title);
    }

    /**
     * Autocomplete: returns all songs whose title starts with the given prefix.
     * Powered by BST prefix traversal.
     */
    public List<Song> searchByPrefix(String prefix) {
        return titleBST.searchByPrefix(prefix);
    }

    /**
     * Searches by artist using the linked list → O(n).
     * A BST keyed on artist is intentionally omitted here to show the contrast,
     * but could be added identically to SongBST if needed.
     */
    public Song searchByArtist(String artist) {
        return songs.searchByArtist(artist);
    }

    // ── Sorted view (free from BST in-order traversal) ────────────────────────
    public List<Song> sortedByTitle() {
        return titleBST.inOrderSorted();
    }

    // ── Query helpers ─────────────────────────────────────────────────────────
    public Song    getSongAt(int index) { return songs.get(index); }
    public boolean contains(Song song)  { return songs.contains(song); }
    public int     size()               { return songs.size(); }
    public boolean isEmpty()            { return songs.isEmpty(); }
    public String  getName()            { return name; }

    // ── Display: playlist in playback order ──────────────────────────────────
    public void display() {
        printDivider('═', 105);
        System.out.printf("  🎵  Playlist : %-40s  Songs: %-3d  Duration: %s%n",
                name, songs.size(), songs.formattedTotalDuration());
        printDivider('═', 105);
        if (songs.isEmpty()) {
            System.out.println("  (empty playlist)");
        } else {
            System.out.printf("  %-4s %-35s %-25s %-25s %s%n",
                    "#", "Title", "Artist", "Album", "Duration");
            printDivider('─', 105);
            int idx = 1;
            for (Song s : songs) System.out.printf("  %-4d %s%n", idx++, s);
        }
        printDivider('═', 105);
    }

    // ── Display: songs sorted alphabetically (BST in-order) ──────────────────
    public void displaySortedByTitle() {
        printDivider('═', 105);
        System.out.printf("  🔤  Sorted Playlist : %-35s  Songs: %-3d%n", name, songs.size());
        printDivider('═', 105);
        List<Song> sorted = sortedByTitle();
        if (sorted.isEmpty()) {
            System.out.println("  (empty playlist)");
        } else {
            System.out.printf("  %-4s %-35s %-25s %-25s %s%n",
                    "#", "Title", "Artist", "Album", "Duration");
            printDivider('─', 105);
            int idx = 1;
            for (Song s : sorted) System.out.printf("  %-4d %s%n", idx++, s);
        }
        printDivider('═', 105);
    }

    // ── Display: BST tree shape ───────────────────────────────────────────────
    public void displayBSTShape() {
        System.out.println();
        printDivider('─', 60);
        System.out.println("  BST shape (rotated 90° — right subtree on top):");
        printDivider('─', 60);
        titleBST.printTree();
        printDivider('─', 60);
    }

    private static void printDivider(char ch, int len) {
        System.out.println(String.valueOf(ch).repeat(len));
    }
}


// ══════════════════════════════════════════════════════════════════════════════
//  PlaylistManager  –  driver / demonstration
// ══════════════════════════════════════════════════════════════════════════════
public class PlaylistManager {

    public static void main(String[] args) {

        Playlist playlist = new Playlist("Classic Rock Essentials");

        // ── 1. Add songs ──────────────────────────────────────────────────────
        System.out.println("\n>>> Adding songs...\n");
        playlist.addSong(new Song("Bohemian Rhapsody",     "Queen",        "A Night at the Opera", 355));
        playlist.addSong(new Song("Hotel California",      "Eagles",       "Hotel California",     391));
        playlist.addSong(new Song("Stairway to Heaven",    "Led Zeppelin", "Led Zeppelin IV",      482));
        playlist.addSong(new Song("Imagine",               "John Lennon",  "Imagine",              187));
        playlist.addSong(new Song("Smells Like Teen Spirit","Nirvana",     "Nevermind",            301));
        playlist.addSong(new Song("Purple Haze",           "Jimi Hendrix", "Are You Experienced",  170));
        playlist.addSong(new Song("Come As You Are",       "Nirvana",      "Nevermind",            219));
        playlist.addSong(new Song("Black",                 "Pearl Jam",    "Ten",                  335));
        playlist.addSong(new Song("Angie",                 "Rolling Stones","Goats Head Soup",     271));
        playlist.addSong(new Song("Behind Blue Eyes",      "The Who",      "Who's Next",           209));

        playlist.display();

        // ── 2. Show BST internal shape ───────────────────────────────────────
        System.out.println("\n>>> BST internal structure (title index):");
        playlist.displayBSTShape();

        // ── 3. searchByTitle  →  BST  O(log n) ───────────────────────────────
        System.out.println("\n>>> [BST] searchByTitle – exact match:");
        String[] titlesToFind = { "Hotel California", "Imagine", "Yesterday" };
        for (String t : titlesToFind) {
            Song result = playlist.searchByTitle(t);
            System.out.printf("  %-30s → %s%n", "\"" + t + "\"",
                    result != null ? result.getArtist() + " (" + result.getFormattedDuration() + ")" : "NOT FOUND");
        }

        // ── 4. searchByPrefix  →  BST prefix walk ────────────────────────────
        System.out.println("\n>>> [BST] searchByPrefix – autocomplete for \"b\":");
        List<Song> prefixResults = playlist.searchByPrefix("b");
        if (prefixResults.isEmpty()) {
            System.out.println("  No songs found.");
        } else {
            prefixResults.forEach(s -> System.out.println("  " + s));
        }

        System.out.println("\n>>> [BST] searchByPrefix – autocomplete for \"s\":");
        playlist.searchByPrefix("s").forEach(s -> System.out.println("  " + s));

        // ── 5. searchByArtist  →  Linked list  O(n) ──────────────────────────
        System.out.println("\n>>> [Linked list] searchByArtist – first match for \"Nirvana\":");
        Song byArtist = playlist.searchByArtist("Nirvana");
        System.out.println(byArtist != null ? "  " + byArtist : "  Not found.");

        // ── 6. Sorted view via BST in-order traversal (free, no extra sort) ──
        System.out.println("\n>>> [BST in-order] Songs sorted alphabetically by title:\n");
        playlist.displaySortedByTitle();

        // ── 7. Remove songs and verify BST sync ───────────────────────────────
        System.out.println("\n>>> Removing 'Purple Haze' by title (updates list + BST)...");
        System.out.println(playlist.removeSongByTitle("Purple Haze")
                ? "  Removed successfully."
                : "  Not found.");

        System.out.println(">>> Removing first song by index...");
        System.out.println("  Removed: " + playlist.removeSongAt(0).getTitle());

        System.out.println("\n>>> Verifying BST search returns null for removed song:");
        Song deleted = playlist.searchByTitle("Purple Haze");
        System.out.println("  searchByTitle(\"Purple Haze\") → " + (deleted == null ? "null ✓ (correctly removed from BST)" : deleted));

        System.out.println("\n>>> Playlist after removals:\n");
        playlist.display();

        // ── 8. Edge cases ─────────────────────────────────────────────────────
        System.out.println("\n>>> Edge cases:");

        System.out.print("  Blank prefix search \"\": ");
        List<Song> all = playlist.searchByPrefix("");
        System.out.println(all.size() + " songs returned (all, because every title starts with \"\").");

        System.out.print("  Non-existent title search: ");
        System.out.println(playlist.searchByTitle("Yesterday") == null ? "null ✓" : "ERROR");

        System.out.println("  Invalid Song (blank title):");
        try { new Song("", "Artist", "Album", 200); }
        catch (IllegalArgumentException e) { System.out.println("    Caught: " + e.getMessage()); }

        System.out.println("  Index out of bounds removal:");
        try { playlist.removeSongAt(999); }
        catch (IndexOutOfBoundsException e) { System.out.println("    Caught: " + e.getMessage()); }

        System.out.println("\nDone.\n");
    }
}
