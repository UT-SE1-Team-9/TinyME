package ir.ramtung.tinyme.domain.service;

import ir.ramtung.tinyme.domain.entity.Security;
import ir.ramtung.tinyme.messaging.EventPublisher;
import ir.ramtung.tinyme.messaging.event.SecurityStateChangedEvent;
import ir.ramtung.tinyme.messaging.request.ChangeMatchingStateRq;
import ir.ramtung.tinyme.messaging.request.MatchingState;
import ir.ramtung.tinyme.repository.SecurityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityConfigurationHandler {

    final OrderHandler orderHandler;
    final SecurityRepository securityRepository;
    final EventPublisher eventPublisher;

    public void handleMatchingStateRq(ChangeMatchingStateRq changeMatchingStateRq) {
        var security = this.securityRepository.findSecurityByIsin(changeMatchingStateRq.getSecurityIsin());
        var targetMatchingState = changeMatchingStateRq.getTargetState();
        changeSecurityState(security, changeMatchingStateRq);
        eventPublisher.publish(new SecurityStateChangedEvent(
                security.getIsin(),
                targetMatchingState
        ));
    }

    private void changeSecurityState(Security security, ChangeMatchingStateRq changeMatchingStateRq) {
        var targetMatchingState = changeMatchingStateRq.getTargetState();
        var prevState = security.getMatchingState();

        if (prevState == MatchingState.AUCTION) {
            orderHandler.handleAuctionOpening(changeMatchingStateRq); // incubating decision: even though this decision was made by the executives, but it's not the best possible way to initiate an auction opening
        }

        security.setMatchingState(targetMatchingState);
    }
}
