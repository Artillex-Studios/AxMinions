package com.artillexstudios.axminions.jfr;

import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;

@StackTrace(value = false)
@Label("Minion Tick Event")
@Description("Called when minions tick")
public class MinionTickEvent extends Event {
}
