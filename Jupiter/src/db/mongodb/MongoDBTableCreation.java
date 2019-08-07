package db.mongodb;
import java.text.ParseException;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

public class MongoDBTableCreation {
	//Run as Java application to create MongoDB collection with index
	public static void main(String[] args) throws ParseException {
		//step 1, connection to MongoDB
		MongoClient mongoClient = MongoClients.create();
		MongoDatabase db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);
		
		//step 2, remove old collections.
		db.getCollection("users").drop();
		db.getCollection("items").drop();
		
		//step 3, create new collections
		IndexOptions indexOptions = new IndexOptions().unique(true);
		db.getCollection("users").createIndex(new Document("user_id", 1), indexOptions);
		db.getCollection("items").createIndex(new Document("item_id", 1), indexOptions);
		
		//step 4, insert fake user data and create index.
		db.getCollection("users").insertOne(new Document().append("user_id", "111").
				append("password", "3229c1097c00d497a0fd282d586be050").
				append("first_name", "John").append("last_name", "Smith"));
		
		//step 5 close connection
		mongoClient.close();
		System.out.println("Import is done successfully!");
		
	}

}
