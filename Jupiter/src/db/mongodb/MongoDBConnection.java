package db.mongodb;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import static com.mongodb.client.model.Filters.eq;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterClient;

public class MongoDBConnection implements DBConnection {
	private static MongoDBConnection instance;
	public static DBConnection getInstance() {
		if (instance == null) {
			instance = new MongoDBConnection();
		}
		return instance;
	}
	private MongoClient mongoClient;
	private MongoDatabase db;
	
	public MongoDBConnection() {
		//connects to local mongodb server.
		mongoClient = MongoClients.create();
		db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);
	}
	@Override
	public void close() {
		//TODO
		if (mongoClient != null) {
			mongoClient.close();
		}
	}
	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		if (db == null) {
			return;
		}
		db.getCollection("users").updateOne(eq("user_id", userId), 
				new Document("$push", new Document("favorite", new Document("$each", itemIds))));
	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		if (db == null) {
			return;
		}
		db.getCollection("users").updateOne(eq("user_id", userId), new Document("$pullAll", new Document("favorite", itemIds)));
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		if (db == null) {
			return new HashSet<>();
		}
		Set<String> favoriteItemIds = new HashSet<>();
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		
		if (iterable.first() != null && iterable.first().containsKey("favorite")) {
			@SuppressWarnings("unchecked")
			List<String> list = (List<String>) iterable.first().get("favorite");
			favoriteItemIds.addAll(list);
		}
		return favoriteItemIds;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		if (db == null) {
			return new HashSet<>();
		}
		Set<Item> favoriteItems = new HashSet<>();
		Set<String> itemIds = getFavoriteItemIds(userId);
		for (String itemId : itemIds) {
			FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", itemId));
			if (iterable.first() != null) {
				Document doc = iterable.first();
				ItemBuilder builder = new ItemBuilder();
				builder.setItemId(doc.getString("item_id"));
				builder.setName(doc.getString("name"));
				builder.setAddress(doc.getString("address"));
				builder.setUrl(doc.getString("url"));
				builder.setImageUrl(doc.getString("image_url"));
				builder.setRating(doc.getDouble("rating"));
				builder.setDistance(doc.getDouble("distance"));
				builder.setCategories(getCategories(itemId));
				
				favoriteItems.add(builder.build());

			}
		}
		return favoriteItems;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		if (db == null) {
			return new HashSet<>();
		}
		Set<String> categories = new HashSet<>();
		FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", itemId));
		if (iterable.first() != null && iterable.first().containsKey("categories")) {
			@SuppressWarnings("unchecked")
			List<String> list = (List<String>) iterable.first().get("categories");
			categories.addAll(list);
		}
		return categories;
	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		// TODO Auto-generated method stub
		TicketMasterClient ticketMasterClient = new TicketMasterClient();
		List<Item> items = ticketMasterClient.search(lat, lon, term);
		
		for (Item item : items) {
			saveItem(item);
		}
		return items;
	}

	@Override
	public void saveItem(Item item) {
		if (db == null) {
			return;
		}
		FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", item.getItemId()));
		if (iterable.first() == null) {
			db.getCollection("items").insertOne(new Document().append("item_id", item.getItemId()).
					append("distance", item.getDistance()).append("name", item.getName()).append("address", item.getAddress()).
					append("url", item.getUrl()).append("image_url", item.getImageUrl()).append("rating", item.getRating()).
					append("categories", item.getCategories()));
		}

	}

	@Override
	public String getFullname(String userId) {
		FindIterable<Document> iterable = db.getCollection("users").find(new Document("user_id", userId));
		Document doc = iterable.first();
		String firstName = doc.getString("first_name");
		String lastName = doc.getString("last_name");
		return firstName + " " + lastName;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		Document doc = iterable.first();
		return doc.getString("password").equals(password);
	}
	
	  @Override
	  public boolean registerUser(String userId, String password, String firstname, String lastname) {
	    FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));

	    if (iterable.first() == null) {
	      db.getCollection("users").insertOne(new Document().append("first_name", firstname)
	          .append("last_name", lastname).append("password", password).append("user_id", userId));
	      return true;
	    }
	    return false;
	  }

}




















