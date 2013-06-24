/*
  Remote Serial Control
  Language: Arduino
 
 This program waits for a command to read analog device or to
 switch something on or off.
 
 The circuit:

 
 
 Created 1 Feb. 2013
 by Eduardo González García edu@iact.ugr-csic.es
 
 */

#define STATUS_LED 13
#define RELAY 4

//Commands
#define COMMAND 0x01
#define SWITCH_ON 0x02
#define SWITCH_OFF 0x04
#define IDENTIFY 0x08

//Responses
#define OK 0x10
#define FAIL 0x20
#define ON 0x40
#define OFF 0x80

int inByte = 0;         // incoming serial byte
int light = ON;



void setup()
{
  //wdt_disable();
  pinMode(STATUS_LED, OUTPUT);

  pinMode(RELAY,OUTPUT);
  
  // start serial port at 9600 bps:
 
  digitalWrite(RELAY, LOW);
  digitalWrite(STATUS_LED, LOW);
  Serial.begin(9600);
  
}

void loop()
{
  
}
  
void serialEvent(){
  while (Serial.available()) {
    
    inByte = Serial.read();
    
    if(inByte&COMMAND){
      if(inByte&SWITCH_ON){
        
        digitalWrite(STATUS_LED, HIGH);
        digitalWrite(RELAY, HIGH);
        
        light=ON;
        
        Serial.write(SWITCH_ON|OK);
        
      }else if(inByte&SWITCH_OFF){
        digitalWrite(STATUS_LED, LOW);
        digitalWrite(RELAY, LOW);
        
        light=OFF;
        
        Serial.write(SWITCH_OFF|OK);
        
      }else if(inByte&IDENTIFY){
        
        Serial.write(IDENTIFY|light);
      }
      
    }
    
  }

}


