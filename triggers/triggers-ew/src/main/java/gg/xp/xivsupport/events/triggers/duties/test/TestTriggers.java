package gg.xp.xivsupport.events.triggers.duties.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import gg.xp.reevent.events.EventContext;
import gg.xp.reevent.scan.AutoChildEventHandler;
import gg.xp.reevent.scan.FilteredEventHandler;
import gg.xp.reevent.scan.HandleEvents;
import gg.xp.xivdata.data.duties.*;
import gg.xp.xivsupport.callouts.CalloutRepo;
import gg.xp.xivsupport.events.actlines.events.AbilityCastStart;
import gg.xp.xivsupport.events.actlines.events.ChatLineEvent;
import gg.xp.xivsupport.events.actlines.events.actorcontrol.DutyCommenceEvent;
import gg.xp.xivsupport.events.misc.EchoEvent;
import gg.xp.xivsupport.events.misc.pulls.PullEndedEvent;
import gg.xp.xivsupport.events.state.XivState;
import gg.xp.xivsupport.events.state.combatstate.ActiveCastRepository;

@CalloutRepo(name = "TestTriggers", duty = KnownDuty.None)
public class TestTriggers extends AutoChildEventHandler implements FilteredEventHandler {

	private SimpleDateFormat logEntryFormat = new SimpleDateFormat("HH:mm:ss");
	private SimpleDateFormat logNameFormat = new SimpleDateFormat("M-d_HHmmss");
	private String logPath;
	private File file;
	private XivState state;
	private ActiveCastRepository acr;

	public TestTriggers(XivState state, ActiveCastRepository acr) {
		this.state = state;
		this.acr = acr;
	}

	@Override
	public boolean enabled(EventContext context) {
		//return state.dutyIs(KnownDuty.None);
		return false;
		//return true;
	}

	private String appendTime() {
		return '[' + logEntryFormat.format(Calendar.getInstance().getTime()) + ']';
	}

	private void logEvent(String message) {
		if (file == null) {
			try {
				this.logPath = "D:/TriggEvent/CustomLogs/Testing/" + logNameFormat.format(Calendar.getInstance().getTime()) + ".log";
				file = new File(logPath);
				if (file.createNewFile()) {
					System.out.println("File created: " + file.getName());
				}
				else {
					System.out.println("File already exists.");
				}

			}
			catch (IOException e) {
				System.out.println("An error occurred.");
			}
		}
		try {
			FileWriter myWriter = new FileWriter(logPath, true);
			myWriter.write(message + "\n");
			myWriter.close();
			System.out.println("Log Event: " + message + "\n Path: " + logPath);
		} catch (IOException e) {
			System.out.println("An error occurred.");
		}
	}

	@HandleEvents
	public void dutyCommenced(EventContext context, DutyCommenceEvent event) {
		logEvent("\n - - - - - - - - \n" + appendTime() + "Duty Commenced");
	}

	@HandleEvents
	public void pullEnd(EventContext context, PullEndedEvent event) {
		logEvent(appendTime() + "Pull ended");
	}

	@HandleEvents
	public void playerMessage(EventContext context, ChatLineEvent event) {
		if (event.getName().contains("Dynzad")) {
			logEvent(appendTime() + event.getName() + ": " + event.getLine());
		}
	}

	@HandleEvents
	public void echoMessage(EventContext context, EchoEvent event) {
		logEvent(appendTime() + "Echo: " + event.getLine());
		System.out.println(state.getZone().getId());
	}

	@HandleEvents
	public void abilityCastStart(EventContext context, AbilityCastStart event) {
		if(!event.getSource().isPc()) {
			String sourceText = event.getSource().getName() + "[" + Long.toHexString(event.getSource().getId()).toUpperCase() + "]: ";
			String castText = event.getAbility().getName() + "[" + Long.toHexString(event.getAbility().getId()).toUpperCase() + "] started cast";
			logEvent(appendTime() + sourceText + castText);
		}
	}
}
