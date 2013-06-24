/*
  Remote Serial Control
  Language: Arduino
 
 This program waits for a command to read analog device or to
 switch something on or off.
 
 The circuit:

 
 
 Created 1 Feb. 2013
 by Eduardo González García edu@iact.ugr-csic.es
 
 */

#define RELAY 4

#define LED1 5 /* WHITE WIRE*/ /* RED LED */
#define LED2 3 /* GREEN WIRE*/ /* BLUE RED */

/* RED WIRE */ /* RESET BUTTON */

#define POWER 2 /* BLUE WIRE */ /* POWER BUTTON */

#define STATUS_LED LED2

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
int light = OFF;

int lastButtonState = HIGH;
int state;
int lastState = HIGH;

long lastDebounceTime = 0;  // the last time the output pin was toggled
long debounceDelay = 50;    // the debounce time; increase if the output flickers

void setup()
{
  //wdt_disable();
  pinMode(STATUS_LED, OUTPUT);

  pinMode(RELAY,OUTPUT);
  
  pinMode(LED1, OUTPUT);
  
  pinMode(POWER,INPUT_PULLUP);
  
  // start serial port at 9600 bps:
 
  digitalWrite(RELAY, LOW);
  digitalWrite(STATUS_LED, LOW);
  Serial.begin(9600);
  
  digitalWrite(LED1, HIGH);
  
  Serial.write(IDENTIFY|OFF);
}

void loop()
{
   int reading = digitalRead(POWER);
  
  // If the switch changed, due to noise or pressing:
  if (reading != lastButtonState) {
    // reset the debouncing timer
    lastDebounceTime = millis();
  } 
  
  if ((millis() - lastDebounceTime) > debounceDelay) {
    state = reading;
    
    if(state==LOW && lastState==HIGH){
      if(light==ON){
        digitalWrite(STATUS_LED, LOW);
        digitalWrite(RELAY, LOW);
        light=OFF;
        Serial.write(IDENTIFY|OFF);
        
      }else{
        digitalWrite(STATUS_LED, HIGH);
        digitalWrite(RELAY, HIGH);
        light=ON;
        Serial.write(IDENTIFY|ON);
      }  
    }
    
    lastState=state;
  }
  
  
  // save the reading.  Next time through the loop,
  // it'll be the lastButtonState:
  lastButtonState = reading;
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


