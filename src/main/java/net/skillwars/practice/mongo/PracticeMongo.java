package net.skillwars.practice.mongo;

import com.mongodb.AuthenticationMechanism;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import lombok.Getter;

import net.skillwars.practice.Practice;
import org.bson.Document;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;

@Getter
public class PracticeMongo {

    @Getter
    private static PracticeMongo instance;

    private MongoClient client;
    private MongoDatabase database;
    @Getter
    private MongoCollection<Document> players;

    public PracticeMongo() {
        if (instance != null) {
            throw new RuntimeException("The mongo database has already been instantiated.");
        }

        instance = this;

        FileConfiguration config = Practice.getInstance().getMainConfig().getConfig();

        if (!config.contains("mongo.host")
                || !config.contains("mongo.port")
                || !config.contains("mongo.database")
                || !config.contains("mongo.authentication.enabled")
                || !config.contains("mongo.authentication.username")
                || !config.contains("mongo.authentication.password")
                || !config.contains("mongo.authentication.database")) {
            throw new RuntimeException("Missing configuration option");
        }

        if (config.getBoolean("mongo.authentication.enabled")) {
            final MongoCredential credential = MongoCredential.createCredential(
                    config.getString("mongo.authentication.username"),
                    config.getString("mongo.authentication.database"),
                    config.getString("mongo.authentication.password").toCharArray()
            );

            this.client = new MongoClient(new ServerAddress(config.getString("mongo.host"), config.getInt("mongo.port")), Collections.singletonList(credential));
        } else {
            this.client = new MongoClient(new ServerAddress(config.getString("mongo.host"), config.getInt("mongo.port")));
        }

        this.database = this.client.getDatabase(config.getString("mongo.database"));
        this.players = this.database.getCollection("players");
    }

}
