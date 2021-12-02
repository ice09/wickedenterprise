package dev.wickedenterprise.prototyping.neo4jwriter;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.stereotype.Service;

import static org.neo4j.driver.Values.parameters;

@Service
public class GraphUpdateService {

	private final Driver driver;
	private final DatabaseSelectionProvider databaseSelectionProvider;

	GraphUpdateService(Driver driver, DatabaseSelectionProvider databaseSelectionProvider) {
		this.driver = driver;
		this.databaseSelectionProvider = databaseSelectionProvider;
	}

	private Session sessionFor(String database) {
		if (database == null) {
			return driver.session();
		}
		return driver.session(SessionConfig.forDatabase(database));
	}

	private String database() {
		return databaseSelectionProvider.getDatabaseSelection().getValue();
	}

	public void addTrustLine(LogEntry logEntry) {
		try (Session session = sessionFor(database())) {
			String stmt =
			"MERGE (u1:User {address: $trusterAddr}) "+
					"SET u1.name = $trusterName \n" +
					"SET u1.image_url = $trusterImgUrl \n" +
			"MERGE (u2:User {address: $trusteeAddr}) "+
					"SET u2.name = $trusteeName \n" +
					"SET u2.image_url = $trusteeImgUrl \n" +
			"MERGE (u1)-[r:TRUSTS]->(u2) "+
					"SET r.blockNumber = toInteger($blockNumber), r.amount = toFloat($amount)";
			session.run( stmt,
					parameters(
							"trusterAddr", logEntry.getTrusterAddress(),
							"trusterName", logEntry.getTrusterName(),
							"trusterImgUrl", logEntry.getTrusterAvatar(),
							"trusteeAddr", logEntry.getTrusteeAddress(),
							"trusteeName", logEntry.getTrusteeName(),
							"trusteeImgUrl", logEntry.getTrusteeAvatar(),
							"blockNumber", logEntry.getBlockNumber().longValue(), "amount", logEntry.getAmount().longValue()) );
		}
	}
}
