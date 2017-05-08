package deplambda.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.jena.atlas.json.JSON;
import org.bson.Document;

import java.util.Map;

/**
 * Created by ffancellu on 26/04/2017.
 */
public class MongoDumper {

    private static MongoClient mongoClient = new MongoClient();

    public static void dumpJsonArray (JsonArray array,
                                      String dbName,
                                      String collectionName){
        MongoDatabase mongoDB = mongoClient.getDatabase(dbName);
        MongoCollection coll = mongoDB.getCollection(collectionName);
        for (Object obj: array){
            JsonObject sentObj = (JsonObject) obj;
            Document doc = Document.parse(sentObj.toString());
            coll.insertOne(doc);
        }
    }

}
