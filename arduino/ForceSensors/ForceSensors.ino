#include <SoftwareSerial.h>
SoftwareSerial BTserial(3, 2); // RX | TX
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
int heel = 6;
int toe = 0;

void setup() {
  
  BTserial.begin(9600);  
  Serial.begin(9600);
}

void testSensor() {
  int toeValue = forceReading0;
  int heelValue = forceReading6;

  forceSensor[0] = toeValue;
  forceSensor[1] = toeValue -10;
  forceSensor[2] = toeValue -10;
  forceSensor[3] = heelValue -10;
  forceSensor[4] = heelValue -10;
  forceSensor[5] = heelValue -10;
  forceSensor[7] = heelValue;
}

void sendForceData() {
  forceReading0 = analogRead(forceSensor0);
  forceReading6 = analogRead(forceSensor6);
  testSensor();
  
  char myBuffer[30];

  //Serial.print(forceReading);
  //int mappedValue = forceReading;//map(forceReading, 0, 767, 0000, 1023);
  sprintf(myBuffer,"%c%04d%04d%04d%04d%04d%04d%04d%c", startString, forceSensor[0], forceSensor[1], forceSensor[2], forceSensor[3], forceSensor[4], forceSensor[5], forceSensor[6], endString );
//    sprintf(myBuffer,"%c%04d%04d%04d%04d%04d%04d%04d%c", startString, mappedValue, mappedValue, mappedValue, mappedValue, mappedValue, mappedValue, mappedValue, endString );
  BTserial.print(myBuffer);
  //Serial.println(myBuffer);
}


bool greaterThanRestOfSensors( int sensor ) {
  for( int i = 0; i < 6; i++ ) {
    if( !sensor == i ) { continue; }
    else if ( forceSensor[ sensor ] > forceSensor[ i ] ) {
      continue;
    } else {
      return false;
    }
  }
  return true;
}

void checkForStep() {
  if( greaterThanRestOfSensors( heel ) ) {
    stepping = true;
  }
  if( greaterThanRestOfSensors( toe ) ) {
    stepping == false;
    // DO 'X' Terminating Character Here
  }
}

void loop ()
{
  checkForStep();
  if(BTserial.available() && stepping )
  {
    sendForceData();
  }
  
  delay(100);
}
