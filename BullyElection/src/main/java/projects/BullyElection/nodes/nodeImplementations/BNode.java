package projects.election.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.*;

import projects.defaultProject.nodes.timers.MessageTimer;
import sinalgo.tools.Tools;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import projects.election.enums.MessageType;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.Connections;
import sinalgo.nodes.edges.Edge;
import projects.election.nodes.nodeImplementations.BNode;
import projects.election.nodes.messages.BMessage;
import projects.election.nodes.timers.BTimer;

public class BNode extends Node {

  private Color color = Color.BLUE;
  private long elected;
  private  BNode successor;
  private int max=0;
  private ArrayList< BNode> greaters = new ArrayList();

  public BNode() {
    super();
  }

  @Override
  public void handleMessages(Inbox inbox) {
    for (Message msg : inbox) {
      if (msg instanceof BMessage) {
        BMessage emsg = (BMessage) msg;
        switch (emsg.getType()) {
          case ELECTION:
            ElectionMessage(emsg);
            break;

          case COORDENATOR:
            CoordMessage(emsg);
            break;

          case ANSWER:
            AnswerMessage(emsg);
            break;

          default:
            auxCoord(emsg);
            break;
        }
      }
    }
  }

  public void AnswerMessage(BMessage msg){
    if(msg.getId() > this.getID()){
      System.out.println(String.format("%s Recebendo Resposta de %s", this, msg.getId()));
      this.max++;
    }

    if(this.max == this.getGreaters().size()){
        startE(this.getGreaters().get(0));
        this.getGreaters().get(0).color = Color.RED;
    }
  }

  public void auxCoord(BMessage msg){
    BMessage coordMSG = new BMessage(this.getID(), MessageType.COORDENATOR, this);
    System.out.println(String.format("%d sending msg COORD para todos", this.getID()));
    this.color = Color.GREEN;
    broadcast(coordMSG);
  }


  public void ElectionMessage(BMessage msg) {

    if (msg.getId() < this.getID()) {
      BMessage answerMSG = new BMessage(this.getID(), MessageType.ANSWER, this);
      send(answerMSG, msg.getSender());
      this.color = Color.BLACK;
    }
  }

  public void startE( BNode node){
    int aux =0;
      for( BNode gt: node.getGreaters()){
        BMessage electionMSG = new BMessage(node.getID(), MessageType.ELECTION, node);
        System.out.println(String.format("Node %d sending to node %d.", node.getID(), gt.getID()));
        send(electionMSG, gt);
        aux++;
      }
    if(aux ==0){
        BMessage coordMSG = new BMessage(node.getID(), MessageType.COORD2, node);
        send(coordMSG, node);
    }
  }

  public void CoordMessage(BMessage msg) {
    this.elected = msg.getId();
    System.out.println(String.format("%s definindo %s como LIDER", this, msg.getId()));
  }

  public String toString() {
    return "Node " + this.getID();
  }

  @Override
  public void neighborhoodChange() {

    Connections nodeConnections = this.getOutgoingConnections();
     BNode firstConnectionNode = ( BNode) nodeConnections.iterator().next().getEndNode();
    successor = null;
    int i = 0;
    for (Edge edge : nodeConnections) {
       BNode endNode = ( BNode) edge.getEndNode();
      if (endNode.getID() > this.getID()) {
        this.greaters.add(endNode);
      }
    }
    if(!this.greaters.isEmpty())
    System.out.println(String.format("%s", this.greaters.get(0)));

    if (successor == null) {
      successor = firstConnectionNode;
      for (Edge edge : nodeConnections) {
         BNode endNode = ( BNode) edge.getEndNode();
        successor = endNode.compareTo(successor) < 0 ? endNode : successor;
      }
    }
  }

  public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
    String text = "" + this.getID();
    Color textColor = Color.WHITE;

    this.setColor(this.color);
    super.drawNodeAsDiskWithText(g, pt, highlight, text, 35, textColor);
  }

  public ArrayList< BNode> getGreaters(){
    return this.greaters;
  }

  @NodePopupMethod(menuText = "Start Election")
  public void startElection() {
    BMessage msg = new BMessage(this.getID(), MessageType.ELECTION,this);
    BTimer timer = new projects.election.nodes.timers.BTimer(this, successor, 1);
    this.getGreaters().get(0).color = Color.RED;
    timer.startRelative(1, this);
    Tools.appendToOutput(String.format("Start Routing from %s \n", this));
  }

  public void preStep() {
  }

  public void init() {
  }

  public void postStep() {
  }

  public void checkRequirements() {
  }

  public void compute() {
  }
}
