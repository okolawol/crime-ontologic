import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import controlP5.*; 

import controlP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Crime_Ontologic extends PApplet {

CircleCloudPlotter cloudPlotter;
ControlPanel controlPanel;
public void setup(){
  size(1400,800);
  smooth();
  background(255);
  
  cloudPlotter = new CircleCloudPlotter(width/2,height/2);
  controlPanel = new ControlPanel(250,50,100,100,"BOTTOM");
  controlP5();
  loadFile();
}
public void draw(){
  background(0);
  cloudPlotter.draw();
  controlPanel.run();
}
public void loadFile(){
  
  String[] lines = loadStrings("crimesInCarlifornia.csv");
  for(int i=0; i<lines.length; i++){
    String[] in = split(lines[i],",");
    if(i!=0){
      //println("STATE: "+in[0]+" TYPE OF CRIME: "+in[1]+" CRIME: "+in[2]+" YEAR: "+in[3]+" COUNT: "+in[4]);
      cloudPlotter.fillPoints(in[1],in[2],in[3],in[4]);
    }
  }
  
}

public void mouseClicked(){
  cloudPlotter.mouseClick();
  controlPanel.mouseClick();
}

public void keyPressed(){
  //space bar
  if(keyCode == 32){
    cloudPlotter.freezePoints(true);
  }
  //shift key
  if(keyCode == 16){
    cloudPlotter.repulse(true);
  }
}
public void keyReleased(){
  if(keyCode == 32){
    cloudPlotter.freezePoints(false);
  }
  if(keyCode == 16){
    cloudPlotter.repulse(false);
  }
}

class CircleCloudPlotter{
  int filterYear = 2004;
  boolean dragging = false;
  CloudPoint dragPoint;
  
  //position of the graph
  PVector position;
  
  //stores all the points and anchors
  ArrayList<CloudPoint> cloudPoints = new ArrayList<CloudPoint>();
  
  //this is used for storing all the points
  CloudPoint centerPoint;
  
  //this is used for pointing to the
  //anchor obects in memory so i can use
  //them later
  CloudPoint rootPoint;
  CloudPoint violentPoint;
  CloudPoint propertyPoint;
  CloudPoint murderPoint;
  CloudPoint rapePoint;
  CloudPoint robberyPoint;
  CloudPoint assaultPoint;
  CloudPoint burglaryPoint;
  CloudPoint larcenyPoint;
  CloudPoint motorPoint;
  CloudPoint beforeYearPoint;
  CloudPoint afterYearPoint;
  
  
  //stores all the counts
  int totalCounts = 0;
  
  
  public CircleCloudPlotter(int xPos,int yPos){
    position = new PVector(xPos,yPos);
    
    //Initiatinga all the anchor points
    rootPoint = new RootPoint(0,0,color(255,0,255),200);
    cloudPoints.add(rootPoint);
    
    //MAIN 2
    centerPoint = new PropertyPoint(0,0,color(255,0,0),100,"Violent Crime");
    centerPoint.target = new PVector(width/4,0);
    cloudPoints.add(centerPoint);
    violentPoint = centerPoint;
    
    centerPoint = new PropertyPoint(0,0,color(0,255,127),100,"Property Crime");
    centerPoint.target = new PVector(-width/4,0);
    cloudPoints.add(centerPoint);
    propertyPoint = centerPoint;
    
    //SUBCATEGORIES
    centerPoint = new DetailPoint(0,0,color(238,238,0),50,"Murder and nonnegligent Manslaughter");
    centerPoint.target = new PVector(width*3/8,-height*3/8);
    cloudPoints.add(centerPoint);
    murderPoint = centerPoint;
    
    centerPoint = new DetailPoint(0,0,color(139,101,8),50,"Forcible rape");
    centerPoint.target = new PVector(width*3/8,-height/6);
    cloudPoints.add(centerPoint);
    rapePoint = centerPoint;
    
    centerPoint = new DetailPoint(0,0,color(255,140,0),50,"Robbery");
    centerPoint.target = new PVector(width*3/8,height/6);
    cloudPoints.add(centerPoint);
    robberyPoint = centerPoint;
    
    centerPoint = new DetailPoint(0,0,color(255,97,3),50,"Aggravated assault");
    centerPoint.target = new PVector(width*3/8,height*3/8);
    cloudPoints.add(centerPoint);
    assaultPoint = centerPoint;
    //
    
    //SUBCATEGORIES 2
    centerPoint = new DetailPoint(0,0,color(113,113,198),50,"Burglary");
    centerPoint.target = new PVector(-width*3/8,-height/4);
    cloudPoints.add(centerPoint);
    burglaryPoint = centerPoint;
    
    centerPoint = new DetailPoint(0,0,color(56,142,142),50,"Larceny-theft");
    centerPoint.target = new PVector(-width*3/8,0);
    cloudPoints.add(centerPoint);
    larcenyPoint = centerPoint;
    
    centerPoint = new DetailPoint(0,0,color(142,56,142),50,"Motor vehicle theft");
    centerPoint.target = new PVector(-width*3/8,height/4);
    cloudPoints.add(centerPoint);
    motorPoint = centerPoint;
    
    
    //FilteredDatePoint
    centerPoint = new FilteredDatePoint(0,0,color(255),50,"","Before "+filterYear);//2000");
    centerPoint.target = new PVector(-width/15,-height/4);
    centerPoint.cYear = filterYear;//2000;
    cloudPoints.add(centerPoint);
    beforeYearPoint = centerPoint;
    
    centerPoint = new FilteredDatePoint(0,0,color(255),50, "", filterYear+" or after");//2000");
    centerPoint.target = new PVector(width/15,-height/4);
    centerPoint.cYear = filterYear;//2000
    cloudPoints.add(centerPoint);
    afterYearPoint = centerPoint;
  }
  
  public void draw(){
    pushMatrix();
    translate(position.x,position.y);
    
    //drawing all connection lines between anchors
    drawLines();
    beforeYearPoint.avgIncrease();
    afterYearPoint.avgIncrease();
    
    //loop through all points and anchors
    for(CloudPoint p : cloudPoints){
      
      //check collision between anchors and points
      for(int i = cloudPoints.indexOf(p)+1;i<cloudPoints.size();i++){
        CloudPoint p2 = cloudPoints.get(i);
        collisionCheck(p,p2);
      }
      
      //anchors follow a point on the screen
      //points follow anchors
      p.follow();
      
      //updating the scale of all points
      p.updateScale();
      
      p.draw();
      
      //checking for mouse over
      mouseOver(p);
      
      if(!mousePressed){dragging = false; }

    }//for p
    
     if(dragging){
           dragPoint.position.x = mouseX-position.x;
           dragPoint.position.y = mouseY-position.y;
      }
    popMatrix();
  }
  
  //draw lines between anchor points
  private void drawLines(){
    stroke(255);
    strokeWeight(1);
    line(rootPoint.position.x,rootPoint.position.y,violentPoint.position.x,violentPoint.position.y);
    line(rootPoint.position.x,rootPoint.position.y,propertyPoint.position.x,propertyPoint.position.y);
    
    line(violentPoint.position.x,violentPoint.position.y,murderPoint.position.x,murderPoint.position.y);
    line(violentPoint.position.x,violentPoint.position.y,rapePoint.position.x,rapePoint.position.y);
    line(violentPoint.position.x,violentPoint.position.y,robberyPoint.position.x,robberyPoint.position.y);
    line(violentPoint.position.x,violentPoint.position.y,assaultPoint.position.x,assaultPoint.position.y);
    
    line(propertyPoint.position.x,propertyPoint.position.y,burglaryPoint.position.x,burglaryPoint.position.y);
    line(propertyPoint.position.x,propertyPoint.position.y,larcenyPoint.position.x,larcenyPoint.position.y);
    line(propertyPoint.position.x,propertyPoint.position.y,motorPoint.position.x,motorPoint.position.y);
  }
  
  //mouse over function
  private void mouseOver(CloudPoint p){
    
    //if mouse is over a point/anchor any one
    if(mouseX-position.x>p.position.x-p.spacialDiameter.x/2 && mouseX-position.x<p.position.x+p.spacialDiameter.x/2 && 
       mouseY-position.y>p.position.y-p.spacialDiameter.y/2 && mouseY-position.y<p.position.y+p.spacialDiameter.y/2){
         
         
         
         //if the mouse is pressed and p is an anchor
         //change its position to mouse position to enable dragging
         
         if(mousePressed&&p.isAnchor){
           dragging = true;
           dragPoint = p;
         }
         
         //if p is a normal point show the required text info
         //above the position of the anchor it is following
         if(p.isPoint){
           fill(p.colour);
           textSize(20);
           text("COUNT: "+p.count+" YEAR: "+p.cYear,p.anchor.position.x-100,p.anchor.position.y-60);
         }
         
         //if p is the root anchor show the required text info
         if(p.isRoot){
           fill(p.colour);
           textSize(20);
           text("TOTAL COUNTS: "+totalCounts,p.position.x-100,p.position.y-100);
           
         }
         
         //if p is not a detail/root/normal point but is an anchor i.e property or violent crime
         //show text info
         if(p.isAnchor&&!(p.isRoot)&&!(p.isDetail)&&!(p.isPoint)){
           fill(p.colour);
           textSize(20);
           text(""+p.typeOfCrime+"\n"+" total counts: "+p.count,p.position.x-150,p.position.y-100);
           
         }
         
         //if p is a detailed point/subcategory show text info
         if(p.isDetail && p.datePoint){
           fill(p.colour);
           textSize(20);
           text(""+p.crimeYear+"| increase= "+p.avg+"%\n"+" total counts: "+p.count,p.position.x-150,p.position.y-70);

         }
         else if(p.isDetail){
           fill(p.colour);
           textSize(20);
           text(""+p.crime+"\n"+" total counts: "+p.count,p.position.x-150,p.position.y-70);
         }
      

        
            if((mouseX-position.x > beforeYearPoint.position.x - beforeYearPoint.spacialDiameter.x/2
            && mouseX-position.x < beforeYearPoint.position.x + beforeYearPoint.spacialDiameter.x/2
            &&mouseY-position.y > beforeYearPoint.position.y - beforeYearPoint.spacialDiameter.y/2
            && mouseY-position.y < beforeYearPoint.position.y + beforeYearPoint.spacialDiameter.y/2) 
            ||
            (mouseX-position.x > afterYearPoint.position.x - afterYearPoint.spacialDiameter.x/2
            && mouseX-position.x < afterYearPoint.position.x + afterYearPoint.spacialDiameter.x/2
            &&mouseY-position.y > afterYearPoint.position.y - afterYearPoint.spacialDiameter.y/2
            && mouseY-position.y < afterYearPoint.position.y + afterYearPoint.spacialDiameter.y/2) 
            ){
            if(p.isDetail){// || p.isAnchor){
               beforeYearPoint.colour  = afterYearPoint.colour = p.colour;
               beforeYearPoint.crime = afterYearPoint.crime = p.crime;

             if(mousePressed){
                //if double clicked reset the crime to empty string
                if (mouseEvent.getClickCount()==2) {
                  beforeYearPoint.crime = afterYearPoint.crime = "";
                  beforeYearPoint.colour = afterYearPoint.colour = color(255);
                }
               
               
               beforeYearPoint.count = 0;
               beforeYearPoint.avg = 0;
               beforeYearPoint.totalCountsAdded = false;
               afterYearPoint.count = 0;
               afterYearPoint.avg = 0;
               afterYearPoint.totalCountsAdded = false;
             
              attachToRoot();
              
             }//end if mousePressed
            }
        }
     }
  }
  
  //function for mouse click
  public void mouseClick(){
    
    //loop through each point in the array
    for(CloudPoint p : cloudPoints){
      
      //check if the mouse is over it
      if(mouseX-position.x>p.position.x-p.spacialDiameter.x/2 && mouseX-position.x<p.position.x+p.spacialDiameter.x/2 && 
         mouseY-position.y>p.position.y-p.spacialDiameter.y/2 && mouseY-position.y<p.position.y+p.spacialDiameter.y/2){
           
           //only anchors can be clicked
           if(p.isAnchor){
             
             //loop throught the array again to find normal points
             //that have the same crime of the anchor
             for(CloudPoint p2 : cloudPoints){
               
               //when root anchor is clicked it attracts every point to it
               if(p.isRoot) {
                 if(p2.isPoint){
                   p2.placeAnchor(p);
                   p2.colour = color(255);
                 }
               }
                else if(p.datePoint){
                  if(p2.isPoint){
                 
                  if(((p.crime.equals(p2.crime)) || p.crime.equals(""))){
                    p2.colour = p.colour;
                    if(p2.cYear >= p.cYear){
                      //after
                      p2.placeAnchor(afterYearPoint);
                      afterYearPoint.count += p2.count;
                      afterYearPoint.datePoints.add(p2);
                    }
                    else{
                      //before
                      p2.placeAnchor(beforeYearPoint);
                      beforeYearPoint.count += p2.count;
                      beforeYearPoint.datePoints.add(p2);
                    }
                  }


                   
                 }//end if(p2.isPoint)
               }//end if(p.datePoint
                 
               //when any other anchor is clicked check other points that have the same
               //crime as it, attract them and change thier color
               else if(p2.isPoint){
                 if(p2.typeOfCrime.equals(p.typeOfCrime)||p2.crime.equals(p.crime)){
                   p2.placeAnchor(p);
                   p2.colour = p.colour;
                   //used for adding all the counts together ONLY ONCE
                   //which is why totalCountsAdded is used, it becomes true after and never
                   //runs again
                   if(!p.totalCountsAdded){
                     p.count = p.count + p2.count;
                   }
                 }
                 
               
                 
               }
             }
             p.totalCountsAdded = true;
           }
       }
    }
  }
  
  //freezes all points when space is pressed
  public void freezePoints(boolean freeze){
    if(freeze){
      for(CloudPoint p : cloudPoints){
        p.freeze = true;
      }
    }
    else {
      for(CloudPoint p : cloudPoints){
        p.freeze = false;
      }
    }
  }
  
  //spreads the points when shift is pressed
  public void repulse(boolean repulse){
    if(repulse){
      for(CloudPoint p : cloudPoints){
        p.repulse = true;
      }
    }
    else{
      for(CloudPoint p : cloudPoints){
        p.repulse = false;
      }
    }
  }
  
  public void attachToRoot(){
  //release all the clouds 
               for(CloudPoint p : cloudPoints){
                 if(p.isAnchor){
                   //loop throught the array again to find normal points
                   //that have the same crime of the anchor
                   for(CloudPoint p2 : cloudPoints){
                     //when root anchor is clicked it attracts every point to it
                     if(p.isRoot) {
                       if(p2.isPoint){
                         p2.placeAnchor(p);
                         p2.colour = color(255);
                       }//end if
                     }//end if
                   }//for
                  }//end if
                }//for
  }
  
  //collision checking between 2 points
  private void collisionCheck(CloudPoint c1,CloudPoint c2){
    if(dist(c1.position.x,c1.position.y,c2.position.x,c2.position.y)
      < c1.spacialDiameter.x/2+c2.spacialDiameter.x/2){
     
     float angle = atan2(c1.position.y-c2.position.y,c1.position.x-c2.position.x);
    
     float xAvg = 0.2f;
     float yAvg = 0.2f;
     
     //2 anchors cannot collide with each other
     if(!(c1.isAnchor&&c2.isAnchor)){
       
       //if c1 is not an anchor
       if(!c1.isAnchor){
         
         //if c1 is colliding with an anchor that it is not following
         //it gets pushed back
         if(!(c1.anchor.equals(c2))&&!(c2.isPoint)){
           xAvg = 10;
           yAvg = 10;
         }
         
         //if c1 and c2 are following different anchors they dont collide
         //but collide otherwise
         if((c2.isPoint&&c1.anchor.equals(c2.anchor))||(c1.isPoint&&c2.isAnchor)){
           c1.xVel = xAvg*cos(angle);
           c1.yVel = xAvg*sin(angle);
         }
       }
       
       //same as above but we are checking c2 instead of c1
       if(!c2.isAnchor){
         
         if(!(c2.anchor.equals(c1))&&!(c1.isPoint)){
           xAvg = 10;
           yAvg = 10;
         }
         
         if((c1.isPoint&&c2.anchor.equals(c1.anchor))||(c2.isPoint&&c1.isAnchor)){
           c2.xVel = yAvg*cos(angle-PI);
           c2.yVel = yAvg*sin(angle-PI);
         }
       }
     }
     
    }
  }
  
  //loads data from the file into the arraylist & turns one crime into a normal point
  public void fillPoints(String typeOfCrime,String crime,String cYear,String count){
    centerPoint = new CloudPoint(0,random(height,1000),color(255,255,255),5);
    centerPoint.typeOfCrime = typeOfCrime;
    centerPoint.crime = crime;
    
    int iYear = Integer.parseInt(cYear);
    centerPoint.cYear = iYear;
    
    int fCount = Integer.parseInt(count);
    centerPoint.count = fCount;
    
    //scale based on the counts of the crime
    float scaleGoal = map((float)centerPoint.count,605,986120,5,10);
    centerPoint.scaleGoal = scaleGoal;
    
    
    totalCounts = totalCounts + fCount;
    
    centerPoint.placeAnchor(rootPoint);
    cloudPoints.add(centerPoint);
  }
}
class CloudPoint{
  PVector position;
  int colour;
  PVector spacialDiameter;
  
  float xVel;
  float yVel;
  
  //scaling velocity for animating
  float scaleVel;
  float scaleGoal;
  
  PVector target;
  
  //used for freezing or repelling the points
  boolean freeze;
  boolean repulse;
  
  //check if this point is an anchor or not
  boolean isAnchor;
  //check if this point is a normal point or not
  boolean isPoint;
  //check if this point is root anchor or not
  boolean isRoot;
  //check if this point is a detail anchor or not
  boolean isDetail;
  
  boolean datePoint;
  //check if the total counts have been added
  //only used by children objects
  boolean totalCountsAdded;
  

  
  //stores the anchor that the point is
  //following
  CloudPoint anchor;
  ArrayList<CloudPoint> datePoints = new ArrayList<CloudPoint>();
  float avg=0;
  
  //csv crime properties
  String crimeYear="";
  String typeOfCrime="";
  String crime="";
  int cYear=0;
  int count=0;
  
  public CloudPoint(float xPos, float yPos, int c, float mScale){
    position = new PVector(xPos,yPos);
    colour = c;
    spacialDiameter = new PVector(10,10);
    scaleGoal = mScale;
     target = new PVector(0,0);
     freeze = false;
     repulse = false;
     isAnchor = false;
     isRoot = false;
     isDetail = false;
     isPoint = true;
     totalCountsAdded = false;
     datePoint = false;
     
  }
  
  public void draw(){
    pushMatrix();
    translate(position.x,position.y);
    noStroke();
    fill(colour);
    ellipse(0,0,spacialDiameter.x,spacialDiameter.y);
    popMatrix();
  }
  
  //the point always follows it's anchors position
  //if freeze(spacebar) or repulse(shift) isnt pressed
  
  //if you want a point to go anywhere just create an anchor(at the goal position you want the point to go to)
  //and add it to this object. you could create an extension of this class and overide the draw function not to
  //draw any circles if you dont want to show one.so that it can be invisible
  public void follow(){
    if(freeze){
      target.x = position.x;
      target.y = position.y;
    }
    else{
      target.x = anchor.position.x;
      target.y = anchor.position.y;
    }
    
    if(!repulse){
      xVel += (target.x-position.x)/width*1/2;
      yVel += (target.y-position.y)/height*1/2;
    }
    else{
      xVel += (target.x+position.x)/width*1/2;
      yVel += (target.y+position.y)/height*1/2;
    }
    
    xVel *= 0.98f;
    yVel *= 0.98f;
    
    position.x += xVel;
    position.y += yVel;
  }
  
  //animating scale
  public void updateScale(){
    scaleVel += (scaleGoal-spacialDiameter.x)/width*1/2;
    scaleVel *= 0.98f;
    spacialDiameter.x += scaleVel;
    spacialDiameter.y += scaleVel;
  }
  
  //used for placing the anchor that the point follows
  public void placeTarget(float xPos,float yPos){
    target.x = xPos;
    target.y = yPos;
  }
  public void placeAnchor(CloudPoint anchor){
    this.anchor = anchor;
    target.x = anchor.position.x;
    target.y = anchor.position.y;
  }
  public void avgIncrease(){
  }

}


PFont arial;
ControlP5 cp5;
Textfield textF;

public void controlP5(){
  arial = createFont("arial",20);
  cp5 = new ControlP5(this);
  
  cp5.addTextfield("YEARINPUT")
     .setSize(80,20)
     .setPosition((int)controlPanel.xPos+10, (int)controlPanel.yPos+20)
     .setFont(arial)
     .setFocus(false)
     .setColor(color(220))
     ;
     
  cp5.addBang("SEND")
     .setPosition((int)controlPanel.xPos+10, (int)controlPanel.yPos+60)
     .setSize(80,20)
     .getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER)
     ;   
     
  
}//controlP5

