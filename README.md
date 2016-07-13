# Importar ficheros CSV en Elasticsearch

## Descripción

Cuando se trata de cargas masivas de datos, no podemos utilizar el API Java directamente para indexar uno por uno todos los documentos. 
Se puede hacer pero no es práctico desde el punto de vista del rendimiento. 
En estos casos necesitamos que la carga sea rápida y nos de un feedback de lo que ha ocurrido.
En estos casos debemos utilizar el API Bulk de elasticsearch. Tenemos dos formas:

1. Invocar directamente al API Rest de bulk. Útil cuando ya disponemos de un fichero Bulk generado previamente.
   Podemos cargarlo utilizando el comando curl.
 
2- Utilizar el Java Bulk API de la librería cliente de elasticsearch.
   Útil en los casos de no disponer de un fichero Bulk o bien nos sentimos más cómodos utilizando Java.
 
La recomendación dada por elasticsearch es de 5 a 15 MB por cada bulk. 
No se suele utililizar el número de documentos como referencia ya que como es obvio el tamaño de un tipo de documento varia, 
y por tanto no es lo mismo 10000 documento de 1 KB cada uno que 10000 documentos de 10 KB.

En el ejemplo que vamos a ver hemos puesto un tamaño de bulk muy pequeño, ya que el fichero csv con datos es un fichero de poco tamaño.

También influye el número de shards primarios y replicas que tengamos en nuestro índice. Recordemos que cuantos más replicas tengamos más rápido son las búsquedas, 
y cuantos más shards primarios tengamos más rápido serán las cargas bulk y las indexaciones (rapidez en la indexación de domentos)

Dentro del Java API tenemos dos posibilidades de realizar la carga. La opción elegida depende como siempre de lo que necesitemos. 
El principal objetivo de este proyecto es el de mostrar el uso del API Bulk de elasticsearch. 
Para ello hemos creado un proyecto que carga un fichero CSV cualquiera en elasticsearch. 
La creación del mapping se deja que sea elasticsearch el se encargue de su creación,
 aunque como siempre es recomentable que lo hagamos nosotros. 


## Formato del fichero CSV  

**Primera fila**. Nombre de las columnas separadas por el carácter pipe |. 
Este nombre de las columnas será también el utilizado por el documento JSON  que irá a elasticsearch.

**Resto de filas**. Valores de las columnas separados por el caráter pipe |.

Un fichero de ejemplo se encuentra en este mismo proyecto, es el fichero **example.csv**. La estructura es:

~~~~
Orden|Apellido|Total
1|GARCIA|1476378
2|GONZALEZ|929938
3|RODRIGUEZ|928305
~~~~
## Formato del tipo de documento de elasticsearch

Su estructura depende el fichero CSV. Así por ejemplo, para el fichero CSV incluido en este proyecto estos son documentos Json válidos para elasticsearch.

~~~~
{"orden": "1", "apellido": "GARCIA", "total": "1476378" }
{"orden": "2", "apellido": "GONZALEZ", "total": "929938" }
{"orden": "3", "apellido": "RODRIGUEZ", "total": "928305" }
~~~~

## Formato del tipo de documento de elasticsearch

Para el fichero CSV examplekpisevent.csv incluido en este proyecto  estos son documentos Json válidos para elasticsearch.
Separados por comas
Crear json.txt
~~~~
5646939,27/01/2016 8:03:43,SSO,I-CONPERSONALIZACIONMIVF,I-Consulta,Middleware,653,662969918,CLIENTEPREPAGO,OK,null,null,null




## Versión de frameworks y software utilizado

- Spring 4.2.1.RELEASE
- Log4j 1.2.14
- Elasticsearch Java API client 1.7.1
- Java 8

## Clases Principales y ficheros de configuración

- com.tecmaral.elastic.conf.AppConfig. Clase de configuración basada en anotaciones de Spring.
- com.tecmaral.elastic.MainProcess. Clase *main* que inicia la carga del fichero csv.
- com.tecmaral.elastic.LoadCsvImpl. Clase principal que organiza la carga del fichero CSV en elasticsearch.
- com.tecmaral.elastic.csv.CsvManagerImpl. Lee el fichero CSV línea a línea.
- com.tecmaral.elastic.es.ESManagerImpl. Se conecta a elasticsearch utilizando *Transport client* y prepara la carga bulk.
- default.properties y development.properties. Son ficheros de configuración para un entorno local y un entorno de desarrollo donde se guarda la configuración
  de acceso a elasticsearch, la ruta fichero csv, la configuración del bulk etc. 
  Sus valores pueden ser sobreescritos por propiedades de la Jvm en el arranque de la aplicación.


## Ejecución 

**Arranque por defecto, entorno local**. Podemos indicar la propiedad spring.profiles.active o dejarla sin indicar. 

~~~~
MainProcess
MainProcess -Dspring.profiles.active=default
loquemeinteresadelared.MainProcess -Dspring.profiles.active=development 

