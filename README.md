# chatop-api


## Installation du projet


### 1/ Récupérer le code source de l'application Spring Boot

Assurez-vous que Java et Maven sont installés sur votre machine en amont

Exécutez la commande  git clone https://github.com/nicrz/chatop-api.git dans votre terminal afin de récupérer les fichiers du projet

Exécutez la commande mvn install afin d'installer les dépendances


### 2/ Installer la base de données

Installez MySQL Server sur votre machine, vous pouvez suivre ce tutoriel : https://openclassrooms.com/fr/courses/6971126-implementez-vos-bases-de-donnees-relationnelles-avec-sql/7152681-installez-le-sgbd-mysql#/id/r-7152721

Une fois que vous avez lancé votre terminal, renseignez votre mot de passe défini précédemment au cours de l'installation

Exécutez la commande CREATE DATABASE chatopdb; puis USE chatopdb; puis source chatopdb.sql (en spécifiant le bon chemin vers le fichier que vous trouverez dans resources/chatopdb.sql)

Attendez que le processus d'importation se termine.

Désormais il faut nous rendre dans le fichier application.properities et modifier ces 4 lignes en conséquence :

spring.datasource.url=jdbc:mysql://localhost:3306/nombdd

spring.datasource.username=root

spring.datasource.password=mdp

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

### 3/ Lancer le serveur

Ouvrez le dossier du projet avec un IDE (IntelliJ, VS Code, Eclipse..) et démarrez l'application avec le bouton "play" situé en haut à droite de votre interfac, l'IDE va compiler et lancer le projet, votre serveur dédié au back-end de votre appplication est désormais lancé !