public void SEND() {
  // The code that might produce an error goes within the \u201ctry\u201d section.  
  try {
    int i = Integer.parseInt(cp5.get(Textfield.class,"YEARINPUT").getText());
    cloudPlotter.beforeYearPoint.cYear = cloudPlotter.afterYearPoint.cYear = cloudPlotter.filterYear =  i;
    cloudPlotter.beforeYearPoint.crimeYear = "Before "+cloudPlotter.filterYear;
    cloudPlotter.afterYearPoint.crimeYear = cloudPlotter.filterYear+" or after";
    cloudPlotter.attachToRoot();
  // The code that should happen if an error occurs goes in the \u201ccatch\u201d section.
  } catch (Exception e){
    println("Input is not valid");
  }//catch
}//SEND



class ControlPanel{
  
  //fields
  boolean visible, open;
  float finalXPos, finalYPos;//final destination
  float xPos, yPos;//temp position for animation
  float buttonSize;
  
  //var used for drawing
  float panelX, panelY, panelW, panelH; 
  float buttonX, buttonY, buttonW, buttonH; 
  int panelColor, buttonColor; 
    int mouseOffColor, mouseOnColor; 
  
  //state
  String buttonPos;//UP, DOWN, LEFT, RIGHT
  
  //constructor
    ControlPanel(float x, float y, float w, float h, String pos){

      finalXPos = xPos = panelX = x;
      finalYPos = yPos = panelY = y;
      panelW = w; 
      panelH = h;
      buttonPos = pos;
      buttonSize = 15;

      mouseOffColor = color(200, 50, 110, 180);
      mouseOnColor = color(255, 105, 180, 180);
      panelColor = color(200, 180);
      
      visible = true;
      open = true;

    }//Panel()
    //end constructor
  
  
  //methods
  public void run(){

    updateButtonVar();    //init/update button var
    Draw();    //draw the panel
    
    if(visible){ 
      if(open){  
        xPos = finalXPos;
        yPos = finalYPos; 
      }//end if open
      else if(!open){ 
        if     (buttonPos.equals("RIGHT" )) {  xPos = -panelW;  }
        else if(buttonPos.equals("LEFT"  )) {  xPos = width;    }
        else if(buttonPos.equals("TOP"   )) {  yPos = height;   }
        else if(buttonPos.equals("BOTTOM")) {  yPos = -panelH;  }
      }//end if !open  
    }//end if visible
    
    else if(!visible){
      if     (buttonPos.equals("RIGHT" )) { xPos = -panelW - buttonW;  }
      else if(buttonPos.equals("LEFT"  )) { xPos = width   + buttonW;  }
      else if(buttonPos.equals("TOP"   )) { yPos = height  + buttonH;  }
      else if(buttonPos.equals("BOTTOM")) { yPos = -panelH - buttonH;  }
    }//end if !visible
    
    updatePos();//updatePos
   
    // show/hide the controlP5 objects
    if(panelX == xPos && panelY == yPos){ //panel is opened
      //ControlP5.setVisible = true
    }
    else{ //panel is closed
      //ControlP5.setVisible = false
    }

    //change the button color if the mouse is on it
    if(mouseOnButton()){ buttonColor = mouseOnColor; }
    else{ buttonColor = mouseOffColor; }
  
  }//run()
  
  
  
