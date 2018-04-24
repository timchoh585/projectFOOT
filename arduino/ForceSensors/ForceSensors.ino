#include <SoftwareSerial.h>
SoftwareSerial BTserial(3, 2); 
// RX | TX on Arduino (so HC-05 RX goes to pin 2, TX goes to pin 3, GND goes between 1 and 2k resistors)
int forceSensor0 = 0;
int forceSensor1 = 0;
int forceSensor2 = 0;
int forceSensor3 = 0;
int forceSensor4 = 0;
int forceSensor5 = 0;
int forceSensor6 = 1;

int forceSensor[7] = { 0, 0, 0, 0, 0, 0, 0 };

int forceReading0, forceReading6;
char startString = '#';
char endString = '~';
bool stepping = false;
bool startRec = false;
bool stopRec = false;
int toe = 0;
int heel = 6;

void setup() {
  
  BTserial.begin(9600);  
  Serial.begin(9600);
}

void testSensor() {
  int toeValue = forceReading0;
  int heelValue = forceReading6;

  forceSensor[0] = toeValue;
  forceSensor[1] = toeValue -50 < 0 ? 0 : toeValue - 50;
  forceSensor[2] = toeValue -50 < 0 ? 0 : toeValue - 50;
  forceSensor[3] = heelValue -50 < 0 ? 0 : heelValue - 50;
  forceSensor[4] = heelValue -50 < 0 ? 0 : heelValue - 50;
  forceSensor[5] = heelValue -50 < 0 ? 0 : heelValue - 50;
  forceSensor[6] = heelValue;
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

void checkForStep() {
  if( greaterThanRestOfSensors( heel ) && !stopRec) {
    if(forceSensor[ heel ] > 15){
      stepping = true;
      startRec = true;
    }
  }
  if( greaterThanRestOfSensors( toe )  && startRec) {
    stopRec = true;
    if ((forceSensor[ toe ] < 15) && stepping){
      char endBuffer[31];
      sprintf(endBuffer,"%c%04d%04d%04d%04d%04d%04d%04d%c%c", startString, forceSensor[0], forceSensor[1], forceSensor[2], forceSensor[3], forceSensor[4], forceSensor[5], forceSensor[6], 'X', endString);
      BTserial.print(endBuffer);
      Serial.println(endBuffer);
      stepping = false;
      stopRec = false;
      startRec= false;
    }
  }
}

void loop ()
{
  //stepping = false;
  forceReading0 = analogRead(forceSensor0);
  forceReading6 = analogRead(forceSensor6);
  testSensor();
  
  checkForStep();
  //if(BTserial.available() && stepping )
  //{
  if(stepping){
    sendForceData();
  }
  //}
  
  delay(333);
}

