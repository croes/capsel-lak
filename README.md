# capsel-lak

## Requirements

* [unfolding](http://unfoldingmaps.org), more specifically @bgotink's [fork](/bgotink/unfolding)
    * Use the unfolding app template, but replace ```unfolding.jar``` with the jar in ```lib/```
    * or:
        * Clone the fork
        * build the library using ```ant```
        * use the unfolding app template found at ```dist/unfolding_app_template.zip```, or use the jar in ```lib/```
* the jar files of Apache Jena, JFreeChart, libAni, ControlP5, log-wrapper, geonames and jdom in the library folder have to be added to the project, as wel as ```vecmath.jar```
    * Use the ```vecmath.jar``` provided by your OS if available (```/Library/Java/Extensions/vecmath.jar``` on OS X, ```/usr/share/java/vecmath.jar``` on Ubuntu)