  public void updateButtonVar(){
      if(buttonPos.equals("LEFT") || buttonPos.equals("RIGHT") ){
      buttonW = buttonSize; 
      buttonH = panelH;
      buttonY = panelY;
    }
    else if(buttonPos.equals("TOP") || buttonPos.equals("BOTTOM") ){
      buttonW = panelW; 
      buttonH = buttonSize;
      buttonX = panelX;
    }
    if     (buttonPos.equals("LEFT"  )) { buttonX = panelX - buttonW; }
    else if(buttonPos.equals("RIGHT" )) { buttonX = panelX + panelW ; }
    else if(buttonPos.equals("TOP"   )) { buttonY = panelY - buttonH; }
    else if(buttonPos.equals("BOTTOM")) { buttonY = panelY + panelH ; }
  }//initButton


  public void Draw(){
    //drawPanel
    noStroke();
    fill(panelColor);
    rect(panelX, panelY, panelW, panelH);
    //drawButton
    fill(buttonColor);
    rect(buttonX, buttonY, buttonW, buttonH);
  }//Draw
  
  
  public void updatePos(){
    updatePanelPos();
    updateControlP5Pos();//updateControlPtP
  }//updatePos
  
  public void updatePanelPos(){
     if(xPos > panelX){ panelX +=5; }
    if(xPos < panelX){ panelX -=5; }
    if(yPos > panelY){ panelY +=5; }
    if(yPos < panelY){ panelY -=5; }
  }//updatePanelPos
  
