package com.artillexstudios.axminions.minions.actions.collectors.shapes;

import com.artillexstudios.axminions.minions.actions.collectors.CollectorContext;
import com.artillexstudios.axminions.minions.actions.collectors.CollectorOptionNotPresentException;

public abstract class CollectorShape {

    public abstract void getBlocks(CollectorContext context) throws CollectorOptionNotPresentException;

    public abstract void getEntities(CollectorContext context) throws CollectorOptionNotPresentException;
}
