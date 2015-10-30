package net.engio.mbassy.bus;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import net.engio.mbassy.bus.common.IMessageBus;
import net.engio.mbassy.bus.common.PubSubPauseSupport;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.publication.SyncAsyncPostCommand;

public class SuspendableMBassador<T> extends AbstractSyncAsyncMessageBus<T, SyncAsyncPostCommand<T>> implements IMessageBus<T, SyncAsyncPostCommand<T>>, PubSubPauseSupport<T> {

    private final MBassador<T> internalBus;
    
    private final ConcurrentLinkedQueue<T> msgPauseQueue = new ConcurrentLinkedQueue<T>();

    private final AtomicBoolean paused = new AtomicBoolean();
    
    protected SuspendableMBassador(IBusConfiguration configuration) {
	super(configuration);
	
	this.internalBus = new MBassador<>(configuration);
    }


    public IMessagePublication publishAsync(final T message) {
	final IMessagePublication publication = createMessagePublication(message);
	if (isPaused()) {
	    enqueueMessageOnPause(message);
	    return publication;
	}
        return addAsynchronousPublication(publication);
    }

    public IMessagePublication publishAsync(final T message, final long timeout, final TimeUnit unit) {
	final IMessagePublication publication = createMessagePublication(message);
	if (isPaused()) {
	    enqueueMessageOnPause(message);
	    return publication;
	}
        return addAsynchronousPublication(publication, timeout, unit);
    }


    /**
     * Synchronously publish a message to all registered listeners (this includes listeners defined for super types)
     * The call blocks until every messageHandler has processed the message.
     *
     * @param message
     */
    @Override
    public void publish(final T message) {
	if (isPaused()) {
	    enqueueMessageOnPause(message);
	    return;
	}
        internalBus.publish(message);
    }

    @Override
    public void pause() {
	paused.set(true);
    }

    @Override
    public void resume() {
	if (!paused.get()) return;

	paused.set(false);
	while (!paused.get() && msgPauseQueue.size() > 0) {
	    publish (msgPauseQueue.poll());
	}
    }

    @Override
    public boolean isPaused() {
	return paused.get();
    }
    

    @Override
    public SyncAsyncPostCommand<T> post(T message) {
	return internalBus.post(message);
    }

    protected final void enqueueMessageOnPause(final T msg) {
	if (!isPaused()) return;
	msgPauseQueue.offer(msg);
    }
}