  public void updateControlP5Pos(){
    cp5.get(Textfield.class,"YEARINPUT").setPosition((int)panelX+10, (int)panelY+20);
    cp5.get(Bang.class,"SEND").setPosition((int)panelX+10, (int)panelY+60);

  }//updateControlP5Pos
  
  public boolean mouseOnButton(){
    if(mouseX>buttonX && mouseX<buttonX+buttonW && mouseY>buttonY && mouseY<buttonY+buttonH){ return true; }
    return false;
  }//mouseOnButton

  public void visible(boolean b){ visible = b; }//visible
  public void Open(boolean b){ open = b; } //open
  public void setX(int i){ xPos = i ; }
  public void setY(int i){ yPos = i ; }

  public void mouseClick(){
    if(mouseOnButton()){
      open = !open;
    }//end if
  }//mouseClick

}// class Panel
class DetailPoint extends RootPoint {
  public DetailPoint(float xPos, float yPos, int c, float mScale, String property){
    super(xPos,yPos,c,mScale);
    crime = property;
    isRoot = false;
    isDetail = true;
  }
  public void draw(){
    pushMatrix();
    translate(position.x,position.y);
    fill(colour);
    noStroke();
    ellipse(0,0,spacialDiameter.x,spacialDiameter.y);
    popMatrix();
  }
}
class FilteredDatePoint extends RootPoint {
  public FilteredDatePoint(float xPos, float yPos, int c, float mScale, String property, String crimeYear){
    super(xPos,yPos,c,mScale);
    crime = property;
    isRoot = false;
    isDetail = true;
    isPoint = false;
    datePoint = true;
    this.crimeYear = crimeYear;
  }
  public void draw(){
    pushMatrix();
    translate(position.x,position.y);
    fill(colour);
    noStroke();
    ellipse(0,0,spacialDiameter.x,spacialDiameter.y);
    popMatrix();
  }
  public void avgIncrease(){
    
    if(!datePoints.isEmpty()){
      float late = datePoints.get(datePoints.size()-1).count;
      float early = datePoints.get(0).count;
      
      avg = (late - early)/early * 100;
    }
    
    datePoints.clear();
  }
  

}
class PropertyPoint extends RootPoint{
  
