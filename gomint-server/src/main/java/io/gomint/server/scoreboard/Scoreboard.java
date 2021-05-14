package io.gomint.server.scoreboard;

import io.gomint.scoreboard.DisplaySlot;
import io.gomint.scoreboard.SortOrder;
import io.gomint.server.entity.Entity;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.network.packet.Packet;
import io.gomint.server.network.packet.PacketRemoveObjective;
import io.gomint.server.network.packet.PacketSetObjective;
import io.gomint.server.network.packet.PacketSetScore;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author geNAZt
 * @version 1.0
 */
public class Scoreboard implements io.gomint.scoreboard.Scoreboard {

    // Scores
    private long scoreIdCounter = 0;
    private Long2ObjectMap<ScoreboardLine> scoreboardLines = new Long2ObjectArrayMap<>();

    // Viewers
    private Set<EntityPlayer> viewers = new HashSet<>();

    // Displays
    private Map<DisplaySlot, io.gomint.scoreboard.ScoreboardDisplay> displays = new EnumMap<>(DisplaySlot.class);

    @Override
    public synchronized io.gomint.scoreboard.ScoreboardDisplay addDisplay(DisplaySlot slot, String objectiveName, String displayName) {
        return this.addDisplay(slot, objectiveName, displayName, SortOrder.ASCENDING);
    }

    @Override
    public synchronized io.gomint.scoreboard.ScoreboardDisplay addDisplay(DisplaySlot slot, String objectiveName, String displayName, SortOrder sortOrder) {
        io.gomint.scoreboard.ScoreboardDisplay scoreboardDisplay = this.displays.get(slot);
        if (scoreboardDisplay == null) {
            scoreboardDisplay = new ScoreboardDisplay(this, objectiveName, displayName, sortOrder);
            this.displays.put(slot, scoreboardDisplay);

            this.broadcast(this.constructDisplayPacket(slot, scoreboardDisplay));
        }

        return scoreboardDisplay;
    }

    @Override
    public synchronized io.gomint.scoreboard.ScoreboardDisplay display(DisplaySlot slot) {
        return this.displays.get(slot);
    }

    @Override
    public synchronized Scoreboard removeDisplay(DisplaySlot slot) {
        io.gomint.scoreboard.ScoreboardDisplay display = this.displays.remove(slot);
        if (display != null) {
            // Remove all scores on this display
            LongList validScoreIDs = new LongArrayList();

            // Map fake entries first
            Long2ObjectMap.FastEntrySet<ScoreboardLine> fastSet = (Long2ObjectMap.FastEntrySet<ScoreboardLine>) this.scoreboardLines.long2ObjectEntrySet();
            ObjectIterator<Long2ObjectMap.Entry<ScoreboardLine>> fastIterator = fastSet.fastIterator();
            while (fastIterator.hasNext()) {
                Long2ObjectMap.Entry<ScoreboardLine> entry = fastIterator.next();
                if (entry.getValue().objective.equals(display.objective())) {
                    validScoreIDs.add(entry.getLongKey());
                }
            }

            // Remove all scores
            for (long scoreID : validScoreIDs) {
                this.scoreboardLines.remove(scoreID);
            }

            this.broadcast(this.constructRemoveScores(validScoreIDs));

            // Now get rid of the objective
            this.broadcast(this.constructRemoveDisplayPacket(display));
        }

        return this;
    }

    private Packet constructDisplayPacket(DisplaySlot slot, io.gomint.scoreboard.ScoreboardDisplay display) {
        PacketSetObjective packetSetObjective = new PacketSetObjective();
        packetSetObjective.setCriteriaName("dummy");
        packetSetObjective.setDisplayName(display.display());
        packetSetObjective.setObjectiveName(display.objective());
        packetSetObjective.setDisplaySlot(slot.name().toLowerCase());
        packetSetObjective.setSortOrder(display.sortOrder().ordinal());
        return packetSetObjective;
    }

    private void broadcast(Packet packet) {
        for (EntityPlayer viewer : this.viewers) {
            viewer.connection().addToSendQueue(packet);
        }
    }

