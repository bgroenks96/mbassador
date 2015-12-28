package net.engio.mbassy.bus;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import net.engio.mbassy.bus.common.IMessageBus;
import net.engio.mbassy.bus.common.PubSubPauseSupport;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.publication.SyncAsyncPausePostCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuspendableMBassador<T> implements IMessageBus<T, SyncAsyncPausePostCommand<T>>, PubSubPauseSupport<T> {

    private static final Logger log = LoggerFactory.getLogger(SuspendableMBassador.class);
    private final MBassador<T> internalBus;
    private final ConcurrentLinkedQueue<T> msgPauseQueue = new ConcurrentLinkedQueue<T>();
    private final AtomicBoolean paused = new AtomicBoolean();

    public SuspendableMBassador(final MBassador<T> internalBus) {
	this.internalBus = internalBus;
    }

    public SuspendableMBassador(final IBusConfiguration configuration) {
	this(new MBassador<T>(configuration));
    }

    public IMessagePublication publishAsync(final T message) {
	final IMessagePublication publication = internalBus.createMessagePublication(message);
	if (isPaused()) {
	    enqueueMessageOnPause(message);
	    return publication;
	}
        return internalBus.addAsynchronousPublication(publication);
    }

    public IMessagePublication publishAsync(final T message, final long timeout, final TimeUnit unit) {
	final IMessagePublication publication = internalBus.createMessagePublication(message);
	if (isPaused()) {
	    enqueueMessageOnPause(message);
	    return publication;
	}
        return internalBus.addAsynchronousPublication(publication, timeout, unit);
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
	log.info("Pausing event publication [{} messages in queue].", msgPauseQueue.size());
	paused.set(true);
    }

    @Override
    public void resume() {
	if (!paused.get()) return;

	log.info("Resuming event publication [{} messages in queue].", msgPauseQueue.size());
	paused.set(false);
	while (!paused.get() && msgPauseQueue.size() > 0) {
	    publish (msgPauseQueue.poll());
	}
    }

    @Override
    public void resumeAsync() {
	if (!paused.get()) return;

	paused.set(false);
	while (!paused.get() && msgPauseQueue.size() > 0) {
	    publishAsync (msgPauseQueue.poll());
	}
    }

    @Override
    public boolean isPaused() {
	return paused.get();
    }


    @Override
    public SyncAsyncPausePostCommand<T> post(final T message) {
	return new SyncAsyncPausePostCommand <T> (this, message);
    }

    @Override
    public Collection<IPublicationErrorHandler> getRegisteredErrorHandlers() {
	return internalBus.getRegisteredErrorHandlers();
    }

    @Override
    public boolean unsubscribe(final Object listener) {
	return internalBus.unsubscribe(listener);
    }

    @Override
    public void subscribe(final Object listener) {
	internalBus.subscribe(listener);
    }

    @Override
    public BusRuntime getRuntime() {
	return internalBus.getRuntime();
    }

    @Override
    public String toString() {
	return internalBus.toString() + " | paused=" + paused.get();
    }

    @Override
    public void shutdown() {
	internalBus.shutdown();
    }

    @Override
    public boolean hasPendingMessages() {
	return internalBus.hasPendingMessages();
    }

    protected final void enqueueMessageOnPause(final T msg) {
	if (!isPaused()) return;
	msgPauseQueue.offer(msg);
    }
}
