package net.engio.mbassy.bus.publication;

import java.util.concurrent.TimeUnit;

import net.engio.mbassy.bus.IMessagePublication;
import net.engio.mbassy.bus.SuspendableMBassador;

public class SyncAsyncPausePostCommand <T> implements ISyncAsyncPublicationCommand {

    private final T message;
    private final SuspendableMBassador<T> mBassador;

    public SyncAsyncPausePostCommand(final SuspendableMBassador<T> mBassador, final T message) {
        this.mBassador = mBassador;
        this.message = message;
    }

    @Override
    public void now() {
        mBassador.publish(message);
    }

    @Override
    public IMessagePublication asynchronously() {
        return mBassador.publishAsync(message);
    }

    @Override
    public IMessagePublication asynchronously(final long timeout, final TimeUnit unit) {
        return mBassador.publishAsync(message, timeout, unit);
    }
}
