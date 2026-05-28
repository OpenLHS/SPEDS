
package ca.griis.speds.session.internal.handler.indication.initiator;

import ca.griis.speds.session.internal.domain.ExpandedSidu;

public interface InitiatorEvent {
  void notifyPubRec(ExpandedSidu idu);

  void notifySakRec(ExpandedSidu idu);

  void notifyCleRec(ExpandedSidu idu);

  void notifyMsgRec(ExpandedSidu idu);

  void notifyFinRec(ExpandedSidu idu);
}
