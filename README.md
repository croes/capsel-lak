# capsel-lak

## Requirements

* [unfolding](http://unfoldingmaps.org), more specifically @bgotink's [fork](/bgotink/unfolding)
    * Clone the fork
    * build the library using ```ant```
    * use the unfolding app template found at ```dist/unfolding_app_template.zip```, or use the jar in ```lib/``` (source is more up to date)
    * the jar files in the library folder have to be added to the project
    * Use the vecmath.jar provided by your OS if available (```/Library/Java/Extensions/vecmath.jar``` on OS X, ```/usr/share/java/vecmath.jar``` on Ubuntu)

## TODO

- Verschillende kleuren voor edges/nodes?
- Add other visualisations (like number of authors/papers for that conference/year)
- brushing and linking, link the different visualisations together (click on something on the 1 visualisation will change something in another visualisation)
- Country locations veranderen naar hoofdstad, halen van de google maps api
- Kleuren aanpassen en over nadenken
- Readability van markers (bvb met rechthoek waarin geschreven wordt.)


## TODO tegen volgende week
- Presentatie van 10-15 minuten, 
- - focus op wat er uit u brainstorm is gekomen
- - wat is de onderzoeksvraag die je wilt oplossen, 
- - welke opties heb je overwogen, wat waren de voor en nadelen?
- - DEMO 
- - Welke verhalen heb je al uit u visualisatie gehaald?
- - Wat hebt ge geleerd uit dat proces
- - Hoe werk je samen, hoe wordt het werk verdeeld?
- - Technische problemen gehad? 
- - Hoe wil je deze visualisatie(s) nu evalueren? (gewoon op de conferentie staan, maar wat gaat ge die mensen dan vragen?)


## TODO
- KeywordMap bugs eruithalen


## DONE
+ Title van barchart wijzigen naar volledige titel als hover
+ Als hover over barchart, de universiteit op de map openen
+ Meer filtering-opties (conferentie scheiden van jaar bvb) -> show multiple at the same time and differentiate between them with colors or shading. 
+ Countrymap samenbrengen met UniversityMap (met animatie)
+ Edges toevoegen aan lakmap
+ Treshold tussen country en organisation markers aanpassen