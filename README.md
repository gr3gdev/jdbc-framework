# jdbc-framework

Une librairie java pour générer le code de la couche DAO et gérer automatiquement les appels à la base de données :

  * CREATE TABLE
  * SELECT
  * UPDATE
  * INSERT
  * DELETE

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
|autoincrementSyntax|String|AUTO_INCREMENT   |Syntaxe SQL pour l'auto-incrément de la colonne                                    |

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
|required           |Boolean|false            |Vrai si la colonne est obligatoire             |
|sqlType            |String |                 |Type SQL pour la colonne                       |

Exemple de syntaxe :

    @Table
    public class House {
    
        @Column(primaryKey = true, autoincrement = true, sqlType = "INT")
        private int id;
    
        @Column(required = true, sqlType = "TEXT")
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

|Attribut     |Type   |Valeur par défaut|Description                                            |
|-------------|-------|-----------------|-------------------------------------------------------|
|mapTo        |Class  |                 |Classe correspondante à la @Table associée aux requêtes|

@Query

|Attribut  |Type         |Valeur par défaut|Description                                                 |
|----------|-------------|-----------------|------------------------------------------------------------|
|type      |QueryType    |                 |Type de requête : INSERT, SELECT, UPDATE, DELETE            |
|attributes|Array<String>|*                |Liste des attributs à sélectionner, mettre à jour ou insérer|
|filters   |Array<String>|                 |Liste des filtres à appliquer à une requête                 |

Exemple de syntaxe :
    
    @Queries(mapTo = House.class)
    public interface HouseDAO {
    
        @Query(type = QueryType.INSERT)
        void add(House house);
    
        @Query(type = QueryType.SELECT, attributes = {"id", "name"}, filters = {"id"})
        House findById(int id);
    
    }
    

## Le code généré

### JDBCFactory

La classe JDBCFactory fournit les méthodes suivantes :

    * init()
    * une par DAO

Voir le projet `jdbc-framework-sample`
