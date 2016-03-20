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
   |                   | Message Received            |
   |                   |<----------------------------|
   |                   |                             |   
   |                   | Commit                      |
   |                   |---------------------------->|
   
   
      
```

Synchronization flow:
---------------------

```
Node                         Network          Selected Node
-----                        -------          -------------
  |                             |                   |
  | Syncronization Request      |                   |
  |---------------------------->|                   |
  |                             |                   |
  | Versions responses          |                   |
  |<============================|                   |
  |                             |                   |
  | Select a node having max v  |                   |
  |---------+                   |                   |
  |         |                   |                   |
  |<--------+                   |                   |
  |                             |                   |
  | Get last DB request         |                   |
  |------------------------------------------------>|
  |                             |                   |
  | Last DB dat                 |                   |
  |<------------------------------------------------|
  |                             |                   |
  | Update local cache          |                   |
  |--------+                    |                   |
  |        |                    |                   |
  |<-------+                    |                   |
  |                             |                   |
  
```

Heartbeat:
----------

```
Node                         Network
-----                        -------
  |                             |
  | Ping broadcast (T=100ms)    |
  |---------------------------->|
  |                             |
    
Node                         Network
-----                        -------
  |                             |
  | Incoming Ping broadcast     |
  |<----------------------------|
  |                             |
  | Refresh nodelist            |
  |--------+                    |
  |        |                    |
  |<-------+                    |
  |                             |
  
```

Node state machine:
-------------------

```
+---------------+  Update   +-------------------+                        
|               |---------->|                   |                     
| InTransaction |  Received |  UpdatedByRemote  |                                            
|               |           |                   |                                     
+---------------+           +-------------------+  UpdateMeRequestReceived  +------------------------+
    ^                              | |      +------------------------------>|                        |
    |                       Commit/Rollback |      UpdateByRequestSent      | SendingUpdateByRequest | 
    |                         Received      |  +--------------------------->|                        |
    | StartTransactionReceived     | |      |  |                            +------------------------+
    |                              V V      |  V                         
    |                           +----------------+  UpdateMeRequestSent     +------------------------+
    +---------------------------|                |------------------------->|                        |
                                |    Connected   |  UpdateByRequestReceived | WaitForUpdateByRequest |
    +---------------------------|                |<-------------------------|                        |
    |                           +----------------+                          +------------------------+
    |                                 ^  ^
    |                                 |  |      RollbackSent
    | IncomingMessageArrived          |  +-------------------------------------------------+
    |                                 |         CommitSent                                 | 
    |                                 +----------------------------------------------+     | 
    V                                                                                |     | 
+-------------------------+  Start        +--------------------+  Update        +----------------+
|                         |-------------->|                    | -------------->|                |
| IncomingMessageReceived |  Transaction  | StartedTransaction |  RemoteNodes   | UpdatingRemote |
|                         |  Sent         |                    |                |                |
+-------------------------+               +--------------------+                +----------------+
  
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
| IncomingMessageArrived | Connected | IncomingMessageReceived | Формирование обновления |
| StartTransactionSent | IncomingMessageReceived | StartedTransaction | Отправка бродкаста о старте транзакции, ожидание подтверждений |
| UpdateRemoteNodes | StartedTransaction | UpdatingRemote | Отправка бродкаста с обновлением |
| RollbackSent | UpdatingRemote | Connected | Отправка бродкаста с сигналом Rollback, Запланировать повторное обновление позже |
| CommitSent | UpdatingRemote | Connected | Отправка бродкаста с сигналом Commit |
| UpdateMeRequestReceived | Connected | SendingUpdateByRequest | Отправка обновления по запросу |
| UpdateByRequestSent | SendingUpdateByRequest | Connected | - |
| UpdateMeRequestSent | Connected | WaitForUpdateByRequest | Отправка запроса на обновление, ожидание ответа |
| UpdateByRequestReceived | WaitForUpdateByRequest | Connected | Обновление хранилища |