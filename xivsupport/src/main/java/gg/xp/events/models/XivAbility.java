package gg.xp.events.models;

import java.io.Serializable;

public class XivAbility implements Serializable {
	private static final long serialVersionUID = -6170494857373031360L;
	private final long id;
	private final String name;

	public XivAbility(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return String.format("XivAbility(0x%X:%s)", id, name);
	}

}
