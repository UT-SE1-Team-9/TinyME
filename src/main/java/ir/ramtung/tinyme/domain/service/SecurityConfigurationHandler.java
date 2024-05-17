package ir.ramtung.tinyme.domain.service;

import ir.ramtung.tinyme.domain.service.matcher.AuctionMatcher;
import ir.ramtung.tinyme.domain.service.matcher.ContinuousMatcher;
import ir.ramtung.tinyme.messaging.EventPublisher;
import ir.ramtung.tinyme.messaging.event.SecurityStateChangedEvent;
import ir.ramtung.tinyme.messaging.request.ChangeMatchingStateRq;
import ir.ramtung.tinyme.repository.SecurityRepository;
import org.springframework.stereotype.Service;

@Service
public class SecurityConfigurationHandler {

    SecurityRepository securityRepository;
    EventPublisher eventPublisher;
    ContinuousMatcher continuousMatcher;
    AuctionMatcher auctionMatcher;

    public SecurityConfigurationHandler(SecurityRepository securityRepository, EventPublisher eventPublisher, ContinuousMatcher continuousMatcher, AuctionMatcher auctionMatcher) {
        this.securityRepository = securityRepository;
        this.eventPublisher = eventPublisher;
        this.continuousMatcher = continuousMatcher;
        this.auctionMatcher = auctionMatcher;
    }

    public void handleMatchingStateRq(ChangeMatchingStateRq changeMatchingStateRq) {
        var security = this.securityRepository.findSecurityByIsin(changeMatchingStateRq.getSecurityIsin());
        var targetMatchingState = changeMatchingStateRq.getTargetState();
        security.setMatcher(switch (targetMatchingState) {
            case CONTINUOUS -> continuousMatcher;
            case AUCTION -> auctionMatcher;
        });
        eventPublisher.publish(new SecurityStateChangedEvent(
                security.getIsin(),
                targetMatchingState
        ));
    }
}
