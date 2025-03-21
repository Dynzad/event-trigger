package gg.xp.xivsupport.timelines;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

public class TimelineParserTest {

	@Test
	void timelineMinimal() {
		TimelineEntry textFileTimelineEntry = TimelineParser.parseRaw("458.7 \"Firebomb\"");
		Assert.assertNotNull(textFileTimelineEntry);
		Assert.assertEquals(textFileTimelineEntry.time(), 458.7);
		Assert.assertEquals(textFileTimelineEntry.name(), "Firebomb");
		Assert.assertNull(textFileTimelineEntry.sync());
		Assert.assertNull(textFileTimelineEntry.duration());
		assertDefaultTimelineWindow(textFileTimelineEntry);
		Assert.assertNull(textFileTimelineEntry.jump());
	}

	@Test
	void timelineWithSync() {
		TimelineEntry textFileTimelineEntry = TimelineParser.parseRaw("2.0 \"Shield Skewer\" sync / 1[56]:[^:]*:Rhitahtyn sas Arvina:471:/");
		Assert.assertNotNull(textFileTimelineEntry);
		Assert.assertEquals(textFileTimelineEntry.time(), 2.0d);
		Assert.assertEquals(textFileTimelineEntry.name(), "Shield Skewer");
		Assert.assertEquals(textFileTimelineEntry.sync().pattern(), " 1[56]:[^:]*:Rhitahtyn sas Arvina:471:");
		Assert.assertNull(textFileTimelineEntry.duration());
		assertDefaultTimelineWindow(textFileTimelineEntry);
		Assert.assertNull(textFileTimelineEntry.jump());
	}

	@Test
	void timelineWithSyncAndWindow() {
		TimelineEntry textFileTimelineEntry = TimelineParser.parseRaw("413.2 \"Shrapnel Shell\" sync / 1[56]:[^:]*:Rhitahtyn sas Arvina:474:/ window 20,20");
		Assert.assertNotNull(textFileTimelineEntry);
		Assert.assertEquals(textFileTimelineEntry.time(), 413.2d);
		Assert.assertEquals(textFileTimelineEntry.name(), "Shrapnel Shell");
		Assert.assertEquals(textFileTimelineEntry.sync().pattern(), " 1[56]:[^:]*:Rhitahtyn sas Arvina:474:");
		Assert.assertNull(textFileTimelineEntry.duration());
		Assert.assertNotNull(textFileTimelineEntry.timelineWindow());
		Assert.assertEquals(textFileTimelineEntry.timelineWindow().start(), 20.0d);
		Assert.assertEquals(textFileTimelineEntry.timelineWindow().end(), 20.0d);
		Assert.assertNull(textFileTimelineEntry.jump());
	}

	@Test
	void timelineWithSyncWindowAndJump() {
		TimelineEntry textFileTimelineEntry = TimelineParser.parseRaw("449.5 \"Shrapnel Shell\" sync / 1[56]:[^:]*:Rhitahtyn sas Arvina:474:/ window 20,100 jump 413.2");
		Assert.assertNotNull(textFileTimelineEntry);
		Assert.assertEquals(textFileTimelineEntry.time(), 449.5d);
		Assert.assertEquals(textFileTimelineEntry.name(), "Shrapnel Shell");
		Assert.assertEquals(textFileTimelineEntry.sync().pattern(), " 1[56]:[^:]*:Rhitahtyn sas Arvina:474:");
		Assert.assertNull(textFileTimelineEntry.duration());
		Assert.assertNotNull(textFileTimelineEntry.timelineWindow());
		Assert.assertEquals(textFileTimelineEntry.timelineWindow().start(), 20.0d);
		Assert.assertEquals(textFileTimelineEntry.timelineWindow().end(), 100.0d);
		Assert.assertEquals(textFileTimelineEntry.jump(), (Double) 413.2d);
	}

