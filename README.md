# sdtd-spark

### Description 
- Premièrement, nous avons réalisé une classe Spark en Scala `StreamHandler.scala` qui va se connecter au topic `crimes` du cluster Kafka pour avoir accès au streaming des données et puis veillera à traiter ces dernières avec pour type de calcul le `Batch Processing` en utilisant le principe de l'algorithme de traitement de données qui est le `Random Forest Classifier`. 

- Le `Dockerfile` nous permet de setup l'environnement adéquat pour le bon fonctionnement du logiciel Spark.

- Le fichier de configuration `ha.conf` nous permet d'introduire la haute disponibilité à notre cluster Spark en utilisant le service Zookeeper.

- Enfin, le `startup_script.sh` va nous permettre d'exécuter un master node et un worker node, puis de compiler la classe de traitement et de l'exécuter avec les packages nécessaires (drivers, connectors...).

