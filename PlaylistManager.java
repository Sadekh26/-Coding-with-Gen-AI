import java.util.Iterator;
import java.util.NoSuchElementException;

// ─────────────────────────────────────────────
//  Song  – represents a single track
// ─────────────────────────────────────────────
class Song {
    private final String title;
    private final String artist;
    private final String album;
    private final int    durationSeconds; // stored in seconds

    public Song(String title, String artist, String album, int durationSeconds) {
        if (title  == null || title.isBlank())  throw new IllegalArgumentException("Title cannot be empty.");
        if (artist == null || artist.isBlank()) throw new IllegalArgumentException("Artist cannot be empty.");
        if (durationSeconds <= 0)               throw new IllegalArgumentException("Duration must be positive.");

        this.title           = title.trim();
        this.artist          = artist.trim();
        this.album           = (album != null && !album.isBlank()) ? album.trim() : "Unknown Album";
        this.durationSeconds = durationSeconds;
    }

    // ── Getters ──────────────────────────────
    public String getTitle()           { return title;           }
    public String getArtist()          { return artist;          }
    public String getAlbum()           { return album;           }
    public int    getDurationSeconds() { return durationSeconds; }

    /** Returns duration as "mm:ss" */
    public String getFormattedDuration() {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
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


// ─────────────────────────────────────────────
//  Node  – internal building block of the list
// ─────────────────────────────────────────────
class Node {
    Song song;
    Node next;
    Node prev;          // doubly-linked for O(1) removal given the node

    Node(Song song) {
        this.song = song;
    }
}


// ─────────────────────────────────────────────
//  PlaylistLinkedList  – custom doubly linked list
// ─────────────────────────────────────────────
class PlaylistLinkedList implements Iterable<Song> {

    private Node head;
    private Node tail;
    private int  size;

    // ── Add at end ────────────────────────────
    public void addLast(Song song) {
        if (song == null) throw new IllegalArgumentException("Cannot add a null song.");
        Node newNode = new Node(song);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            tail.next    = newNode;
            newNode.prev = tail;
            tail         = newNode;
        }
        size++;
    }

    // ── Add at beginning ──────────────────────
    public void addFirst(Song song) {
        if (song == null) throw new IllegalArgumentException("Cannot add a null song.");
        Node newNode = new Node(song);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            newNode.next = head;
            head.prev    = newNode;
            head         = newNode;
        }
        size++;
    }

    // ── Insert after a given index (0-based) ──
    public void addAt(int index, Song song) {
        checkIndexForInsert(index);
        if (index == 0)    { addFirst(song); return; }
        if (index == size) { addLast(song);  return; }

        Node current  = getNodeAt(index - 1);
        Node newNode  = new Node(song);
        newNode.next  = current.next;
        newNode.prev  = current;
        if (current.next != null) current.next.prev = newNode;
        current.next  = newNode;
        size++;
    }

    // ── Remove by title (case-insensitive) ───
    public boolean removeByTitle(String title) {
        Node current = head;
        while (current != null) {
            if (current.song.getTitle().equalsIgnoreCase(title)) {
                unlink(current);
                return true;
            }
            current = current.next;
        }
        return false;
    }

    // ── Remove by index (0-based) ────────────
    public Song removeAt(int index) {
        checkIndexForAccess(index);
        Node target = getNodeAt(index);
        unlink(target);
        return target.song;
    }

    // ── Remove first ─────────────────────────
    public Song removeFirst() {
        if (isEmpty()) throw new NoSuchElementException("Playlist is empty.");
        return removeAt(0);
    }

    // ── Remove last ──────────────────────────
    public Song removeLast() {
        if (isEmpty()) throw new NoSuchElementException("Playlist is empty.");
        return removeAt(size - 1);
    }

    // ── Search by title ───────────────────────
    public Song searchByTitle(String title) {
        Node current = head;
        while (current != null) {
            if (current.song.getTitle().equalsIgnoreCase(title)) return current.song;
            current = current.next;
        }
        return null;
    }

    // ── Search by artist (returns first match) ─
    public Song searchByArtist(String artist) {
        Node current = head;
        while (current != null) {
            if (current.song.getArtist().equalsIgnoreCase(artist)) return current.song;
            current = current.next;
        }
        return null;
    }