  public PropertyPoint(float xPos, float yPos, int c, float mScale, String property){
    super(xPos,yPos,c,mScale);
    typeOfCrime = property;
    isRoot = false;
  }
  public void draw(){
    pushMatrix();
    translate(position.x,position.y);
    noStroke();
    fill(colour);
    ellipse(0,0,spacialDiameter.x,spacialDiameter.y);
    popMatrix();
  }
}
class RootPoint extends CloudPoint{
  
  
  public RootPoint(float xPos, float yPos, int c, float mScale){
    super(xPos,yPos,c,mScale);
    isAnchor = true;
    isRoot = true;
    isPoint = false;
  }
  public void draw(){
    pushMatrix();
    translate(position.x,position.y);
    noStroke();
    fill(colour);    
    ellipse(0,0,spacialDiameter.x,spacialDiameter.y);
    
    fill(255);
    float tSize = map(spacialDiameter.x,10,300,1,30);
    textSize(tSize);
    text("Crimes", -40, -25);
    text("in", -10, 0);
    text("California", -55,25);
    popMatrix();
  }
  
  public void follow(){
    if(freeze){
      target.x = position.x;
      target.y = position.y;
    }
    
    if(!repulse){
      xVel += (target.x-position.x)/width*1/4;
      yVel += (target.y-position.y)/height*1/4;
    }
    else{
      xVel += (target.x+position.x)/width*1/4;
      yVel += (target.y+position.y)/height*1/4;
    }
    
    xVel *= 0.98f;
    yVel *= 0.98f;
    
    position.x += xVel;
    position.y += yVel;
  }
  
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--full-screen", "--bgcolor=#666666", "--stop-color=#cccccc", "Crime_Ontologic" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
