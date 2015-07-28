package net.engio.mbassy.bus.common;

public interface PubSubPauseSupport<T> extends PubSubSupport<T> {

    /**
     * Pauses event publishing. All messages submitted via {@link #publish(Object)} will be stored in a queue until
     * {@link #resume()} is called. Any subsequent calls to this method before a call to {@link #resume()} will have no
     * effect.
     */
    void pause();

    /**
     * Resumes event publishing. All messages enqueued since the first call to {@link #pause()} will be subsequently
     * flushed and published in the order that they arrived. Does nothing if the runtime is not currently in a paused
     * state from a call to {@link #pause()}.
     */
    void resume();

    /**
     * @return true if this PubSubPauseSupport is currently paused, false otherwise.
     */
    boolean isPaused();
}
