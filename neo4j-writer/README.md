## Run Neo4j 

* Create directories: `logs`, `import`, `plugins` and `data`
* Run

`docker run --name circlesubi-neo4j -p7687:7687 -d -v %cd%/neo4j/data:/data -v %cd%/neo4j/logs:/logs -v %cd%/neo4j/import:/var/lib/neo4j/import -v %cd%/neo4j/plugins:/plugins --env NEO4J_AUTH=neo4j/123654789 neo4j:latest`

## Run Neo4j Writer

* Start application with `Neo4jWriterApplication`