package projects.election.nodes.timers;

import projects.election.enums.MessageType;
import sinalgo.nodes.timers.Timer;
import projects.election.nodes.nodeImplementations.BNode;
import projects.election.nodes.messages.BMessage;
/* Description of Timer */
public class BTimer extends Timer {
  BNode sender, receiver;
  int interval;

  public BTimer(BNode sender,BNode receiver, int interval) {
    this.sender = sender;
    this.receiver = receiver;
    this.interval = interval;
  }

  /**
   * This function execute when called timer.startRelative(x, node), after x rounds
   */
  public void fire() {

    for(BNode gt: sender.getGreaters()){
      BMessage msg = new BMessage(sender.getID(), MessageType.ELECTION, sender);
      System.out.println(String.format("Node %d sending to node %d.", sender.getID(), gt.getID()));
      sender.send(msg, gt);
    }
  }
}
