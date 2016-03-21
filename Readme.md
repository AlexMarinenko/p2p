Реализовать приложение, работающее в одноранговой P2P сети. 
===========================================================

Базовый функционал:
-------------------
1. Любой узел может принять на вход сообщение.
2. Узел должен разослать сообщение всем остальным узлам в сети.
3. Каждый узел хранит у себя историю сообщений.
4. Необходим алгоритм консенсуса (новый узел при включении в сеть должен синхронизироваться автоматически; при сбое в работе узла он должен понять, что произошел сбой и синхронизироваться с сетью)

Дополнительные сведения:
------------------------
Формат сериализации сообщений и протокол общения между узлами - на свой выбор, с обоснованием, почему выбраны конкретные решения. В рамках тестовой задачи можно считать, что узлы находятся в одном VLAN

Использованные сторонние решения:
---------------------------------
1. Spring boot / общая структура приложения
2. Spring integration / организация общего flow
3. Spring MVC / организация REST контроллера
4. Spring statemachine / построение FSM для логики работы Node
5. Jackson / сериализация-десериализация данных

Сборка приложения:
------------------
```
mvn package
```

Конфигурация приложения:
------------------------
```
cp config/application.properties-dist config/application.properties

nano config/application.properties

p2p.heartbeatPeriod=1000 // период обновления
p2p.heartbeatExpired=1200 // время после которого узел исключается из рассмотрения
p2p.broadcastAddress=192.168.99.255 // бродкаст-адрес
p2p.listenerAddress=0.0.0.0 // адрес для listener-a входящих пакетов
p2p.port=8888 // UDP-порт
```

Запуск приложения:
------------------
```
mvn spring-boot:run
```

Heartbeat:
----------

```
Node                           Network
-----                          -------
  |                               |
  | Incoming Heartbeat broadcast  |
  |<------------------------------|
  |                               |
  | Refresh nodelist              |
  |--------+                      |
  |        |                      |
  |<-------+                      |
  |                               |
  
```

Synchronization flow:
---------------------

```
Node                           Network
-----                          -------
  |                               |
  | Incoming Heartbeat broadcast  |
  |<------------------------------|
  |    contains DB version        |
  |                               |
  | Compare local cache version   |  
  |----------+                    | 
  |          |                    |
  |<---------+                    |
  |                               |
  |  UpdateMeRequest              |  
  |------------------------------>|
  |  UpdateResponse               |
  |<------------------------------|
  |                               |      
  | Update local cache            | 
  |--------+                      | 
  |        |                      | 
  |<-------+                      | 
  |                               | 
  
```


Transaction flow:
-----------------

```
Client               Node                         Network
-------              -----                        -------
   |                   |                             |
   | Incoming message  |                             |
   |------------------>|                             |
   | Message accepted  |                             |
   |<------------------|                             |
   |                   |                             |   
   |                   | Start Transaction           |   
   |                   |---------------------------->|
   |                   |                             |
   |                   | Confirm Transaction Started |
   |                   |<----------------------------|
   |                   |                             |   
   |                   | Message Packet              |
   |                   |---------------------------->|
   |                   |                             |   
   |                   | Confirm Message Received    |
   |                   |<----------------------------|
   |                   |                             |   
   |                   | Commit                      |
   |                   |---------------------------->|
   |                   | Rollback                    |   
   |                   |---------------------------->|
   |                   |                             |
    
```

Send message:
-------------

```
curl -X POST --data '{ "content": "test message" }' -H 'Content-type: application/json' http://localhost:8080/send
```


Node state machine:
-------------------

```
+---------------+  Update   +-------------------+                        
|               |---------->|                   |                     
| InTransaction |  Received |  UpdatedByRemote  |                                            
|               |           |                   |                                     
+---------------+           +-------------------+  UpdateMeRequestReceived   +------------------------+
    ^                              | |      +------------------------------->|                        |
    |                       Commit/Rollback |      UpdateByRequestSent       | SendingUpdateByRequest | 
    |                         Received      |  +---------------------------->|                        |
    |                              | |      |  |                             +------------------------+
    | StartTransactionReceived     V V      |  V                         
    |                           +----------------+  UpdateMeRequestSent      +------------------------+
    +---------------------------|                |-------------------------->|                        |
                                |    Connected   |  UpdateByRequestReceived  | WaitForUpdateByRequest |
    +---------------------------|                |<--------------------------|                        |
    | IncomingMessageArrived    +----------------+                           +------------------------+
    |                              ^   |  ^  ^  ^     RollbackSent
    |                              |   |  |  |  +-----------------------------------------------+
    |       +----------------------+   |  |  |         CommitSent                               |  
    |       | IncomingMessageAccepted  |  |  +--------------------------------------------+     | 
    |       |                          |  +----------------------+                        |     |
    V       |                          |  StartTransactionFailed |                        |     | 
+-------------------------+            |       +--------------------+  Update        +----------------+
|                         |     Start  +------>|                    | -------------->|                |
| IncomingMessageReceived |     Transaction    | StartedTransaction |  RemoteNodes   | UpdatingRemote |
|                         |     Sent           |                    |                |                |
+-------------------------+                    +--------------------+                +----------------+
  
```

Node state transitions:
-----------------------

| Сигнал | Состояние | Следующее состояние | Действие |
|--------|-----------|---------------------|----------|
| - | Connected | Connected | - |
| StartTransactionReceived | Connected | InTranscaction | Старт транзакции, готовность к приёму апдейта |
| UpdateReceived | InTranscaction | UpdatedByRemote | Обновление хранилища, отправка подтверждения |
| CommitReceived | UpdatedByRemote | Connected | Фиксация состояния хранилища |
| RollbackReceived | UpdatedByRemote | Connected | Откат изменений последнего обновления |
| IncomingMessageArrived | Connected | IncomingMessageReceived | Сохранение входящего сообщения |
| IncomingMessageAccepted | IncomingMessageReceived | Connected | - |
| StartTransactionSent | Connected | StartedTransaction | Отправка бродкаста о старте транзакции, ожидание подтверждений |
| StartTransactionFailed | StartedTransaction | Connected | Обработка ошибки старта транзакции |
| UpdateRemoteNodes | StartedTransaction | UpdatingRemote | Отправка бродкаста с обновлением |
| RollbackSent | UpdatingRemote | Connected | Отправка бродкаста с сигналом Rollback, Запланировать повторное обновление позже |
| CommitSent | UpdatingRemote | Connected | Отправка бродкаста с сигналом Commit |
| UpdateMeRequestReceived | Connected | SendingUpdateByRequest | Отправка обновления по запросу |
| UpdateByRequestSent | SendingUpdateByRequest | Connected | - |
| UpdateMeRequestSent | Connected | WaitForUpdateByRequest | Отправка запроса на обновление, ожидание ответа |
| UpdateByRequestReceived | WaitForUpdateByRequest | Connected | Обновление хранилища |