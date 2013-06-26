# LightsOn!
Simple Light Switcher from Desktop with Arduino

## Compile

## Run
### Windows 32 bit
```
start javaw -Djava.library.path="%CD%\lib\win32" -cp .;.\lib\commons-io-2.4.jar;.\lib\RXTXcomm.jar es.csic.lec.lightson.Main
```

### Windows 64 bit
```
start javaw -Djava.library.path="%CD%\lib\win64" -cp .;.\lib\commons-io-2.4.jar;.\lib\RXTXcomm.jar es.csic.lec.lightson.Main
```

## Libraries
* [Apache Commons IO 2.4](http://commons.apache.org/proper/commons-io/)
* [RxTx 2.2pre2](http://rxtx.qbang.org/)