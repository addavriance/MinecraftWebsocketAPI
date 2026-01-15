package me.adda.mcwebapi.api.modules;

import me.adda.mcwebapi.api.BaseApiModule;
import me.adda.mcwebapi.api.annotations.ApiMethod;
import me.adda.mcwebapi.api.annotations.ApiModule;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.*;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.stream.Collectors;

@ApiModule("scoreboard")
public class ScoreboardApiModule extends BaseApiModule {

    // ===== OBJECTIVES =====

    @ApiMethod("createObjective")
    public boolean createObjective(String name, String criteriaId, String displayName) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            if (scoreboard.getObjective(name) != null) {
                return false; // Already exists
            }

            Optional<ObjectiveCriteria> criteria = ObjectiveCriteria.byName(criteriaId);
            if (criteria.isEmpty()) {
                return false;
            }

            scoreboard.addObjective(
                    name,
                    criteria.get(),
                    Component.literal(displayName),
                    criteria.get().getDefaultRenderType(),
                    true,
                    null
            );
            return true;
        });
    }

    @ApiMethod("removeObjective")
    public boolean removeObjective(String name) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            Objective objective = scoreboard.getObjective(name);
            if (objective == null) return false;

            scoreboard.removeObjective(objective);
            return true;
        });
    }

    @ApiMethod("getObjectives")
    public List<Map<String, Object>> getObjectives() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return Collections.emptyList();

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            return scoreboard.getObjectives().stream()
                    .map(this::objectiveToMap)
                    .collect(Collectors.toList());
        });
    }

    @ApiMethod("getObjective")
    public Map<String, Object> getObjective(String name) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            Objective objective = scoreboard.getObjective(name);
            return objective != null ? objectiveToMap(objective) : null;
        });
    }

    @ApiMethod("setDisplaySlot")
    public boolean setDisplaySlot(String slot, String objectiveName) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            Objective objective = objectiveName != null ? scoreboard.getObjective(objectiveName) : null;

            try {
                DisplaySlot displaySlot = DisplaySlot.valueOf(slot.toUpperCase());
                scoreboard.setDisplayObjective(displaySlot, objective);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        });
    }

    @ApiMethod("getDisplaySlots")
    public Map<String, String> getDisplaySlots() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return Collections.emptyMap();

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            Map<String, String> slots = new HashMap<>();

            for (DisplaySlot slot : DisplaySlot.values()) {
                Objective objective = scoreboard.getDisplayObjective(slot);
                slots.put(slot.name(), objective != null ? objective.getName() : null);
            }

            return slots;
        });
    }

    // ===== TEAMS =====

    @ApiMethod("createTeam")
    public boolean createTeam(String name) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            if (scoreboard.getPlayerTeam(name) != null) {
                return false; // Already exists
            }

            scoreboard.addPlayerTeam(name);
            return true;
        });
    }

    @ApiMethod("removeTeam")
    public boolean removeTeam(String name) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            PlayerTeam team = scoreboard.getPlayerTeam(name);
            if (team == null) return false;

            scoreboard.removePlayerTeam(team);
            return true;
        });
    }

    @ApiMethod("getTeams")
    public List<Map<String, Object>> getTeams() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return Collections.emptyList();

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            return scoreboard.getPlayerTeams().stream()
                    .map(this::teamToMap)
                    .collect(Collectors.toList());
        });
    }

    @ApiMethod("getTeam")
    public Map<String, Object> getTeam(String name) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            PlayerTeam team = scoreboard.getPlayerTeam(name);
            return team != null ? teamToMap(team) : null;
        });
    }

    @ApiMethod("addPlayerToTeam")
    public boolean addPlayerToTeam(String teamName, String playerName) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            PlayerTeam team = scoreboard.getPlayerTeam(teamName);
            if (team == null) return false;

            return scoreboard.addPlayerToTeam(playerName, team);
        });
    }

    @ApiMethod("removePlayerFromTeam")
    public boolean removePlayerFromTeam(String playerName) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            scoreboard.removePlayerFromTeam(playerName);
            return true;
        });
    }

    @ApiMethod("setTeamDisplayName")
    public boolean setTeamDisplayName(String teamName, String displayName) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            PlayerTeam team = scoreboard.getPlayerTeam(teamName);
            if (team == null) return false;

            team.setDisplayName(Component.literal(displayName));
            return true;
        });
    }

    @ApiMethod("setTeamColor")
    public boolean setTeamColor(String teamName, String colorName) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            PlayerTeam team = scoreboard.getPlayerTeam(teamName);
            if (team == null) return false;

            try {
                ChatFormatting color = ChatFormatting.valueOf(colorName.toUpperCase());
                team.setColor(color);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        });
    }

    @ApiMethod("setTeamPrefix")
    public boolean setTeamPrefix(String teamName, String prefix) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            PlayerTeam team = scoreboard.getPlayerTeam(teamName);
            if (team == null) return false;

            team.setPlayerPrefix(Component.literal(prefix));
            return true;
        });
    }

    @ApiMethod("setTeamSuffix")
    public boolean setTeamSuffix(String teamName, String suffix) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            PlayerTeam team = scoreboard.getPlayerTeam(teamName);
            if (team == null) return false;

            team.setPlayerSuffix(Component.literal(suffix));
            return true;
        });
    }

    @ApiMethod("setTeamFriendlyFire")
    public boolean setTeamFriendlyFire(String teamName, boolean enabled) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            PlayerTeam team = scoreboard.getPlayerTeam(teamName);
            if (team == null) return false;

            team.setAllowFriendlyFire(enabled);
            return true;
        });
    }

    @ApiMethod("setTeamSeeFriendlyInvisibles")
    public boolean setTeamSeeFriendlyInvisibles(String teamName, boolean enabled) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            PlayerTeam team = scoreboard.getPlayerTeam(teamName);
            if (team == null) return false;

            team.setSeeFriendlyInvisibles(enabled);
            return true;
        });
    }

    // ===== SCORES =====

    @ApiMethod("getScore")
    public Integer getScore(String objectiveName, String target) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            Objective objective = scoreboard.getObjective(objectiveName);
            if (objective == null) return null;

            ReadOnlyScoreInfo scoreInfo = scoreboard.getPlayerScoreInfo(ScoreHolder.forNameOnly(target), objective);
            return scoreInfo != null ? scoreInfo.value() : null;
        });
    }

    @ApiMethod("setScore")
    public boolean setScore(String objectiveName, String target, int value) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            Objective objective = scoreboard.getObjective(objectiveName);
            if (objective == null) return false;

            scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(target), objective).set(value);
            return true;
        });
    }

    @ApiMethod("addScore")
    public boolean addScore(String objectiveName, String target, int value) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            Objective objective = scoreboard.getObjective(objectiveName);
            if (objective == null) return false;

            ScoreAccess score = scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(target), objective);
            score.set(score.get() + value);
            return true;
        });
    }

    @ApiMethod("resetScore")
    public boolean resetScore(String objectiveName, String target) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            Objective objective = scoreboard.getObjective(objectiveName);
            if (objective == null) return false;

            scoreboard.resetSinglePlayerScore(ScoreHolder.forNameOnly(target), objective);
            return true;
        });
    }

    @ApiMethod("resetAllScores")
    public boolean resetAllScores(String target) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            scoreboard.resetAllPlayerScores(ScoreHolder.forNameOnly(target));
            return true;
        });
    }

    @ApiMethod("getScores")
    public Map<String, Integer> getScores(String target) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return Collections.emptyMap();

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            ScoreHolder holder = ScoreHolder.forNameOnly(target);

            Map<String, Integer> scores = new HashMap<>();
            for (Objective objective : scoreboard.getObjectives()) {
                ReadOnlyScoreInfo scoreInfo = scoreboard.getPlayerScoreInfo(holder, objective);
                if (scoreInfo != null) {
                    scores.put(objective.getName(), scoreInfo.value());
                }
            }

            return scores;
        });
    }

    @ApiMethod("getObjectiveScores")
    public Map<String, Integer> getObjectiveScores(String objectiveName) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return Collections.emptyMap();

        return executeOnServerThread(() -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            Objective objective = scoreboard.getObjective(objectiveName);
            if (objective == null) return Collections.emptyMap();

            Map<String, Integer> scores = new HashMap<>();
            scoreboard.listPlayerScores(objective).forEach(score -> {
                scores.put(score.ownerName().getString(), score.value());
            });

            return scores;
        });
    }

    // ===== HELPER METHODS =====

    private Map<String, Object> objectiveToMap(Objective objective) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", objective.getName());
        map.put("displayName", objective.getDisplayName().getString());
        map.put("criteria", objective.getCriteria().getName());
        map.put("renderType", objective.getRenderType().name());
        return map;
    }

    private Map<String, Object> teamToMap(PlayerTeam team) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", team.getName());
        map.put("displayName", team.getDisplayName().getString());
        map.put("color", team.getColor().name());
        map.put("prefix", team.getPlayerPrefix().getString());
        map.put("suffix", team.getPlayerSuffix().getString());
        map.put("friendlyFire", team.isAllowFriendlyFire());
        map.put("seeFriendlyInvisibles", team.canSeeFriendlyInvisibles());
        map.put("players", new ArrayList<>(team.getPlayers()));
        return map;
    }
}
