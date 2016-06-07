#include <SoftwareSerial.h>
#include "LPD8806.h"
#include "SPI.h"

// Bluetooth rx and tx pins.
const uint8_t rxPin = 10;
const uint8_t txPin = 11;

// Number of RGB LEDs in strand:
const int nLEDs = 48;

char previousChar = '\n';

uint32_t amplitude = 0;
int16_t position = -1;

// Chose 2 pins for output; can be any valid output pins:
int dataPin  = 2;
int clockPin = 3;

SoftwareSerial bluetooth(rxPin, txPin);
LPD8806 strip = LPD8806(nLEDs, dataPin, clockPin);

void setup() {
  // Sets pin mode for bluetooth rx and tx
  pinMode(rxPin, INPUT);
  pinMode(txPin, OUTPUT);
  
  // Start up the LED strip
  strip.begin();

  // Update the strip, to start they are all 'off'
  strip.show();

  bluetooth.begin(115200);
  Serial.begin(115200);
}

void loop() {
  while(bluetooth.available()) {
    char receivedChar = bluetooth.read();
    if(receivedChar == -1) {
      receivedChar = 0;
    }
    
    if(receivedChar != '\t') {
      if(receivedChar != '\n') {
        if(previousChar == '\n') {
          if(amplitude > 255) {
            amplitude = 255;
          }
          strip.setPixelColor(position, amplitude, 0, 0);
          amplitude = 0;
          position++;
          if(position == nLEDs) {
            position = 0;
            strip.show();
          }
        }
        amplitude = (amplitude * 10) + (receivedChar - '0');
      }
      previousChar = receivedChar;
    } else {
      for(int i = 0; i < nLEDs; i++) {
        strip.setPixelColor(i, 0, 0, 0);
      }
      strip.show();
    }
  }
}