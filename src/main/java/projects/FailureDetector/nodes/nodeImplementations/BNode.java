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

public class BNode extends Node {

  private Color color = Color.BLUE;
  private BNode successor;
  private boolean sendMessage = true;
  private long roundNumber = 0;
  private long hbNumber = 0;
  private HashMap<BNode, Long> hbTable = new HashMap<BNode, Long>();

  public BNode() {
    super();
  }

  @Override
  public void handleMessages(Inbox inbox) {
    for (Message msg : inbox) {
      if (msg instanceof BMessage) {
        BMessage emsg = (BMessage) msg;
        switch (emsg.getType()) {
          case HEARTBEAT:
            updateTable(emsg);
            break;

          default:
            break;
        }
      }
    }
  }

  public void updateTable(BMessage msg) {
    if(this.sendMessage){
      hbTable.put(msg.getSender(), roundNumber);
      HashMap<BNode, Long> hbTableAux = new HashMap<BNode, Long>(this.hbTable);
      Iterator it = hbTableAux.entrySet().iterator();

      while(it.hasNext()) {
        Map.Entry pair = (Map.Entry)it.next();
        BNode node = (BNode)pair.getKey();
        long heartbeatNumber = (long)pair.getValue();
        if(this.roundNumber - heartbeatNumber > 1) { // 1 rodada, na proxima ja da falha
          System.out.println(this + " : " + node+ " FALHOU :/");
          node.color = color.RED;
          this.hbTable.remove(node);
        }
      }
    }
  }


  @Override
  public void neighborhoodChange() {
  }

  public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
    String text = "" + this.getID();
    Color textColor = Color.WHITE;

    this.setColor(this.color);
    super.drawNodeAsDiskWithText(g, pt, highlight, text, 35, textColor);
  }


  @NodePopupMethod(menuText = "Nó apresenta Falhas") // setar que vai falhar
  public void startElection() {
    this.sendMessage = false;
    this.color = Color.ORANGE;
  }

  public void preStep() { // atualiza seqNumber
    if(this.sendMessage){
      this.hbNumber++;
      this.roundNumber++;
      BMessage broadcastMessage = new BMessage(this.getID(), MessageType.HEARTBEAT, this.hbNumber, this);
      broadcast(broadcastMessage);
    }
  }

  public void init() {
  }

  public HashMap<BNode, Long> getHT(){
    return this.hbTable;
    }

  public void postStep() {// manda pra todos
    if(this.sendMessage){
      Iterator it = this.hbTable.entrySet().iterator();
      String aux = "{";

      while(it.hasNext()) {
        Map.Entry pair = (Map.Entry)it.next();
        BNode node = (BNode)pair.getKey();
        long heartbeatNumber = (long)pair.getValue();
          aux+= (node + " = "+node.getHbNumber().toString()+", ");
      }
      aux+= "}";
      System.out.println(aux);
    }
  }

  public void checkRequirements() {
  }

  public void compute() {
  }

  public Long getHbNumber(){
    return this.hbNumber;
  }

  public String toString() {
    return "Node " + this.getID();
  }

}
