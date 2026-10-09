package de.jobs;

/** Die vier Jobs. Jeder Spieler hat alle vier, jeder mit eigenem Level und XP. */
public enum Job {

    WOODCUTTER("Holzfäller"),
    MINER("Minenarbeiter"),
    EXPLORER("Erkunder"),
    FARMER("Farmer");

    private final String displayName;

    Job(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
