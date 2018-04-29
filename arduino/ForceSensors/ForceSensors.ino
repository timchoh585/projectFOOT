#include <SoftwareSerial.h>
SoftwareSerial BTserial(3, 2); 
// RX | TX on Arduino (so HC-05 RX goes to pin 2, TX goes to pin 3, GND goes between 1 and 2k resistors)
int forceSensor0 = 0;
int forceSensor1 = 2;
int forceSensor2 = 3;
int forceSensor3 = 4;
int forceSensor4 = 5;
int forceSensor5 = 6;
int forceSensor6 = 1;

int forceSensor[7] = { 0, 0, 0, 0, 0, 0, 0 };

int forceReading0, forceReading1, forceReading2, forceReading3, forceReading4, forceReading5, forceReading6;
char startString = '#';
char endString = '~';
bool stepping = false;
bool startRec = false;
bool stopRec = false;
int toe = 0;
int heel = 6;
int threshold = 130;

void setup() {
  
  BTserial.begin(9600);  
  Serial.begin(9600);
}

void testSensor() {
  int toeValue = forceReading0;
  int heelValue = forceReading6;

  forceSensor[0] = forceReading0;
  forceSensor[1] = 0;//forceReading1;
  forceSensor[2] = 0;//forceReading2;
  forceSensor[3] = 0;//forceReading3;
  forceSensor[4] = 0;//forceReading4;
  forceSensor[5] = 0;//forceReading5;
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

void checkForStep() {
  if( greaterThanRestOfSensors( heel ) && !stopRec) {
    if(forceSensor[ heel ] > threshold){
      stepping = true;
      startRec = true;
    }
  }
  if( greaterThanRestOfSensors( toe )  && startRec) {
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
  //if(BTserial.available() && stepping )
  //{
  if(stepping){
    sendForceData();
  }
  //}
  
  delay(333);
}
