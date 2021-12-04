## Setup

* Start Nethermind: 
  * `.\Nethermind.Launcher.exe --JsonRpc.Enabled true --JsonRpc.Timeout 200000000`
  
## Run

* cd `docker-compose-fullstack`
* `docker-compose up -d` 

### Manual Index Creation

* Create Index on Neo4j
    * `CREATE CONSTRAINT ON (u:User) ASSERT u.address IS UNIQUE;`
* Create Index on RDMBS
    * `CREATE INDEX log_all_attrs_idx ON log_entry_entity (amount,block_number,trustee_address,trustee_name,trustee_avatar,truster_address,truster_avatar,truster_name);`

### Diagnosis

* Latest Block
  * Neo4j: `MATCH ()-[r:TRUSTS]-() RETURN r ORDER BY r.blockNumber DESC LIMIT 1`
  * RDBMS: `select max(block_number) from log_entry_entity;`