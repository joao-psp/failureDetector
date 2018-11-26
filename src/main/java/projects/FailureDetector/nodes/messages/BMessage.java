package projects.election.nodes.messages;

import sinalgo.nodes.messages.Message;
import projects.election.enums.MessageType;
import  projects.election.nodes.nodeImplementations.BNode;


/* Description of Message Type */
public class BMessage extends Message {
  public long id;
  public long seqNumber;
  public MessageType type;

  public BMessage(long id, MessageType type, long seqNumber) {
    this.id = id;
    this.seqNumber = seqNumber;
    this.type = type;
  }

  public Message clone() {
    return new projects.election.nodes.messages.BMessage(id, type, seqNumber);
  }

  public long getId() {
    return this.id;
  }

  // public BNode getSender(){
  //   return this.sender;
  // }

  public void setId(long id) {
    this.id = id;
  }

  public long getSeqNumber(){
    return this.seqNumber;
  }

  public void setSeqNumber(long seqNumber){
    this.seqNumber = seqNumber;
  }

  public MessageType getType() {
    return this.type;
  }

  public void setType(MessageType type) {
    this.type = type;
  }
}
