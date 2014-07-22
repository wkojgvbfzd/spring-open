package net.onrc.onos.core.matchaction;

import net.onrc.onos.api.batchoperation.BatchOperationTargetId;

public class MatchActionId extends BatchOperationTargetId {
    private final String value;

    public MatchActionId(String id) {
        value = id;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MatchActionId) {
            MatchActionId other = (MatchActionId) obj;
            return (this.value.equals(other.value));
        }
        return false;
    }

}