    // ── Get song at index (0-based) ───────────
    public Song get(int index) {
        checkIndexForAccess(index);
        return getNodeAt(index).song;
    }

    // ── Contains ─────────────────────────────
    public boolean contains(Song song) {
        Node current = head;
        while (current != null) {
            if (current.song.equals(song)) return true;
            current = current.next;
        }
        return false;
    }

    // ── Total duration ────────────────────────
    public int totalDurationSeconds() {
        int total = 0;
        for (Song s : this) total += s.getDurationSeconds();
        return total;
    }

    /** Returns total duration as "Xh Ym Zs" */
    public String formattedTotalDuration() {
        int total   = totalDurationSeconds();
        int hours   = total / 3600;
        int minutes = (total % 3600) / 60;
        int seconds = total % 60;
        if (hours > 0) return String.format("%dh %dm %ds", hours, minutes, seconds);
        return String.format("%dm %ds", minutes, seconds);
    }

    // ── Size / Empty ──────────────────────────
    public int     size()    { return size;    }
    public boolean isEmpty() { return size == 0; }

    // ── Clear ─────────────────────────────────
    public void clear() { head = tail = null; size = 0; }

    // ── Iterator (forward) ────────────────────
    @Override
    public Iterator<Song> iterator() {
        return new Iterator<>() {
            Node current = head;
            @Override public boolean hasNext() { return current != null; }
            @Override public Song    next()    {
                if (!hasNext()) throw new NoSuchElementException();
                Song s = current.song;
                current = current.next;
                return s;
            }
        };
    }

    // ── Private helpers ───────────────────────
    private Node getNodeAt(int index) {
        // Traverse from the closer end for efficiency
        Node current;
        if (index < size / 2) {
            current = head;
            for (int i = 0; i < index; i++) current = current.next;
        } else {
            current = tail;
            for (int i = size - 1; i > index; i--) current = current.prev;
        }
        return current;
    }

    private void unlink(Node node) {
        if (node.prev != null) node.prev.next = node.next; else head = node.next;
        if (node.next != null) node.next.prev = node.prev; else tail = node.prev;
        node.prev = node.next = null;
        size--;
    }

    private void checkIndexForAccess(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
    }

    private void checkIndexForInsert(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Insert index " + index + " out of bounds for size " + size);
    }
}


// ─────────────────────────────────────────────
//  Playlist  – high-level manager
// ─────────────────────────────────────────────
class Playlist {
    private final String            name;
    private final PlaylistLinkedList songs;

    public Playlist(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Playlist name cannot be empty.");
        this.name  = name.trim();
        this.songs = new PlaylistLinkedList();
    }

    // ── Mutation ──────────────────────────────
    public void addSong(Song song)             { songs.addLast(song);        }
    public void addSongFirst(Song song)        { songs.addFirst(song);       }
    public void addSongAt(int i, Song song)    { songs.addAt(i, song);       }

    public boolean removeSongByTitle(String t) { return songs.removeByTitle(t); }
    public Song    removeSongAt(int index)     { return songs.removeAt(index);  }
    public Song    removeFirstSong()           { return songs.removeFirst();    }
    public Song    removeLastSong()            { return songs.removeLast();     }
    public void    clear()                     { songs.clear();                }

    // ── Query ─────────────────────────────────
    public Song    searchByTitle(String t)   { return songs.searchByTitle(t);  }
    public Song    searchByArtist(String a)  { return songs.searchByArtist(a); }
    public Song    getSongAt(int index)      { return songs.get(index);        }
    public boolean contains(Song song)       { return songs.contains(song);    }
    public int     size()                    { return songs.size();            }
    public boolean isEmpty()                 { return songs.isEmpty();         }

    // ── Display ───────────────────────────────
    public void display() {
        printDivider('═', 100);
        System.out.printf("  🎵  Playlist: %-40s  Songs: %-3d  Duration: %s%n",
                name, songs.size(), songs.formattedTotalDuration());
        printDivider('═', 100);
        if (songs.isEmpty()) {
            System.out.println("  (empty playlist)");
        } else {
            System.out.printf("  %-4s %-35s %-25s %-25s %s%n",
                    "#", "Title", "Artist", "Album", "Duration");
            printDivider('─', 100);
            int index = 1;
            for (Song song : songs) {
                System.out.printf("  %-4d %s%n", index++, song);
            }
        }
        printDivider('═', 100);
    }