    /**
     * Add or update a line with its score
     *
     * @param line      which should be added
     * @param objective to which this line should be added
     * @param score     which should be given to that line
     */
    synchronized long addOrUpdateLine(String line, String objective, int score) {
        // Check if we already have this registered
        Long2ObjectMap.FastEntrySet<ScoreboardLine> fastEntrySet = (Long2ObjectMap.FastEntrySet<ScoreboardLine>) this.scoreboardLines.long2ObjectEntrySet();
        ObjectIterator<Long2ObjectMap.Entry<ScoreboardLine>> fastIterator = fastEntrySet.fastIterator();
        while (fastIterator.hasNext()) {
            Long2ObjectMap.Entry<ScoreboardLine> entry = fastIterator.next();
            if (entry.getValue().type == 3 && entry.getValue().fakeName.equals(line) && entry.getValue().objective.equals(objective)) {
                return entry.getLongKey();
            }
        }

        // Add this score
        long newId = this.scoreIdCounter++;
        ScoreboardLine scoreboardLine = new ScoreboardLine((byte) 3, 0, line, objective, score);
        this.scoreboardLines.put(newId, scoreboardLine);

        // Broadcast entry
        this.broadcast(this.constructSetScore(newId, scoreboardLine));
        return newId;
    }

    /**
     * Add a entity to the given scoreboard
     *
     * @param entity    which should be added
     * @param objective which should be used
     * @param score     which should be used to register
     */
    synchronized long addOrUpdateEntity(Entity<?> entity, String objective, int score) {
        // Check if we already have this registered
        Long2ObjectMap.FastEntrySet<ScoreboardLine> fastEntrySet = (Long2ObjectMap.FastEntrySet<ScoreboardLine>) this.scoreboardLines.long2ObjectEntrySet();
        ObjectIterator<Long2ObjectMap.Entry<ScoreboardLine>> fastIterator = fastEntrySet.fastIterator();
        while (fastIterator.hasNext()) {
            Long2ObjectMap.Entry<ScoreboardLine> entry = fastIterator.next();
            if (entry.getValue().entityId == entity.id() && entry.getValue().objective.equals(objective)) {
                return entry.getLongKey();
            }
        }

        // Add this score
        long newId = this.scoreIdCounter++;
        ScoreboardLine scoreboardLine = new ScoreboardLine((byte) ((entity instanceof EntityPlayer) ? 1 : 2), entity.id(), "", objective, score);
        this.scoreboardLines.put(newId, scoreboardLine);

        // Broadcast entry
        this.broadcast(this.constructSetScore(newId, scoreboardLine));

        return newId;
    }

    private Packet constructSetScore(long newId, ScoreboardLine line) {
        PacketSetScore packetSetScore = new PacketSetScore();
        packetSetScore.setType((byte) 0);

        packetSetScore.setEntries(new ArrayList<>() {{
            add(new PacketSetScore.ScoreEntry(newId, line.objective, line.score, line.type, line.fakeName, line.entityId));
        }});

        return packetSetScore;
    }

    private Packet constructSetScore() {
        PacketSetScore packetSetScore = new PacketSetScore();
        packetSetScore.setType((byte) 0);

        List<PacketSetScore.ScoreEntry> entries = new ArrayList<>();
        Long2ObjectMap.FastEntrySet<ScoreboardLine> fastEntrySet = (Long2ObjectMap.FastEntrySet<ScoreboardLine>) this.scoreboardLines.long2ObjectEntrySet();
        ObjectIterator<Long2ObjectMap.Entry<ScoreboardLine>> fastIterator = fastEntrySet.fastIterator();
        while (fastIterator.hasNext()) {
            Long2ObjectMap.Entry<ScoreboardLine> entry = fastIterator.next();
            entries.add(new PacketSetScore.ScoreEntry(entry.getLongKey(), entry.getValue().objective, entry.getValue().score, entry.getValue().type, entry.getValue().fakeName, entry.getValue().entityId));
        }

        packetSetScore.setEntries(entries);
        return packetSetScore;
    }

