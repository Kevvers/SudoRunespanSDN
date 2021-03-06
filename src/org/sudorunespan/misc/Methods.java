package org.sudorunespan.misc;

import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Skills;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.Entity;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import org.sudorunespan.SudoRunespan;

import java.awt.*;

import static org.powerbot.game.api.methods.Calculations.distanceTo;

/**
 * Created with IntelliJ IDEA.
 * User: deprecated
 * Date: 5/28/12
 * Time: 11:49 AM
 */

public final class Methods {
    public final static Object mouseLock = new Object();
    private static final int[] MEMBERS_WORLDS =
            {2, 5, 6, 9, 12, 15, 18, 21, 22, 23, 24, 25, 26, 27, 28, 31, 32,
            36, 39, 40, 42, 44, 45, 46, 48, 49, 51, 52, 53, 54, 56, 58, 59,
            60, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 76, 77,
            78, 79, 82, 83, 84, 85, 86, 87, 88, 89, 91, 92, 96, 97, 99, 100,
            103, 104, 105, 114, 115, 116, 117, 119, 123, 124, 137, 138, 139};

    public static void interact(Entity entity, final Tile loc, final String action, final String option) {
        if (entity.getBounds().length == 1) {
            entity = loc;
        }

        if (!Calculations.isOnScreen(entity.getCentralPoint()) || Calculations.distanceTo(loc) > 5) {
            if (!Players.getLocal().isMoving()) {
                synchronized (mouseLock) {
                    Walking.findPath(loc.derive(Random.nextInt(-1, 2), Random.nextInt(-1, 2))).traverse();
                }
            }

            Camera.turnTo(loc.derive(Random.nextInt(-2, 5), Random.nextInt(-2, 5)));
        } else {
            synchronized (mouseLock) {
                entity.hover();
                entity.hover();
                entity.interact(action, option);
            }

            wiggleMouse();
            Time.sleep(Random.nextInt(1500, 2000));
        }
    }

    public static void wiggleMouse() {
        synchronized (mouseLock) {
            final Point p = Mouse.getLocation();
            Mouse.move(new Point(Random.nextInt(-50, 50) + p.x, Random.nextInt(-50, 50) + p.y));
        }
    }

    public static SceneObject getBestReachableNode() {
        final int rcLvl = Skills.getLevel(Skills.RUNECRAFTING);
        final SceneObject[] realNodes = SceneEntities.getLoaded(new Filter<SceneObject>() {
            @Override
            public boolean accept(final SceneObject sceneObject) {
                for (Node node : Node.getValues(SudoRunespan.isMembers())) {
                    if (node.getId() == sceneObject.getId() && rcLvl >= node.getLvl()) {
                        return sceneObject.getLocation().canReach();
                    }
                }
                return false;
            }
        });

        SceneObject bestNode = null;
        Node bestNodeValue = null;

        for (SceneObject object : realNodes) {
            final Node tempNode = Node.getNode(object.getId());

            if (bestNode == null || tempNode.ordinal() > bestNodeValue.ordinal() ||
                    (tempNode.ordinal() == bestNodeValue.ordinal() &&
                            distanceTo(object.getLocation()) < distanceTo(bestNode.getLocation()))) {
                bestNode = object;
                bestNodeValue = tempNode;
            }
        }

        return bestNode;
    }

    public static NPC getBestReachableMonster() {
        final int rcLvl = Skills.getLevel(Skills.RUNECRAFTING);
        final NPC[] realNpcs = NPCs.getLoaded(new Filter<NPC>() {
            @Override
            public boolean accept(final NPC npc) {
                for (Monster monster : Monster.getValues(SudoRunespan.isMembers())) {
                    if (monster.getId() == npc.getId() && rcLvl >= monster.getLvl()
                            && npc.getAnimation() != monster.getDeathAnimation()) {
                        return npc.getLocation().canReach();
                    }
                }
                return false;
            }
        });

        NPC bestMonster = null;
        Monster bestMonsterValue = null;

        for (NPC npc : realNpcs) {
            final Monster tempMonster = Monster.getMonster(npc.getId());

            if (bestMonster == null || tempMonster.ordinal() > bestMonsterValue.ordinal() ||
                    (tempMonster.ordinal() == bestMonsterValue.ordinal() &&
                            distanceTo(npc.getLocation()) < distanceTo(bestMonster.getLocation()))) {
                bestMonster = npc;
                bestMonsterValue = tempMonster;
            }
        }

        return bestMonster;
    }

    public static NPC getNearestReachableNPC(final int id) {
        return NPCs.getNearest(new Filter<NPC>() {
            @Override
            public boolean accept(NPC npc) {
                return npc.getId() == id && npc.getLocation().canReach();
            }
        });
    }

    public static String getTargetName(final Object obj) {
        if (obj instanceof NPC) {
            return getTargetName((NPC) obj);
        } else if (obj instanceof SceneObject) {
            return getTargetName((SceneObject) obj);
        }

        return null;
    }

    private static String getTargetName(final NPC npc) {
        return Monster.getMonster(npc.getId()).getName();
    }

    private static String getTargetName(final SceneObject scnObj) {
        return Node.getNode(scnObj.getId()).getName();
    }

    public static boolean isOrientedTowards(final Tile t2) {
        if (t2 == null) {
            return false;
        }

        final Tile t1 = Players.getLocal().getLocation();
        if (t1.equals(t2)) {
            return true;
        }

        final int xDiv = (t2.getX() - t1.getX()), yDiv = (t2.getY() - t1.getY());
        final double orient = Players.getLocal().getOrientation();
        final double angle = (Math.toDegrees(Math.atan2(yDiv, xDiv)) + 360) % 360;
        return Math.abs(angle - orient) < 5;   //small deviation when converting radians to and from degrees
    }

    public static boolean isMembersWorld(final int world) {
        for (final int worldID : MEMBERS_WORLDS) {
            if (worldID == world) {
                return true;
            }
        }
        return false;
    }

    public static int getCurrentWorld() {
        final WidgetChild wc = Widgets.get(550, 18);
        if (wc.validate()) {
            final String[] desc = wc.getText().split(" ");
            return Integer.parseInt(desc[2]);
        }

        return 0;
    }

    public static boolean validate(final Tile tile, final int id) {
        if (tile == null || id == -1 || !tile.canReach()) {
            return false;
        }

        final SceneObject obj = SceneEntities.getAt(tile);
        if (obj != null && obj.getId() == id) {
            return true;
        }

        final NPC npc = NPCs.getNearest(new Filter<NPC>() {
            @Override
            public boolean accept(NPC npc) {
                return npc.getId() == id && npc.getLocation().equals(tile);
            }
        });

        return npc != null;
    }
}