    public String getName() { return name; }

    private static void printDivider(char ch, int len) {
        System.out.println(String.valueOf(ch).repeat(len));
    }
}


// ─────────────────────────────────────────────
//  Main  – demonstration / driver
// ─────────────────────────────────────────────
public class PlaylistManager {

    public static void main(String[] args) {

        // ── 1. Create playlist ────────────────
        Playlist playlist = new Playlist("My Favorite Tracks");

        // ── 2. Add songs ──────────────────────
        System.out.println("\n>>> Adding songs to the playlist...\n");

        playlist.addSong(new Song("Bohemian Rhapsody",  "Queen",          "A Night at the Opera",       355));
        playlist.addSong(new Song("Hotel California",   "Eagles",         "Hotel California",            391));
        playlist.addSong(new Song("Stairway to Heaven", "Led Zeppelin",   "Led Zeppelin IV",             482));
        playlist.addSong(new Song("Imagine",            "John Lennon",    "Imagine",                     187));
        playlist.addSong(new Song("Smells Like Teen Spirit","Nirvana",    "Nevermind",                   301));
        playlist.addSong(new Song("Purple Haze",        "Jimi Hendrix",   "Are You Experienced",         170));

        // Add at the beginning
        playlist.addSongFirst(new Song("Come As You Are", "Nirvana",      "Nevermind",                   219));

        // Add at specific index (position 3, 0-based)
        playlist.addSongAt(3, new Song("Black",         "Pearl Jam",      "Ten",                         335));

        playlist.display();

        // ── 3. Search ─────────────────────────
        System.out.println("\n>>> Searching for 'Hotel California'...");
        Song found = playlist.searchByTitle("Hotel California");
        System.out.println(found != null
                ? "  Found: " + found
                : "  Song not found.");

        System.out.println("\n>>> Searching by artist 'Nirvana'...");
        Song byArtist = playlist.searchByArtist("Nirvana");
        System.out.println(byArtist != null
                ? "  First match: " + byArtist
                : "  No songs by that artist.");

        System.out.println("\n>>> Searching for non-existent song 'Yesterday'...");
        Song missing = playlist.searchByTitle("Yesterday");
        System.out.println(missing != null ? "  Found: " + missing : "  Song not found.");

        // ── 4. Remove by title ────────────────
        System.out.println("\n>>> Removing 'Purple Haze' by title...");
        boolean removed = playlist.removeSongByTitle("Purple Haze");
        System.out.println(removed ? "  Removed successfully." : "  Song not found.");

        // ── 5. Remove by index ────────────────
        System.out.println("\n>>> Removing song at index 0 (first song)...");
        Song removedFirst = playlist.removeSongAt(0);
        System.out.println("  Removed: " + removedFirst.getTitle());

        // ── 6. Remove last ────────────────────
        System.out.println("\n>>> Removing the last song...");
        Song removedLast = playlist.removeLastSong();
        System.out.println("  Removed: " + removedLast.getTitle());

        // ── 7. Final display ──────────────────
        System.out.println("\n>>> Final playlist state after removals:\n");
        playlist.display();

        // ── 8. Edge-case demos ────────────────
        System.out.println("\n>>> Edge-case: removing a title that does not exist...");
        boolean notFound = playlist.removeSongByTitle("Nonexistent Song");
        System.out.println(notFound ? "  Removed." : "  Song not found in playlist.");

        System.out.println("\n>>> Edge-case: invalid constructor arguments...");
        try {
            new Song("", "Artist", "Album", 180);
        } catch (IllegalArgumentException e) {
            System.out.println("  Caught expected error: " + e.getMessage());
        }

        try {
            new Song("Title", "Artist", "Album", -5);
        } catch (IllegalArgumentException e) {
            System.out.println("  Caught expected error: " + e.getMessage());
        }

        System.out.println("\n>>> Edge-case: index out of bounds...");
        try {
            playlist.removeSongAt(999);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("  Caught expected error: " + e.getMessage());
        }

        System.out.println("\nDone.\n");
    }
}
