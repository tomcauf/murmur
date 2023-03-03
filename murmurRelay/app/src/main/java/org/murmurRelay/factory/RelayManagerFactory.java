package org.murmurRelay.factory;

import org.murmurRelay.domains.Relay;
import org.murmurRelay.grammar.Protocol;
import org.murmurRelay.handler.RelayManager;
import org.murmurRelay.repositories.IRelayRepository;
import org.murmurRelay.utils.NetChooser;

public class RelayManagerFactory {
    private final IRelayRepository repository;
    private final NetChooser netChooser;
    private final Protocol protocol;

    public RelayManagerFactory(IRelayRepository repository, NetChooser netChooser, Protocol protocol) {
        this.repository = repository;
        this.netChooser = netChooser;
        this.protocol = protocol;
    }
    public RelayManager createAndGetRelayManager() {
        return new RelayManager(repository,netChooser,protocol);
    }
}