	@Test
	void timelineWithSyncAndDuration() {
		TimelineEntry textFileTimelineEntry = TimelineParser.parseRaw("705.7 \"J Storm + Waves x16\" sync / 1[56]:[^:]*:Brute Justice:4876:/ duration 50");
		Assert.assertNotNull(textFileTimelineEntry);
		Assert.assertEquals(textFileTimelineEntry.time(), 705.7d);
		Assert.assertEquals(textFileTimelineEntry.name(), "J Storm + Waves x16");
		Assert.assertEquals(textFileTimelineEntry.sync().pattern(), " 1[56]:[^:]*:Brute Justice:4876:");
		Assert.assertEquals(textFileTimelineEntry.duration(), (Double) 50.0d);
		assertDefaultTimelineWindow(textFileTimelineEntry);
		Assert.assertNull(textFileTimelineEntry.jump());
	}

	@Test
	void timelineSyncOnly() {
		TimelineEntry textFileTimelineEntry = TimelineParser.parseRaw("584.3 \"--sync--\" sync / 00:0044:[^:]*:Your defeat will bring/ window 600,0");
		Assert.assertNotNull(textFileTimelineEntry);
		Assert.assertEquals(textFileTimelineEntry.time(), 584.3d);
		Assert.assertNull(textFileTimelineEntry.name());
		Assert.assertEquals(textFileTimelineEntry.sync().pattern(), " 00:0044:[^:]*:Your defeat will bring");
		Assert.assertNull(textFileTimelineEntry.duration());
		Assert.assertNotNull(textFileTimelineEntry.timelineWindow());
		Assert.assertEquals(textFileTimelineEntry.timelineWindow().start(), 600.0d);
		Assert.assertEquals(textFileTimelineEntry.timelineWindow().end(), 0.0d);
		Assert.assertNull(textFileTimelineEntry.jump());
	}

	@Test
	void testDifferentOrder() {
		TimelineEntry textFileTimelineEntry = TimelineParser.parseRaw("674.7 \"Holy Comet x7\" duration 12.7 sync / 1[56]:[^:]*:Ser Noudenet:63E8:/");
		Assert.assertNotNull(textFileTimelineEntry);
		Assert.assertEquals(textFileTimelineEntry.time(), 674.7d);
		Assert.assertEquals(textFileTimelineEntry.name(), "Holy Comet x7");
		Assert.assertEquals(textFileTimelineEntry.sync().pattern(), " 1[56]:[^:]*:Ser Noudenet:63E8:");
		Assert.assertEquals(textFileTimelineEntry.duration(), (Double) 12.7d);
		Assert.assertNotNull(textFileTimelineEntry.timelineWindow());
		Assert.assertEquals(textFileTimelineEntry.timelineWindow().start(), 2.5);
		Assert.assertEquals(textFileTimelineEntry.timelineWindow().end(), 2.5);
		Assert.assertNull(textFileTimelineEntry.jump());
	}

	@Test
	void timelineLabel() {
		TimelineEntry textFileTimelineEntry = TimelineParser.parseRaw("458.7 label \"Firebomb\"");
		Assert.assertNotNull(textFileTimelineEntry);
		Assert.assertEquals(textFileTimelineEntry.time(), 458.7);
		Assert.assertEquals(textFileTimelineEntry.name(), "Firebomb");
		Assert.assertTrue(textFileTimelineEntry.isLabel());
		Assert.assertSame(textFileTimelineEntry.timelineWindow(), TimelineWindow.NONE);
	}

	@Test
	void timelineForceJumpToLabel() {
		TimelineEntry textFileTimelineEntry = TimelineParser.parseRaw("458.7 \"Firebomb\" forcejump \"foo\"");
		Assert.assertNotNull(textFileTimelineEntry);
		Assert.assertEquals(textFileTimelineEntry.time(), 458.7);
		Assert.assertEquals(textFileTimelineEntry.name(), "Firebomb");
		Assert.assertNull(textFileTimelineEntry.sync());
		Assert.assertNull(textFileTimelineEntry.duration());
		assertDefaultTimelineWindow(textFileTimelineEntry);
		Assert.assertNull(textFileTimelineEntry.jump());
		Assert.assertEquals(textFileTimelineEntry.jumpLabel(), "foo");
		Assert.assertTrue(textFileTimelineEntry.forceJump());
	}

	private void assertDefaultTimelineWindow(TimelineEntry entry) {
		Assert.assertNotNull(entry.timelineWindow());
		Assert.assertEquals(entry.timelineWindow().start(), 2.5d);
		Assert.assertEquals(entry.timelineWindow().end(), 2.5d);
	}

}