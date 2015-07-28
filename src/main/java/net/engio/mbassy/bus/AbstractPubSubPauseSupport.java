package net.engio.mbassy.bus;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import net.engio.mbassy.bus.common.PubSubPauseSupport;
import net.engio.mbassy.bus.config.IBusConfiguration;

public abstract class AbstractPubSubPauseSupport<T> extends AbstractPubSubSupport<T> implements PubSubPauseSupport<T> {

    private final ConcurrentLinkedQueue<T> msgPauseQueue = new ConcurrentLinkedQueue<T>();

    private final AtomicBoolean paused = new AtomicBoolean();

    public AbstractPubSubPauseSupport(final IBusConfiguration configuration) {
	super(configuration);
    }

    @Override
    public void pause() {
	paused.set(true);
    }

    @Override
    public void resume() {
	if (!paused.get()) return;

	paused.set(false);
	while (msgPauseQueue.size() > 0) {
	    publish (msgPauseQueue.poll());
	}
    }

    @Override
    public boolean isPaused() {
	return paused.get();
    }

    protected final void enqueueMessageOnPause(final T msg) {
	if (!isPaused()) return;
	msgPauseQueue.offer(msg);
    }
}
