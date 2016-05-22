import controlP5.*;

// Editable Numberbox for ControlP5
PImage img;
ControlP5 cp5;
String[] name={"dummy","dummy","shoes","shirts", "bags"};
String[] price={"1","2","3","4","5"};
String[] quantity={"1","2","3","4","5"};;
String[] description={"dummy","dummy","nike","hanes","prada"};
int index=0;
String[] lines;

void setup() {
  size(400, 550);
  cp5 = new ControlP5(this);
PFont font = createFont("arial",10);
img = loadImage("soek.png");
lines = loadStrings("db.json");

  Numberbox n = cp5.addNumberbox("beacon")
                   .setSize(100, 20)
                   .setPosition(50, 180)
                   .setValue(0)
                   ;
                   
  makeEditable( n );  
      
      cp5.addTextfield("name")
     .setPosition(50,230)
     .setSize(200,30)
     .setFont(font)
     .setAutoClear(true)
     ;
                 
  cp5.addTextfield("price")
     .setPosition(50,280)
     .setSize(200,30)
     .setFont(font)
     .setAutoClear(true)
     ;
     
      cp5.addTextfield("quantity")
     .setPosition(50,350)
     .setSize(200,30)
     .setFont(font)
     .setAutoClear(true)
      ;
                 
  cp5.addTextfield("description")
     .setPosition(50,410)
     .setSize(300,30)
     .setFont(font)
     .setAutoClear(true)
     ;
    
     cp5.addBang("clear")
     .setPosition(260,170)
     .setSize(80,40)
     .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER)
     ;    
      cp5.addBang("SET")
     .setPosition(50,480)
     .setSize(80,40)
     .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER)
     ;    
      cp5.addBang("READ")
     .setPosition(250,480)
     .setSize(80,40)
     .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER)
     ;    
}


void draw() {
  background(0);
  PFont font2 = createFont("arial",15);
  //font = createFont("LetterGothicStd.ttf", 32);
  textFont(font2);
  text("ZOEK geoShopper Updater v1.0", 120,70);
  image(img, 10,10);
  
 // text("\"id\":"+str(index)+",", 120,100); for debugging
}

public void clear() {
  cp5.get(Textfield.class,"name").clear();
  cp5.get(Textfield.class,"price").clear();
  cp5.get(Textfield.class,"quantity").clear();
  cp5.get(Textfield.class,"description").clear();
}
public void SET() {
  name[index]=cp5.get(Textfield.class,"name").getText();
  price[index]=cp5.get(Textfield.class,"price").getText();
  quantity[index]=cp5.get(Textfield.class,"quantity").getText();
  description[index]=cp5.get(Textfield.class,"description").getText();
  String[] m1;
  int i;
  for ( i = 0; i < lines.length; i++) {
     m1 = match(lines[i], "\"id\": "+str(index));
if (m1 != null) {  // If not null, then a match was found
  // This will print to the console, since a match was found.
  println("Found a match in '" ); 
  break;
} else {
  println("No match found in'");
  }
  }
  lines[i+2]="\"name\": \""+name[index]+"\",";
  lines[i+3]="\"price\": "+price[index]+",";
  lines[i+4]="\"quantity\": "+quantity[index]+",";
  lines[i+5]="\"desc\": \""+description[index]+"\"";
  //text(lines[i+3],120,90); for debugging
  saveStrings("db.json", lines); // used to save to the jason file
}
public void READ() {
  cp5.get(Textfield.class,"name").setText(name[index]);
  cp5.get(Textfield.class,"price").setText(price[index]);
  cp5.get(Textfield.class,"quantity").setText(quantity[index]);
  cp5.get(Textfield.class,"description").setText(description[index]);
}

// function that will be called when controller 'numbers' changes
public void beacon(float f) {
  println("received "+f+" from Numberbox numbers ");
  index=int(f);
}
void controlEvent(ControlEvent theEvent) {
  if(theEvent.isAssignableFrom(Textfield.class)) {
    println("controlEvent: accessing a string from controller '"
            +theEvent.getName()+"': "
            +theEvent.getStringValue()
            );
  }
}


void makeEditable( Numberbox n ) {
  // allows the user to click a numberbox and type in a number which is confirmed with RETURN
  
   
  final NumberboxInput nin = new NumberboxInput( n ); // custom input handler for the numberbox
  
  // control the active-status of the input handler when releasing the mouse button inside 
  // the numberbox. deactivate input handler when mouse leaves.
  n.onClick(new CallbackListener() {
    public void controlEvent(CallbackEvent theEvent) {
      nin.setActive( true ); 
    }
  }
  ).onLeave(new CallbackListener() {
    public void controlEvent(CallbackEvent theEvent) {
      nin.setActive( false ); nin.submit();
    }
  });
}



// input handler for a Numberbox that allows the user to 
// key in numbers with the keyboard to change the value of the numberbox

public class NumberboxInput {

  String text = "";

  Numberbox n;

  boolean active;

  
  NumberboxInput(Numberbox theNumberbox) {
    n = theNumberbox;
    registerMethod("keyEvent", this );
  }

  public void keyEvent(KeyEvent k) {
    // only process key event if input is active 
    if (k.getAction()==KeyEvent.PRESS && active) {
      if (k.getKey()=='\n') { // confirm input with enter
        submit();
        return;
      } else if(k.getKeyCode()==BACKSPACE) { 
        text = text.isEmpty() ? "":text.substring(0, text.length()-1);
        //text = ""; // clear all text with backspace
      }
      else if(k.getKey()<255) {
        // check if the input is a valid (decimal) number
        final String regex = "\\d+([.]\\d{0,2})?";
        String s = text + k.getKey();
        if ( java.util.regex.Pattern.matches(regex, s ) ) {
          text += k.getKey();
        }
      }
      n.getValueLabel().setText(this.text);
    }
  }

  public void setActive(boolean b) {
    active = b;
    if(active) {
      n.getValueLabel().setText("");
      text = ""; 
    }
  }
  
  public void submit() {
    if (!text.isEmpty()) {
      n.setValue( float( text ) );
      text = "";
    } 
    else {
      n.getValueLabel().setText(""+n.getValue());
    }
  }
}