    /**
     * Send / show this scoreboard for that player
     *
     * @param player which should be shown this scoreboard
     */
    public synchronized void showFor(EntityPlayer player) {
        if (this.viewers.add(player)) {
            // We send display information first
            for (Map.Entry<DisplaySlot, io.gomint.scoreboard.ScoreboardDisplay> entry : this.displays.entrySet()) {
                player.connection().addToSendQueue(this.constructDisplayPacket(entry.getKey(), entry.getValue()));
            }

            // Send scores
            player.connection().addToSendQueue(this.constructSetScore());
        }
    }

    /**
     * Remove this scoreboard for the player
     *
     * @param player which wants this scoreboard removed
     */
    public synchronized void hideFor(EntityPlayer player) {
        if (this.viewers.remove(player)) {
            // Remove all known scores
            LongList validScoreIDs = new LongArrayList();

            // Map fake entries first
            Long2ObjectMap.FastEntrySet<ScoreboardLine> fastSet = (Long2ObjectMap.FastEntrySet<ScoreboardLine>) this.scoreboardLines.long2ObjectEntrySet();
            ObjectIterator<Long2ObjectMap.Entry<ScoreboardLine>> fastIterator = fastSet.fastIterator();
            while (fastIterator.hasNext()) {
                validScoreIDs.add(fastIterator.next().getLongKey());
            }

            // Remove all scores
            player.connection().addToSendQueue(this.constructRemoveScores(validScoreIDs));

            // Remove all known displays
            for (Map.Entry<DisplaySlot, io.gomint.scoreboard.ScoreboardDisplay> entry : this.displays.entrySet()) {
                player.connection().addToSendQueue(this.constructRemoveDisplayPacket(entry.getValue()));
            }
        }
    }

    private Packet constructRemoveScores(LongList scoreIDs) {
        PacketSetScore packetSetScore = new PacketSetScore();
        packetSetScore.setType((byte) 1);

        List<PacketSetScore.ScoreEntry> entries = new ArrayList<>();
        for (long scoreID : scoreIDs) {
            entries.add(new PacketSetScore.ScoreEntry(scoreID, "", 0));
        }

        packetSetScore.setEntries(entries);
        return packetSetScore;
    }

    private Packet constructRemoveDisplayPacket(io.gomint.scoreboard.ScoreboardDisplay display) {
        PacketRemoveObjective packetRemoveObjective = new PacketRemoveObjective();
        packetRemoveObjective.setObjectiveName(display.objective());
        return packetRemoveObjective;
    }

    public synchronized void updateScore(long scoreId, int score) {
        ScoreboardLine line = this.scoreboardLines.get(scoreId);
        if (line != null) {
            line.setScore(score);

            this.broadcast(this.constructSetScore(scoreId, line));
        }
    }

    public synchronized void removeScoreEntry(long scoreId) {
        ScoreboardLine line = this.scoreboardLines.remove(scoreId);
        if (line != null) {
            this.broadcast(this.constructRemoveScores(scoreId));
        }
    }

    private Packet constructRemoveScores(long scoreId) {
        PacketSetScore packetSetScore = new PacketSetScore();
        packetSetScore.setType((byte) 1);
        packetSetScore.setEntries(new ArrayList<>() {{
            add(new PacketSetScore.ScoreEntry(scoreId, "", 0));
        }});
        return packetSetScore;
    }

    /**
     * Get the score of a specific line
     *
     * @param scoreId of the line
     * @return score of the line or 0 when the line is not found
     */
    public synchronized int getScore(long scoreId) {
        ScoreboardLine line = this.scoreboardLines.remove(scoreId);
        if (line != null) {
            return line.getScore();
        }

        return 0;
    }

    private static class ScoreboardLine {

        private final byte type;
        private final long entityId;
        private final String fakeName;
        private final String objective;

        private int score;

        public ScoreboardLine(byte type, long entityId, String fakeName, String objective, int score) {
            this.type = type;
            this.entityId = entityId;
            this.fakeName = fakeName;
            this.objective = objective;
            this.score = score;
        }

        public int getScore() {
            return this.score;
        }

        public void setScore(int score) {
            this.score = score;
        }

    }

}
