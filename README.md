# cmpe273-lab4 (CRDT Implementation: Write, Read on Repair)

#Server side
cd to 'server' directory and then execute following commands

1. Type 'mvn clean package'

2. Initiate 3 Server instances

- For Server A: Type 'java -jar target/server-0.0.1-SNAPSHOT.jar server config/server_A_config.yml'

- For Server B: Type 'java -jar target/server-0.0.1-SNAPSHOT.jar server config/server_B_config.yml'

- For Server C: Type 'java -jar target/server-0.0.1-SNAPSHOT.jar server config/server_C_config.yml'

#Client Side
cd to 'client' directory and then execute following commands

1. Type 'mvn clean package'

2. Initiate Client instance

- Type 'java -jar target/client-0.0.1-SNAPSHOT.jar'
