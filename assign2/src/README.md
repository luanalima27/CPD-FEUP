# Distributed Chat System

## Requirements
- Java SE 21 or later

## Docker Requirements
1. sudo docker run -d -v ollama:/root/.ollama -p 11434:11434 --name ollama14 ollama/ollama

2. sudo docker exec -it ollama14 ollama run llama3


## Running the Server
1. Compile the project: ` javac -d bin src/server/src/main/java/com/chat/server/*.java src/server/src/main/java/com/chat/server/model/*.java`

2. Run the server: `java -cp bin com.chat.server.ServerMain `

## Running the Client

1. Compile the project `javac -d bin src/client/src/main/java/com/chat/client/*.java`

2. Run the client: `java -cp bin com.chat.client.ClientMain`


