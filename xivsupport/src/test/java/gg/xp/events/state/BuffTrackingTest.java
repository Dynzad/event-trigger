package gg.xp.events.state;

import gg.xp.events.DummyEventToForceAutoScan;
import gg.xp.events.EventDistributor;
import gg.xp.events.TestEventCollector;
import gg.xp.events.actlines.data.Job;
import gg.xp.events.actlines.events.BuffApplied;
import gg.xp.events.actlines.events.BuffRemoved;
import gg.xp.events.actlines.events.RawRemoveCombatantEvent;
import gg.xp.events.actlines.events.XivBuffsUpdatedEvent;
import gg.xp.events.actlines.events.ZoneChangeEvent;
import gg.xp.events.models.HitPoints;
import gg.xp.events.models.Position;
import gg.xp.events.models.XivCombatant;
import gg.xp.events.models.XivPlayerCharacter;
import gg.xp.events.models.XivStatusEffect;
import gg.xp.events.models.XivWorld;
import gg.xp.events.models.XivZone;
import gg.xp.events.triggers.jobs.StatusEffectRepository;
import gg.xp.sys.XivMain;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.picocontainer.MutablePicoContainer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class BuffTrackingTest {
	@Test
	void buffTrackingTest() {
		MutablePicoContainer pico = XivMain.testingMasterInit();
		StatusEffectRepository repo;
		TestEventCollector coll = new TestEventCollector();
		EventDistributor distributor = pico.getComponent(EventDistributor.class);
		distributor.registerHandler(coll);

		XivStatusEffect testBuff1 = new XivStatusEffect(123, "Test Dot");
		XivStatusEffect testBuff2 = new XivStatusEffect(456, "Some Debuff");
		XivCombatant player = new XivPlayerCharacter(1,
				"The Player",
				Job.WHM,
				new XivWorld(),
				true,
				1,
				new HitPoints(123, 456),
				new Position(0, 0, 0, 0),
				0,
				0,
				0,
				80,
				0
		);
		XivCombatant otherPlayer = new XivPlayerCharacter(2,
				"Party Member",
				Job.BLM,
				new XivWorld(),
				true,
				1,
				new HitPoints(123, 456),
				new Position(0, 0, 0, 0),
				0,
				0,
				0,
				80,
				0
		);
		XivCombatant enemy1 = new XivCombatant(5,
				"Some Boss",
				false,
				false,
				2,
				new HitPoints(1000, 2000),
				new Position(0, 0, 0, 0),
				123,
				456,
				0,
				80,
				0
		);
		XivCombatant enemy2 = new XivCombatant(6,
				"Other Boss",
				false,
				false,
				2,
				new HitPoints(1000, 2000),
				new Position(0, 0, 0, 0),
				123,
				456,
				0,
				80,
				0
		);
		BuffApplied player1DotsEnemy1 = new BuffApplied(testBuff1, 10, player, enemy1, 1);
		BuffApplied player1DotsEnemy2 = new BuffApplied(testBuff1, 10, player, enemy2, 1);
		BuffApplied player1DotsEnemy1again = new BuffApplied(testBuff1, 10, player, enemy1, 1);
		BuffApplied player2DotsEnemy1 = new BuffApplied(testBuff1, 10, otherPlayer, enemy1, 1);
		BuffApplied player2DebuffsEnemy1 = new BuffApplied(testBuff2, 10, otherPlayer, enemy1, 1);
		BuffRemoved player1DotExpiresOnEnemy1 = new BuffRemoved(testBuff1, 0, player, enemy1, 1);
		RawRemoveCombatantEvent enemy1removed = new RawRemoveCombatantEvent(enemy1);
		RawRemoveCombatantEvent player2removed = new RawRemoveCombatantEvent(otherPlayer);
		ZoneChangeEvent zoneChange = new ZoneChangeEvent(new XivZone(123, "Help I'm trapped in an integration test"));

		// Dummy event to force initialization
		distributor.acceptEvent(new DummyEventToForceAutoScan());
		// Initial state - empty
		repo = pico.getComponent(StatusEffectRepository.class);
		Assert.assertEquals(repo.getBuffs().size(), 0);

		// Apply one status effect to one enemy
		{
			distributor.acceptEvent(player1DotsEnemy1);
			List<BuffApplied> buffs = repo.getBuffs();
			Assert.assertEquals(buffs.size(), 1);
			Assert.assertEquals(buffs.get(0), player1DotsEnemy1);
			Assert.assertFalse(player1DotsEnemy1.isRefresh());
			Assert.assertEquals(coll.getEventsOf(XivBuffsUpdatedEvent.class).size(), 1);
			coll.clear();
		}
		// Apply the same status effect to another enemy
		{
			distributor.acceptEvent(player1DotsEnemy2);
			List<BuffApplied> buffs = repo.getBuffs();
			Assert.assertEquals(buffs.size(), 2);
			MatcherAssert.assertThat(buffs, Matchers.containsInAnyOrder(player1DotsEnemy1, player1DotsEnemy2));
			Assert.assertEquals(coll.getEventsOf(XivBuffsUpdatedEvent.class).size(), 1);
			Assert.assertFalse(player1DotsEnemy2.isRefresh());
			coll.clear();
		}
		// Refresh dot on first enemy
		{
			distributor.acceptEvent(player1DotsEnemy1again);
			List<BuffApplied> buffs = repo.getBuffs();
			Assert.assertEquals(buffs.size(), 2);
			MatcherAssert.assertThat(buffs, Matchers.containsInAnyOrder(player1DotsEnemy1again, player1DotsEnemy2));
			Assert.assertEquals(coll.getEventsOf(XivBuffsUpdatedEvent.class).size(), 1);
			Assert.assertTrue(player1DotsEnemy1again.isRefresh());
			coll.clear();
		}
		// Player 2 dots first enemy
		{
			distributor.acceptEvent(player2DotsEnemy1);
			List<BuffApplied> buffs = repo.getBuffs();
			Assert.assertEquals(buffs.size(), 3);
			MatcherAssert.assertThat(buffs, Matchers.containsInAnyOrder(player1DotsEnemy1again, player1DotsEnemy2, player2DotsEnemy1));
			Assert.assertEquals(coll.getEventsOf(XivBuffsUpdatedEvent.class).size(), 1);
			Assert.assertFalse(player2DotsEnemy1.isRefresh());
			coll.clear();
		}
		// Player 2 puts another status on first enemy
		{
			distributor.acceptEvent(player2DebuffsEnemy1);
			List<BuffApplied> buffs = repo.getBuffs();
			Assert.assertEquals(buffs.size(), 4);
			MatcherAssert.assertThat(buffs, Matchers.containsInAnyOrder(player1DotsEnemy1again, player1DotsEnemy2, player2DotsEnemy1, player2DebuffsEnemy1));
			Assert.assertEquals(coll.getEventsOf(XivBuffsUpdatedEvent.class).size(), 1);
			Assert.assertFalse(player2DebuffsEnemy1.isRefresh());
			coll.clear();
		}
		// Player 1's dot expires on first enemy
		{
			distributor.acceptEvent(player1DotExpiresOnEnemy1);
			List<BuffApplied> buffs = repo.getBuffs();
			Assert.assertEquals(buffs.size(), 3);
			MatcherAssert.assertThat(buffs, Matchers.containsInAnyOrder(player1DotsEnemy2, player2DotsEnemy1, player2DebuffsEnemy1));
			Assert.assertEquals(coll.getEventsOf(XivBuffsUpdatedEvent.class).size(), 1);
			coll.clear();
		}
		// Enemy 1 removed - all debuffs should be removed from it
		{
			distributor.acceptEvent(enemy1removed);
			List<BuffApplied> buffs = repo.getBuffs();
			Assert.assertEquals(buffs.size(), 1);
			MatcherAssert.assertThat(buffs, Matchers.containsInAnyOrder(player1DotsEnemy2));
			Assert.assertEquals(coll.getEventsOf(XivBuffsUpdatedEvent.class).size(), 1);
			coll.clear();
		}
		// Player 2 removed - nothing should happen because removing a target does nothing
		{
			distributor.acceptEvent(player2removed);
			List<BuffApplied> buffs = repo.getBuffs();
			Assert.assertEquals(buffs.size(), 1);
			MatcherAssert.assertThat(buffs, Matchers.containsInAnyOrder(player1DotsEnemy2));
			Assert.assertEquals(coll.getEventsOf(XivBuffsUpdatedEvent.class).size(), 0);
		}
		// Zone change - everything should be cleared
		{
			distributor.acceptEvent(zoneChange);
			List<BuffApplied> buffs = repo.getBuffs();
			Assert.assertEquals(buffs.size(), 0);
			Assert.assertEquals(coll.getEventsOf(XivBuffsUpdatedEvent.class).size(), 1);
		}

	}
}
