package io.github.isoyigido.bluff.game.packets.broadcasts;

public class SetAllPassedBroadcast {
    private boolean allPassed;

    public SetAllPassedBroadcast(boolean allPassed) {
        this.allPassed = allPassed;
    }

    public boolean didAllPass() {
        return this.allPassed;
    }

    private SetAllPassedBroadcast() {}
}