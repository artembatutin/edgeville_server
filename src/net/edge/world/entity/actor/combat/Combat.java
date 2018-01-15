package net.edge.world.entity.actor.combat;

import net.edge.task.Task;
import net.edge.util.Stopwatch;
import net.edge.world.entity.actor.Actor;
import net.edge.world.entity.actor.combat.attack.CombatModifier;
import net.edge.world.entity.actor.combat.attack.CurseModifier;
import net.edge.world.entity.actor.combat.attack.FightType;
import net.edge.world.entity.actor.combat.attack.listener.CombatListener;
import net.edge.world.entity.actor.combat.hit.CombatData;
import net.edge.world.entity.actor.combat.hit.CombatHit;
import net.edge.world.entity.actor.combat.hit.Hit;
import net.edge.world.entity.actor.combat.strategy.CombatStrategy;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Combat<T extends Actor> {

    private final T attacker;
    private Actor defender;

    private Actor lastAttacker;
    private Actor lastDefender;

    private final Stopwatch lastAttacked = new Stopwatch();
    private final Stopwatch lastBlocked = new Stopwatch();
    private FightType type;

    public final Map<Actor, CurseModifier> curseModifiers = new HashMap<>();

    private CombatStrategy<? super T> lastStrategy;
    private final List<CombatModifier> modifiers = new LinkedList<>();
    private final List<CombatListener<? super T>> listeners = new LinkedList<>();
    private final Deque<CombatListener<? super T>> pendingAddition = new LinkedList<>();
    private final Deque<CombatListener<? super T>> pendingRemoval = new LinkedList<>();

    private final CombatDamage damageCache = new CombatDamage();
    private final Deque<CombatData<T>> combatQueue = new LinkedList<>();
    private final Deque<Hit> damageQueue = new LinkedList<>();

    private final int[] hitsplatCooldowns = new int[4];
    private final int[] cooldowns = new int[3];
    private boolean within;

    public Combat(T attacker) {
        this.attacker = attacker;
        type = FightType.UNARMED_PUNCH;
    }

    public void attack(Actor defender) {
        if(defender == null) {
            return;
        }

        CombatStrategy<? super T> strategy = attacker.getStrategy();
        within = strategy != null && strategy.withinDistance(attacker, defender);

        if (!CombatUtil.canAttack(attacker, defender)) {
            return;
        }

        this.defender = defender;
        this.lastDefender = defender;
        attacker.faceEntity(defender);
        attacker.getMovementQueue().follow(defender);
    }

    public void tick() {
        updateListeners();
        for (int index = 0; index < cooldowns.length; index++) {
            if (cooldowns[index] > 0) {
                cooldowns[index]--;
            } else {
                if (defender == null) {
                    continue;
                }

                CombatStrategy<? super T> strategy = attacker.getStrategy();
                setLastStrategy(strategy);

                if (strategy.getCombatType().ordinal() != index) {
                    continue;
                }

                submitStrategy(defender, strategy);
            }
        }

        while (!combatQueue.isEmpty()) {
            CombatData<T> data = combatQueue.poll();
            hitTask(data).submit();
        }

        for (int index = 0, sent = 0; index < hitsplatCooldowns.length; index++) {
            if (hitsplatCooldowns[index] > 0) {
                hitsplatCooldowns[index]--;
            } else if (sent < 2 && sendNextHitsplat()) {
                hitsplatCooldowns[index] = 2;
                sent++;
            }
        }
    }

    public void setLastStrategy(CombatStrategy<? super T> strategy) {
        if (strategy != lastStrategy) {
            if (lastStrategy != null) {
                removeModifiers(lastStrategy);
            }

            addModifiers(strategy);
            lastStrategy = strategy;
        }
    }

    private boolean sendNextHitsplat() {
        if (damageQueue.isEmpty()) {
            return false;
        }

        if (attacker.isDead() || attacker.isNeedsPlacement() || attacker.isTeleporting()) {
            damageQueue.clear();
            return false;
        }

        Hit hit = damageQueue.poll();
        attacker.writeDamage(hit);
        return true;
    }

    private void updateListeners() {
        if (!pendingAddition.isEmpty()) {
            for (Iterator<CombatListener<? super T>> iterator = pendingAddition.iterator(); iterator.hasNext(); ) {
                CombatListener<? super T> next = iterator.next();
                addModifiers(next);
                listeners.add(next);
                iterator.remove();
            }
        }

        if (!pendingRemoval.isEmpty()) {
            for (Iterator<CombatListener<? super T>> iterator = pendingRemoval.iterator(); iterator.hasNext(); ) {
                CombatListener<? super T> next = iterator.next();
                listeners.remove(next);
                removeModifiers(next);
                iterator.remove();
            }
        }
    }

    public boolean submitStrategy(Actor defender, CombatStrategy<? super T> strategy) {
        if (!canAttack(defender, strategy)) {
            return false;
        }

        int delayIndex = strategy.getCombatType().ordinal();
        submitHits(defender, strategy, strategy.getHits(attacker, defender));
        setDelay(delayIndex, strategy.getAttackDelay(attacker, defender, type));
        return true;
    }

    private void submitHits(Actor defender, CombatStrategy<? super T> strategy, CombatHit... hits) {
        boolean first = true;
        for (int index = 0; index < hits.length; index++) {
            CombatHit hit = hits[index];

            CombatData<T> data = new CombatData<>(attacker, defender, hit, strategy, index == hits.length - 1);
            if (first) {
                start(defender, strategy, hits);
                first = false;
            }

            attack(defender, hit, strategy);
            combatQueue.add(data);
        }
    }

    public void submitHits(Actor defender, CombatHit... hits) {
        submitHits(defender, attacker.getStrategy(), hits);
    }

    public void queueDamage(Hit hit) {
        if (damageQueue.size() >= 8) {
            return;
        }

        if (hit.getDamage() < 0) {
            damageQueue.addFirst(hit);
        } else {
            damageQueue.addLast(hit);
        }
    }

    public void setDelay(int index, int delay) {
        for (int idx = 0; idx < cooldowns.length; idx++) {
            if (idx != index) {
                cooldowns[idx] += 2;
            } else if (cooldowns[idx] < delay) {
                cooldowns[idx] = delay;
            }
        }
    }

    public void setDelay(int delay) {
        for (int idx = 0; idx < cooldowns.length; idx++) {
            setDelay(idx, delay);
        }
    }

    public void resetDelay() {
        if(lastStrategy == null) {
            return;
        }

        cooldowns[lastStrategy.getCombatType().ordinal()] = lastStrategy.getAttackDelay(attacker, defender, type);
    }

    private boolean canAttack(Actor defender, CombatStrategy<? super T> strategy) {
        if (!within) {
            return false;
        }
        if (!CombatUtil.canAttack(attacker, defender)) {
            return false;
        }
        if (!strategy.canAttack(attacker, defender)) {
            return false;
        }
        for (CombatListener<? super T> listener : listeners) {
            if (!listener.canAttack(attacker, defender)) {
                return false;
            }
        }
        return true;
    }

    private void start(Actor defender, CombatStrategy<? super T> strategy, Hit... hits) {
        if (!CombatUtil.canAttack(attacker, defender)) {
            combatQueue.removeIf(_hit -> _hit.getDefender() == defender);
            defender.getCombat().damageQueue.clear();
            reset(true, true);
            return;
        }

        strategy.start(attacker, defender, hits);
        listeners.forEach(listener -> listener.start(attacker, defender, hits));
    }

    private void attack(Actor defender, Hit hit, CombatStrategy<? super T> strategy) {
        if (!CombatUtil.canAttack(attacker, defender)) {
            combatQueue.removeIf(_hit -> _hit.getDefender() == defender);
            defender.getCombat().damageQueue.clear();
            reset(true, true);
            return;
        }

        lastDefender = defender;
        lastAttacked.reset();

        strategy.attack(attacker, defender, hit);
        listeners.forEach(listener -> listener.attack(attacker, defender, hit));
    }

    private void block(Actor attacker, Hit hit, CombatType combatType) {
        T defender = this.attacker;
        lastBlocked.reset();
        lastAttacker = attacker;
        listeners.forEach(listener -> listener.block(attacker, defender, hit, combatType));
        if (defender.getCombat().getDefender() == null && defender.isAutoRetaliate() || defender.getStrategy().hitBack()) {
            defender.getCombat().attack(attacker);
        } else if(defender.isMob() && !defender.getCombat().isUnderAttack()) {
            defender.getCombat().attack(attacker);
        }
    }

    private void hit(Actor defender, Hit hit, CombatStrategy<? super T> strategy) {
        if (!CombatUtil.canAttack(attacker, defender)) {
            combatQueue.removeIf(_hit -> _hit.getDefender() == defender);
            defender.getCombat().damageQueue.clear();
            reset(true, true);
            return;
        }
        defender.getCombat().block(attacker, hit, strategy.getCombatType());
        if (strategy.getCombatType() != CombatType.MAGIC || defender.isMob()) {
            defender.animation(CombatUtil.getBlockAnimation(defender));
        }

        strategy.hit(attacker, defender, hit);
        listeners.forEach(listener -> listener.hit(attacker, defender, hit));
    }

    private void hitsplat(Actor defender, Hit hit, CombatStrategy<? super T> strategy) {
        if (!CombatUtil.canAttack(attacker, defender)) {
            combatQueue.removeIf(_hit -> _hit.getDefender() == defender);
            defender.getCombat().damageQueue.clear();
            reset(true, true);
            return;
        }

        if (strategy.getCombatType() != CombatType.MAGIC || hit.isAccurate()) {
            defender.getCombat().queueDamage(hit);
            defender.getCombat().damageCache.add(attacker, hit.getDamage());

            if (defender.getCurrentHealth() <= 0) {
                defender.getCombat().onDeath(attacker, hit);
            }
        }
    }

    private void onDeath(Actor attacker, Hit hit) {
        T defender = this.attacker;
        listeners.forEach(listener -> listener.onDeath(attacker, defender, hit));
        defender.getMovementQueue().reset();
        reset(true, true);
    }

    private void finishIncoming(Actor attacker) {
        T defender = this.attacker;
        defender.getStrategy().finishIncoming(attacker, defender);
        listeners.forEach(listener -> listener.finishIncoming(attacker, defender));
    }

    private void finishOutgoing(Actor defender, CombatStrategy<? super T> strategy) {
        strategy.finishOutgoing(attacker, defender);
        defender.getCombat().finishIncoming(attacker);
    }


    public void reset(boolean fullCombat, boolean resetWalk) {
        if (defender != null) {
            if(fullCombat) {
                Actor def = defender;
                defender = null;
                def.getCombat().reset(false, true);
            } else {
                defender = null;
            }
            attacker.faceEntity(null);
            attacker.setFollowing(false);
        }
        if(resetWalk) {
            attacker.getMovementQueue().reset();
        }
    }

    public void addModifier(CombatModifier modifier) {
        modifiers.add(modifier);
    }

    public void removeModifier(CombatModifier modifier) {
        modifiers.remove(modifier);
    }

    private void addModifiers(CombatListener<? super T> listener) {
        listener.getModifier(attacker).ifPresent(this::addModifier);
    }

    private void removeModifiers(CombatListener<? super T> listener) {
        listener.getModifier(attacker).ifPresent(this::removeModifier);
    }

    public void addListener(CombatListener<? super T> attack) {
        if (listeners.contains(attack) || pendingAddition.contains(attack)) {
            return;
        }
        pendingAddition.add(attack);
    }

    public void removeListener(CombatListener<? super T> attack) {
        if (!listeners.contains(attack) || pendingRemoval.contains(attack)) {
            return;
        }
        pendingRemoval.add(attack);
    }

    public boolean inCombat() {
        return isAttacking() || isUnderAttack();
    }

    public boolean isAttacking() {
        return lastDefender != null && !stopwatchElapsed(lastAttacked, CombatConstants.COMBAT_TIMER);
    }

    public boolean isUnderAttack() {
        return lastAttacker != null && !stopwatchElapsed(lastBlocked, CombatConstants.COMBAT_TIMER);
    }

    public boolean isAttacking(Actor defender) {
        return defender != null && defender.same(lastDefender) && !stopwatchElapsed(lastAttacked, CombatConstants.COMBAT_TIMER);
    }

    public boolean isUnderAttackBy(Actor attacker) {
        return attacker != null && attacker.same(lastAttacker) && !stopwatchElapsed(lastBlocked, CombatConstants.COMBAT_TIMER);
    }

    public int modAttack(Actor defender, int roll) {
        if(!modifiers.isEmpty()) {
            for(CombatModifier modifier : modifiers) {
                roll = modifier.modifyAttack(attacker, defender, roll);
            }
        }
        return roll;
    }

    public int modDefence(Actor defender, int roll) {
        if(!modifiers.isEmpty()) {
            for(CombatModifier modifier : modifiers) {
                roll = modifier.modifyDefence(attacker, defender, roll);
            }
        }
        return roll;
    }

    public int modDamage(Actor defender, int roll) {
        if(!modifiers.isEmpty()) {
            for(CombatModifier modifier : modifiers) {
                roll = modifier.modifyDamage(attacker, defender, roll);
            }
        }
        return roll;
    }

    public FightType getFightType() {
        return type;
    }

    public void setFightType(FightType type) {
        this.type = type;
    }

    public boolean checkWithin() {
        CombatStrategy<? super T> strategy = attacker.getStrategy();
        within = strategy != null && defender != null && strategy.withinDistance(attacker, defender);
        return within;
    }

    public boolean isWithin() {
        return within;
    }

    public Actor getDefender() {
        return defender;
    }

    public CombatDamage getDamageCache() {
        return damageCache;
    }

    public Actor getLastAttacker() {
        return lastAttacker;
    }

    public Actor getLastDefender() {
        return lastDefender;
    }

    private Task hitTask(CombatData<T> data) {
        return new Task(data.getHitDelay(), data.getHitDelay() == 0) {
            @Override
            protected void execute() {
                hit(data.getDefender(), data.getHit(), data.getStrategy());
                hitsplatTask(data).submit();
                cancel();
            }
        };
    }

    private Task hitsplatTask(CombatData<T> data) {
        return new Task(data.getHitsplatDelay(), data.getHitsplatDelay() == 0) {
            @Override
            protected void execute() {
                hitsplat(data.getDefender(), data.getHit(), data.getStrategy());

                if (data.isLastHit()) {
                    finishOutgoing(data.getDefender(), data.getStrategy());
                }

                cancel();
            }
        };
    }

    public boolean hasPassed(int delay) {
        return stopwatchElapsed(lastAttacked, delay) && stopwatchElapsed(lastBlocked, delay);
    }

    private static boolean stopwatchElapsed(Stopwatch stopwatch, int seconds) {
        return stopwatch.elapsed(seconds, TimeUnit.SECONDS);
    }

    public long elapsedTime() {
        long attacked = lastAttacked.elapsedTime();
        long blocked = lastBlocked.elapsedTime();
        return blocked > attacked ? attacked : blocked;
    }



}
