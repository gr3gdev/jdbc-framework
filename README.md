# jdbc-framework

Une librairie java pour générer le code de la couche DAO et gérer automatiquement les appels à la base de données :

  * SELECT
  * UPDATE
  * INSERT
  * DELETE

Configuration :

- Maven

```xml
<dependency>
    <groupId>com.github.gr3gdev</groupId>
    <artifactId>jdbc-framework</artifactId>
    <version>0.4.1</version>
</dependency>

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.6.1</version>
    <configuration>
        <annotationProcessorPaths>
            <annotationProcessorPath>
                <groupId>com.github.gr3gdev</groupId>
                <artifactId>jdbc-framework</artifactId>
                <version>0.4.1</version>
            </annotationProcessorPath>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

- Gradle

```
implementation 'com.github.gr3gdev:jdbc-framework:0.4.1'
annotationProcessor 'com.github.gr3gdev:jdbc-framework:0.4.1'
```

Avec kotlin

```
plugins {
    kotlin("jvm") version "1.4.21"
    kotlin("kapt") version "1.4.21"
}

implementation("com.github.gr3gdev:jdbc-framework:0.4.1")
kapt("com.github.gr3gdev:jdbc-framework:0.4.1")
```


Annotations disponibles :

## @JDBC

Annotation de configuration des bases de données.

@JDBC

|Attribut    |Type           |Valeur par défaut|Description             |
|------------|---------------|-----------------|------------------------|
|conf        |Array<JdbcConf>|                 |Liste des configurations|

@JdbcConf

|Attribut           |Type  |Valeur par défaut|Description                                                                        |
|-------------------|------|-----------------|-----------------------------------------------------------------------------------|
|configFile         |String|                 |Chemin dans le classpath vers le fichier properties de configuration (HikariConfig)|
|databaseName       |String|default          |Nom de la base de données associée à la configuration                              |

Exemple de syntaxe :

<u>Avec plusieurs bases de données :</u>

    @JDBC(
        conf = {
                @JdbcConf(configFile = "/datasource1.properties", databaseName = "db1"),
                @JdbcConf(configFile = "/datasource2.properties", databaseName = "db2"),
                @JdbcConf(configFile = "/datasource3.properties", databaseName = "db3")
        }
    )
    public class Demo {
        
    }

<u>Avec une seule base de données :</u>

    @JDBC(
        conf = @JdbcConf(configFile = "/datasource.properties")
    )
    public class Demo {
        
    }


## @Table et @Column

Annotations pour définir la structure d'une table.

@Table

|Attribut    |Type   |Valeur par défaut|Description                                                   |
|------------|-------|-----------------|--------------------------------------------------------------|
|databaseName|String |default          |Nom de la base de données où la table doit être ajoutée       |

@Column

|Attribut           |Type   |Valeur par défaut|Description                                    |
|-------------------|-------|-----------------|-----------------------------------------------|
|primaryKey         |Boolean|false            |Vrai si la colonne est de type clé primaire    |
|autoincrement      |Boolean|false            |Vrai si la colonne s'auto incrémente           |
|require            |Boolean|true             |Vrai si la colonne est obligatoire             |

Exemple de syntaxe :

    @Table
    public class House {
    
        @Column(primaryKey = true, autoincrement = true)
        private int id;
    
        @Column(required = false)
        private String name;
    
        public int getId() {
            return id;
        }
    
        public void setId(int id) {
            this.id = id;
        }
    
        public String getName() {
            return name;
        }
    
        public void setName(String name) {
            this.name = name;
        }
    }


## @Queries et @Query

Annotations pour générer le code DAO.

@Queries

|Attribut      |Type   |Valeur par défaut|Description                                                                  |
|--------------|-------|-----------------|-----------------------------------------------------------------------------|
|implementation|Class  |                 |Classe abstraite qui implémente l'interface DAO sur laquelle est l'annotation|

@Query

|Attribut  |Type         |Valeur par défaut|Description                                                   |
|----------|-------------|-----------------|--------------------------------------------------------------|
|sql       |String       |                 |Requête SQL où les tables et les colonnes sont au format objet|

Exemple de syntaxe :
    
    @Queries
    public interface HouseDAO {
    
        @Query(sql = "INSERT House (House.id, House.name)")
        void add(House house);
    
        @Query(sql = "SELECT House FROM House WHERE House.id)
        House findById(int id);
    
    }

#### Format des requêtes

Les requêtes ont les colonnes et les tables au format objet. La table sera appelée par le nom de la classe associée à l'annotaion @Table, ses colonnes auront le nom de ses attributs.

Exemples :

Insertion de toutes les colonnes d'une table

    INSERT Table (Table)

Insertion de certaines colonnes uniquement

    INSERT Table (Table.field1, Table.field2)

Suppression de tous les éléments d'une table

    DELETE Table

Suppression des éléments d'une table avec un filtre

    DELETE Table WHERE Table.field

Mise à jour de tous les éléments d'une table

    UPDATE Table SET Table.field

Mise à jour des éléments d'une table avec un filtre

    UPDATE Table SET Table.field1 WHERE Table.field2

Sélection de tous les éléments d'une table (tient compte des jointures avec les colonnes qui sont obligatoires/non)

    SELECT Table FROM Table

Sélection des éléments d'une table avec un filtre

    SELECT Table FROM Table WHERE Table.field



## Le code généré

### JDBCFactory

La classe JDBCFactory fournit les méthodes suivantes :

    * init()
    * une par DAO

Pour exécuter des scripts sur les bases de données, il faut ajouter un fichier properties avec le nom de la base dans src/main/resources.
Par exemple pour une base de donnée "BaseTest" :

```
src / main / resources / BaseTest.properties
```

Ce fichier doit contenir tous les scripts de migration, exemple :

```
v1_0_0.create_table1.1=/path_to_script1
v1_0_0.create_table2.2=/path_to_script2
v1_0_1.correction1.1=/path_to_correction1
v1_0_2.correction2.1=/path_to_correction2
```

Voir le projet `jdbc-framework-sample`
