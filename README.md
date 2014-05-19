This is your new Play application
=====================================

This file will be packaged with your application, when using `play dist`.

MongoController
---------------
```scala
class Users extends Controller with MongoController
```
MongoController provides nice handling of the JSON objects and a friendly wrapper to MongoDB

Mongo LastError
---------------
What is a LastError?

When a write operation is done on a collection, MongoDB does not send back a message to confirm that all went right or not.
To be sure that a write operation is successful, one must send a GetLastError command.
 When it receives such a command, MongoDB waits until the last operation is done and then sends back the result.
 This result is a LastError message.
    ReactiveMongo handles this by default
