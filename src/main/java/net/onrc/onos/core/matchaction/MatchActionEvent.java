package net.onrc.onos.core.matchaction;

/**
 * A MatchAction-related event.
 *
 * The TODO entry removed by switch or preempted by another request.
 *
 */
public interface MatchActionEvent {

    /**
     * TODO.
     *
     * @return Match Action ID (This is a WIP).
     */
    public MatchActionId getId();

    /**
     * TODO.
     *
     * @return Match Action Status (This is a WIP).
     */
    public MatchActionStatus getStatus();

    /**
     * Returns the time at which the event was created.
     *
     * @return the time in milliseconds since start of epoch
     */
    long getTime();
}
