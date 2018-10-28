package projects.election.nodes.messages;

import sinalgo.nodes.messages.Message;
import projects.election.enums.MessageType;
import  projects.election.nodes.nodeImplementations.BNode;


/* Description of Message Type */
public class BMessage extends Message {
  public long id;
  public boolean answer;
  public MessageType type;
  public boolean isOK;
  public BNode sender;

  public BMessage(long id, MessageType type,BNode sender) {
    this.id = id;
    this.sender = sender;
    this.type = type;
  }

  public BMessage(long id, MessageType type, boolean isOK) {
    this.id = id;
    this.sender = sender;
    this.type = type;
    this.isOK = isOK;
  }

  public boolean getisOK(){
    return this.isOK;
  }

  public Message clone() {
    return new projects.election.nodes.messages.BMessage(id, type, sender);
  }

  public long getId() {
    return this.id;
  }

  public BNode getSender(){
    return this.sender;
  }

  public void setId(long id) {
    this.id = id;
  }

  public MessageType getType() {
    return this.type;
  }

  public void setType(MessageType type) {
    this.type = type;
  }
}
