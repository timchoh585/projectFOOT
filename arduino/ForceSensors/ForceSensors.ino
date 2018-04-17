#include <SoftwareSerial.h>

SoftwareSerial BTserial(3, 2); // RX | TX

int forceSensor = 0;

int forceReading;

char startString = '#';

char endString = '~';int LEDPin = 13;void setup() {

 

 BTserial.begin(9600);  

 Serial.begin(9600);

 pinMode(LEDPin,OUTPUT);

}void sendForceData() {

 forceReading = analogRead(forceSensor);

 //Serial.print(forceReading);

 int mappedValue = forceReading;//map(forceReading, 0, 767, 0000, 1023);

 char myBuffer[30];

 sprintf(myBuffer,"%c%04d%04d%04d%04d%04d%04d%04d%c", startString, mappedValue, mappedValue, mappedValue, mappedValue, mappedValue, mappedValue, mappedValue, endString );  BTserial.println(myBuffer);

 Serial.println(myBuffer);}void loop ()

{

 if(BTserial.available())

 {

   sendForceData();

 }

 delay(100);

}
