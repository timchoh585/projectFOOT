#include <SoftwareSerial.h>
SoftwareSerial BTserial(3, 2); 
// RX | TX on Arduino (so HC-05 RX goes to pin 2, TX goes to pin 3, GND goes between 1 and 2k resistors)
int forceSensor0 = 0; // Toe
int forceSensor1 = 1; // Ball
int forceSensor2 = 2; // Side 
int forceSensor3 = 3; // Side
int forceSensor4 = 4; // Side
int forceSensor5 = 5; // Side
int forceSensor6 = 6; // Heel

int forceSensor[7] = { 0, 0, 0, 0, 0, 0, 0 };

int forceReading0, forceReading1, forceReading2, forceReading3, forceReading4, forceReading5, forceReading6;
char startString = '#';
char endString = '~';
bool stepping = false;
bool startRec = false;
bool stopRec = false;
int toe = 0;
int heel = 6;
int threshold = 150;

void setup() {
  
  BTserial.begin(9600);  
  Serial.begin(9600);
}

void testSensor() {
  int toeValue = forceReading0;
  int heelValue = forceReading6;

  forceSensor[0] = forceReading0;
  forceSensor[1] = forceReading1;
  forceSensor[2] = forceReading2;
  forceSensor[3] = forceReading3;
  forceSensor[4] = forceReading4;
  forceSensor[5] = forceReading5;
  forceSensor[6] = forceReading6;
}

void sendForceData() {
  
  char myBuffer[30];
  //char myBuffer[10];

  //Serial.print(forceReading);
  //int mappedValue = forceReading;//map(forceReading, 0, 767, 0000, 1023);
  sprintf(myBuffer,"%c%04d%04d%04d%04d%04d%04d%04d%c", startString, forceSensor[0], forceSensor[1], forceSensor[2], forceSensor[3], forceSensor[4], forceSensor[5], forceSensor[6], endString );
  //sprintf(myBuffer,"%c%04d%04d%c", startString, forceSensor[0], forceSensor[6], endString );
//    sprintf(myBuffer,"%c%04d%04d%04d%04d%04d%04d%04d%c", startString, mappedValue, mappedValue, mappedValue, mappedValue, mappedValue, mappedValue, mappedValue, endString );
  BTserial.print(myBuffer);
  Serial.println(myBuffer);
}


bool greaterThanRestOfSensors( int sensor ) {
  for( int i = 0; i < 6; i++ ) {
    if( i != sensor){
      if ( forceSensor[ sensor ] < forceSensor[ i ] ) {
        return false;
      } 
    }
  }
  return true;
}

bool allUnderThreshold(){
  for (int i = 0; i < 6; i++){
    if (forceSensor[i] > threshold){
      return false;
    }
  }
  return true;
}
void checkForStep() {
  if( greaterThanRestOfSensors( heel ) && !stopRec) {
    if(forceSensor[ heel ] > threshold){
      stepping = true;
      startRec = true;
    }
  }
  if(startRec) {
      stopRec = true;
    if ((forceSensor[ toe ] < threshold) && (forceSensor[ heel ] < threshold) && stepping){
      char endBuffer[31];
      sprintf(endBuffer,"%c%04d%04d%04d%04d%04d%04d%04d%c%c", startString, forceSensor[0], forceSensor[1], forceSensor[2], forceSensor[3], forceSensor[4], forceSensor[5], forceSensor[6], 'X', endString);
      BTserial.print(endBuffer);
      Serial.println(endBuffer);
      stepping = false;
      stopRec = false;
      startRec= false;
    }
  }
  /*
  // Starting step with toe
  if( greaterThanRestOfSensors( toe )  && !startRec && forceSensor[toe] > threshold){
    char errorBuffer[3];
    sprintf(errorBuffer,"%c%c%c",startString,'^',endString);
    BTserial.print(errorBuffer);
    Serial.println(errorBuffer);
    
  }*/
}

void calibrate(){
  char myBuffer[34];
  char space = ' ';
  sprintf(myBuffer,"%04d%c%04d%c%04d%c%04d%c%04d%c%04d%c%04d", forceSensor[0], space, forceSensor[1], space, forceSensor[2], space, forceSensor[3], space, forceSensor[4], space, forceSensor[5], space, forceSensor[6]);
  Serial.println(myBuffer);
}

void loop ()
{
  //stepping = false;
  forceReading0 = analogRead(forceSensor0);
  forceReading1 = analogRead(forceSensor1);
  forceReading2 = analogRead(forceSensor2);
  forceReading3 = analogRead(forceSensor3);
  forceReading4 = analogRead(forceSensor4);
  forceReading5 = analogRead(forceSensor5);
  forceReading6 = analogRead(forceSensor6);
  testSensor();  
  checkForStep();
  // Send data over bluetooth if stepping and connected to bluetooth
  if(BTserial.available() && stepping ){
    sendForceData();
  }
  // Otherwise print to serial the sensor information for debugging
  else{
    calibrate();
  }
  
  
  
  delay(333);
}
