package net.edge.content.skill.agility.test.gnome;

import net.edge.action.impl.ObjectAction;
import net.edge.content.skill.agility.impl.gnome.GnomeStrongholdAgility;
import net.edge.content.skill.agility.impl.gnome.impl.JumpOverBarrier;
import net.edge.content.skill.agility.impl.gnome.impl.PoleSwing;
import net.edge.content.skill.agility.obstacle.ObstacleAction;
import net.edge.content.skill.agility.obstacle.ObstacleType;
import net.edge.content.skill.agility.obstacle.impl.Climbable;
import net.edge.content.skill.agility.obstacle.impl.Movable;
import net.edge.content.skill.agility.obstacle.impl.Walkable;
import net.edge.content.skill.agility.test.Agility;
import net.edge.content.skill.agility.test.obstacle.Obstacle;
import net.edge.content.skill.agility.test.obstacle.impl.ClimbableObstacle;
import net.edge.content.skill.agility.test.obstacle.impl.WalkableObstacle;
import net.edge.task.LinkedTaskSequence;
import net.edge.world.entity.actor.player.Player;
import net.edge.world.locale.Position;
import net.edge.world.object.GameObject;

import java.util.function.Function;

/**
 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
 * @since 4-8-2017.
 */
public final class GnomeAgility extends Agility {

    /**
     * Constructs a new {@link Agility}.
     * @param player   {@link #player}.
     * @param object   {@link #object}.
     * @param crossing {@link #crossing}.
     */
    public GnomeAgility(Player player, GameObject object, Obstacle crossing) {
        super(player, object, crossing);
    }

    public static void action() {
        for(Obstacles obstacleFunction : Obstacles.values()) {
            ObjectAction action = new ObjectAction() {

                @Override
                public boolean click(Player player, GameObject object, int click) {
                    Obstacle obstacle = obstacleFunction.obstacles.apply(player);

                    Position walk = null;
                    Position[] targets = obstacle.start;
                    if(targets.length == 0) {
                        walk = targets[0];
                    } else {
                        double dist = 0;
                        for(Position p : targets) {
                            double d = player.getPosition().getDistance(p);
                            if(dist == 0 || dist > d) {
                                walk = p;
                                dist = d;
                            }
                        }
                    }
                    final Position dest = walk;
                    player.getMovementQueue().smartWalk(walk);
                    player.getMovementListener().append(() -> {
                        if(player.getPosition().same(dest)) {
                            new GnomeAgility(player, object, obstacleFunction.obstacles.apply(player)).start();
                        }
                    });

                    return true;
                }
            };
            for(int objId : obstacleFunction.ids) {
                action.registerFirst(objId);
                action.registerFirst(objId + 42003);
            }
        }
    }

    @Override
    public double experience() {
        return 39;
    }

    private static final Position[] OBSTACLE_NET_POSITIONS = new Position[]{new Position(2476, 3426), new Position(2475, 3426), new Position(2474, 3426), new Position(2473, 3426), new Position(2472, 3426), new Position(2471, 3426)};

    private static final Position[] OBSTACLE_NET_BACK_POSITIONS = new Position[]{new Position(2483, 3425), new Position(2484, 3425), new Position(2485, 3425), new Position(2486, 3425), new Position(2487, 3425), new Position(2488, 3425),};

    public enum Obstacles {
        LOG_BALANCE(2295, p -> new WalkableObstacle(new Position(2474, 3436), new Position(2474, 3429), 762, 1, 7)),
        OBSTACLE_NET(2285, p -> new ClimbableObstacle(OBSTACLE_NET_POSITIONS, new Position(p.getPosition().getX(), 3424, 1), 828, 1, 7.5)),
        TREE_BRANCH_UP(35970, p -> new ClimbableObstacle(new Position(2473, 3423, 1), new Position(2473, 3420, 2), 828, 1, 5)),
        TIGHT_ROPE(2312, p -> new WalkableObstacle(new Position(2477, 3420, 2), new Position(2483, 3420, 2), 762, 1, 7.5)),
        TREE_BRANCH_DOWN(new int[]{2314, 2315}, p -> new ClimbableObstacle(new Position(p.getPosition().getX(), p.getPosition().getY(), 2), new Position(p.getPosition().getX(), p.getPosition().getY(), 0), 828, 1, 5)),
        OBSTACLE_NET_BACK(2286, p -> new ClimbableObstacle(OBSTACLE_NET_BACK_POSITIONS, new Position(p.getPosition().getX(), 3427), 828, 1, 7.5));

        public final int[] ids;

        Function<Player, Obstacle> obstacles;

        Obstacles(int[] ids, Function<Player, Obstacle> obstacles) {
            this.obstacles = obstacles;
            this.ids = ids;
        }

        Obstacles(int id, Function<Player, Obstacle> obstacles) {
            this.obstacles = obstacles;
            this.ids = new int[]{id};
        }

    }
}
