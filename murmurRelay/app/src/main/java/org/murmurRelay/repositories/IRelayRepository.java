package org.murmurRelay.repositories;

import org.murmurRelay.domains.Relay;

public interface IRelayRepository {
    Relay getRelay();
    void writeRelay(Relay relay);
}
