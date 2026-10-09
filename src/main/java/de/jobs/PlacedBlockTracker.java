package de.jobs;

import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

/**
 * Merkt sich, welche Blöcke ein Spieler gesetzt hat. So lassen sich
 * selbst gesetzte Erze oder Stämme nicht zum Geldverdienen abbauen.
 *
 * Die Daten liegen im PersistentDataContainer jedes Chunks und überleben
 * damit einen Serverneustart. Jede Position wird als eine Zahl kodiert.
 */
public final class PlacedBlockTracker {

    private final NamespacedKey key;

    public PlacedBlockTracker(JobsPlugin plugin) {
        this.key = new NamespacedKey(plugin, "placed_blocks");
    }

    public void markPlaced(Block block) {
        PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
        int[] current = pdc.getOrDefault(key, PersistentDataType.INTEGER_ARRAY, new int[0]);
        int code = encode(block);

        for (int value : current) {
            if (value == code) return; // bereits gespeichert
        }

        int[] updated = Arrays.copyOf(current, current.length + 1);
        updated[current.length] = code;
        pdc.set(key, PersistentDataType.INTEGER_ARRAY, updated);
    }

    /**
     * Prüft, ob der Block vom Spieler gesetzt wurde. Gibt true zurück,
     * wenn ja, und entfernt den Eintrag dann wieder.
     */
    public boolean removeIfPlaced(Block block) {
        Chunk chunk = block.getChunk();
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        int[] current = pdc.get(key, PersistentDataType.INTEGER_ARRAY);
        if (current == null) return false;

        int code = encode(block);
        int index = -1;
        for (int i = 0; i < current.length; i++) {
            if (current[i] == code) {
                index = i;
                break;
            }
        }
        if (index < 0) return false;

        int[] updated = new int[current.length - 1];
        for (int i = 0, j = 0; i < current.length; i++) {
            if (i != index) {
                updated[j++] = current[i];
            }
        }

        if (updated.length == 0) {
            pdc.remove(key);
        } else {
            pdc.set(key, PersistentDataType.INTEGER_ARRAY, updated);
        }
        return true;
    }

    /** Kodiert die Position im Chunk: Höhe (ab Welt-Untergrenze), lokales X und lokales Z. */
    private static int encode(Block block) {
        int localX = block.getX() & 15;
        int localZ = block.getZ() & 15;
        int yOffset = block.getY() - block.getWorld().getMinHeight();
        return (yOffset << 8) | (localX << 4) | localZ;
    }
}
