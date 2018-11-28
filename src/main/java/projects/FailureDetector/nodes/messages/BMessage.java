package projects.election.nodes.messages;

import sinalgo.nodes.messages.Message;
import projects.election.enums.MessageType;
import  projects.election.nodes.nodeImplementations.BNode;

public class BMessage extends Message {
  public long id;
  public long seqNumber;
  public MessageType type;
  public BNode sender;

  public BMessage(long id, MessageType type, long seqNumber, BNode sender) {
    this.id = id;
    this.seqNumber = seqNumber;
    this.type = type;
    this.sender = sender;
  }

  public Message clone() {
    return new BMessage(id, type, seqNumber, sender);
  }

  public long getId() {
    return this.id;
  }


  public void setId(long id) {
    this.id = id;
  }

  public long getSeqNumber(){
    return this.seqNumber;
  }

  public BNode getSender(){
    return this.sender;
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
